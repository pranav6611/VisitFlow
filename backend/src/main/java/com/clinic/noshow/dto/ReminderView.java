package com.clinic.noshow.dto;

import java.time.LocalDateTime;

public record ReminderView(
        Long id,
        Long appointmentId,
        String patientName,
        String phone,
        String doctor,
        LocalDateTime appointmentTime,
        String channel,
        String message,
        LocalDateTime sentAt) {
}
