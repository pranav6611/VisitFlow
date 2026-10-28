package com.clinic.noshow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appt_time", columnList = "appointmentTime"),
        @Index(name = "idx_appt_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    public enum Status { SCHEDULED, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Column(length = 200)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Status status = Status.SCHEDULED;

    private int reminderCount;

    private LocalDateTime lastReminderAt;

    /** 1-95: estimated chance (%) that the patient will not show up. */
    private int riskScore;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.SCHEDULED;
    }

    public String getRiskLevel() {
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 30) return "MEDIUM";
        return "LOW";
    }
}
