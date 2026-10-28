package com.clinic.noshow.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * An appointment as shown in the UI. Risk fields are only filled for upcoming
 * (SCHEDULED / CONFIRMED) appointments, and are calculated live on every request.
 */
public record AppointmentView(
        Long id,
        Long patientId,
        String patientName,
        String phone,
        String email,
        String doctor,
        String department,
        LocalDateTime appointmentTime,
        String status,
        Integer riskScore,
        String riskLevel,
        List<String> riskFactors,
        int remindersSent,
        int patientVisits,
        int patientNoShows,
        String notes) {
}
