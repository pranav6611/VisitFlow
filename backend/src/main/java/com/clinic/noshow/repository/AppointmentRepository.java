package com.clinic.noshow.repository;

import com.clinic.noshow.model.Appointment;
import com.clinic.noshow.model.Appointment.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.appointmentTime BETWEEN :from AND :to
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> findBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId AND a.appointmentTime BETWEEN :from AND :to
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> findDoctorBetween(@Param("doctorId") Long doctorId,
                                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Appointment> findByStatusInAndAppointmentTimeAfter(List<Status> statuses, LocalDateTime after);

    List<Appointment> findByPatient_IdOrderByAppointmentTimeDesc(Long patientId);

    List<Appointment> findByDoctor_IdAndAppointmentTimeBeforeOrderByAppointmentTimeDesc(
            Long doctorId, LocalDateTime before);

    List<Appointment> findByDoctor_IdAndAppointmentTimeAfterOrderByAppointmentTimeAsc(
            Long doctorId, LocalDateTime after);

    long countByStatus(Status status);

    long countByStatusAndAppointmentTimeBetween(Status status, LocalDateTime from, LocalDateTime to);
}
