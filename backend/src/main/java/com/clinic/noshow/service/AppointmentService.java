package com.clinic.noshow.service;

import com.clinic.noshow.model.Appointment;
import com.clinic.noshow.model.Appointment.Status;
import com.clinic.noshow.model.Doctor;
import com.clinic.noshow.model.Patient;
import com.clinic.noshow.repository.AppointmentRepository;
import com.clinic.noshow.repository.DoctorRepository;
import com.clinic.noshow.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Set<Status> OPEN = EnumSet.of(Status.SCHEDULED, Status.CONFIRMED);

    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final RiskService risk;

    public AppointmentService(AppointmentRepository appointments, PatientRepository patients,
                              DoctorRepository doctors, RiskService risk) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
        this.risk = risk;
    }

    /** Upcoming appointments for a day, with risk scores refreshed. */
    @Transactional
    public List<Appointment> forDay(LocalDateTime start, Long doctorId) {
        LocalDateTime end = start.plusDays(1);
        List<Appointment> list = appointments.findDoctorBetween(doctorId, start, end);
        LocalDateTime now = LocalDateTime.now();
        list.forEach(a -> {
            if (OPEN.contains(a.getStatus())) a.setRiskScore(risk.score(a, now));
        });
        return list;
    }

    /** All open appointments across future dates for one doctor. */
    @Transactional
    public List<Appointment> upcomingForDoctor(Long doctorId) {
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        List<Appointment> list = appointments
                .findByDoctor_IdAndAppointmentTimeAfterOrderByAppointmentTimeAsc(doctorId, startOfTomorrow);
        LocalDateTime now = LocalDateTime.now();
        list.forEach(a -> {
            if (OPEN.contains(a.getStatus())) a.setRiskScore(risk.score(a, now));
        });
        return list;
    }

    /** All appointments before today, newest first, for a doctor's history. */
    @Transactional(readOnly = true)
    public List<Appointment> pastForDoctor(Long doctorId) {
        return appointments.findByDoctor_IdAndAppointmentTimeBeforeOrderByAppointmentTimeDesc(
                doctorId, LocalDate.now().atStartOfDay());
    }

    @Transactional
    public Appointment create(Long patientId, Long doctorId, LocalDateTime when, String reason) {
        @SuppressWarnings("null")
        Patient p = patients.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("Patient not found"));
        Appointment a = new Appointment();
        a.setPatient(p);
        a.setDoctor(doctorFromId(doctorId));
        a.setAppointmentTime(when);
        a.setReason(reason);
        a.setStatus(Status.SCHEDULED);
        a.setCreatedAt(LocalDateTime.now()); // needed by the risk model before the first save
        a.setRiskScore(risk.score(a, LocalDateTime.now()));
        return appointments.save(a);
    }

    @Transactional
    public Appointment setStatus(Long id, Status status) {
        @SuppressWarnings("null")
        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found"));
        Status previous = a.getStatus();
        a.setStatus(status);

        // Keep the patient's history in step with the outcome.
        Patient p = a.getPatient();
        if (previous != status) {
            if (status == Status.COMPLETED || status == Status.NO_SHOW) {
                if (previous != Status.COMPLETED && previous != Status.NO_SHOW) {
                    p.setTotalVisits(p.getTotalVisits() + 1);
                }
                if (status == Status.NO_SHOW && previous != Status.NO_SHOW) {
                    p.setNoShows(p.getNoShows() + 1);
                }
                if (previous == Status.NO_SHOW && status != Status.NO_SHOW) {
                    p.setNoShows(Math.max(0, p.getNoShows() - 1));
                }
            }
        }
        if (OPEN.contains(status)) {
            a.setRiskScore(risk.score(a, LocalDateTime.now()));
        } else {
            a.setRiskScore(0);
        }
        return a;
    }

    /** Records that a reminder was sent to the patient. */
    @Transactional
    public Appointment sendReminder(Long id) {
        @SuppressWarnings("null")
        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found"));
        a.setReminderCount(a.getReminderCount() + 1);
        a.setLastReminderAt(LocalDateTime.now());
        a.setRiskScore(risk.score(a, LocalDateTime.now()));
        return a;
    }

    /** Dashboard summary for the selected day. */
    @Transactional(readOnly = true)
    public Map<String, Object> summary(LocalDateTime start, Long doctorId) {
        List<Appointment> day = appointments.findDoctorBetween(doctorId, start, start.plusDays(1));
        long total = day.size();
        long noShow = day.stream().filter(a -> a.getStatus() == Status.NO_SHOW).count();
        long completed = day.stream().filter(a -> a.getStatus() == Status.COMPLETED).count();
        long highRisk = day.stream()
                .filter(a -> OPEN.contains(a.getStatus()) && a.getRiskScore() >= 60).count();
        long confirmed = day.stream().filter(a -> a.getStatus() == Status.CONFIRMED).count();
        long open = day.stream().filter(a -> OPEN.contains(a.getStatus())).count();
        double rate = (completed + noShow) == 0 ? 0 : (double) noShow / (completed + noShow);

        return Map.of(
                "total", total,
                "open", open,
                "confirmed", confirmed,
                "completed", completed,
                "noShows", noShow,
                "highRisk", highRisk,
                "noShowRate", Math.round(rate * 1000) / 10.0
        );
    }

    @SuppressWarnings("null")
    private Doctor doctorFromId(Long id) {
        return doctors.findById(id).orElseThrow(() -> new NoSuchElementException("Doctor not found"));
    }
}
