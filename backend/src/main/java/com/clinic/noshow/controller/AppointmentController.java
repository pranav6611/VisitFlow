package com.clinic.noshow.controller;

import com.clinic.noshow.model.Appointment;
import com.clinic.noshow.model.Appointment.Status;
import com.clinic.noshow.repository.AppointmentRepository;
import com.clinic.noshow.service.AppointmentService;
import com.clinic.noshow.security.AccountPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;
    @SuppressWarnings("unused")
    private final AppointmentRepository repo;

    public AppointmentController(AppointmentService service, AppointmentRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    /** Appointments for a given day (defaults to today), sorted by time, with risk scores. */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Appointment> forDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal AccountPrincipal principal) {
        LocalDate d = date != null ? date : LocalDate.now();
        return service.forDay(d.atStartOfDay(), principal.actorId());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Object> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal AccountPrincipal principal) {
        LocalDate d = date != null ? date : LocalDate.now();
        return service.summary(d.atStartOfDay(), principal.actorId());
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Appointment> upcoming(@AuthenticationPrincipal AccountPrincipal principal) {
        return service.upcomingForDoctor(principal.actorId());
    }

    @GetMapping("/past")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Appointment> past(@AuthenticationPrincipal AccountPrincipal principal) {
        return service.pastForDoctor(principal.actorId());
    }

    public record PatientAppointment(Long id, String doctor, String specialty,
                                     LocalDateTime appointmentTime, String status, String reason) {}

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PATIENT')")
    public List<PatientAppointment> mine(@AuthenticationPrincipal AccountPrincipal principal) {
        return repo.findByPatient_IdOrderByAppointmentTimeDesc(principal.actorId()).stream()
                .map(a -> new PatientAppointment(a.getId(), a.getDoctor().getName(), a.getDoctor().getSpecialty(),
                        a.getAppointmentTime(), a.getStatus().name(), a.getReason()))
                .toList();
    }

    public record CreateRequest(Long patientId, @NotNull Long doctorId,
                                @NotNull @Future LocalDateTime appointmentTime,
                                @Size(max = 200) String reason) {}

    public record PatientBookingRequest(@NotNull Long doctorId,
                                        @NotNull @Future LocalDateTime appointmentTime,
                                        @Size(max = 200) String reason) {}

    /** Patients can book any number of appointments for themselves. */
    @PostMapping("/mine")
    @PreAuthorize("hasRole('PATIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientAppointment bookForSelf(@Valid @RequestBody PatientBookingRequest req,
                                          @AuthenticationPrincipal AccountPrincipal principal) {
        Appointment appointment = service.create(principal.actorId(), req.doctorId(),
                req.appointmentTime(), req.reason());
        return new PatientAppointment(appointment.getId(), appointment.getDoctor().getName(),
                appointment.getDoctor().getSpecialty(), appointment.getAppointmentTime(),
                appointment.getStatus().name(), appointment.getReason());
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment create(@Valid @RequestBody CreateRequest req, @AuthenticationPrincipal AccountPrincipal principal) {
        return service.create(req.patientId(), principal.actorId(), req.appointmentTime(), req.reason());
    }

    public record StatusRequest(Status status) {}

    /** Set COMPLETED, NO_SHOW, CONFIRMED, CANCELLED or SCHEDULED. */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public Appointment setStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        return service.setStatus(id, req.status());
    }

    /** Logs a reminder sent to the patient (SMS, call or WhatsApp). */
    @PostMapping("/{id}/remind")
    @PreAuthorize("hasRole('DOCTOR')")
    public Appointment remind(@PathVariable Long id) {
        return service.sendReminder(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void cancel(@PathVariable Long id) {
        service.setStatus(id, Status.CANCELLED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("error", ex.getMessage());
    }
}
