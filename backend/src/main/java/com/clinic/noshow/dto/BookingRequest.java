package com.clinic.noshow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotBlank @Size(max = 80) String patientName,
        @NotBlank @Size(max = 20) String phone,
        @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 80) String doctor,
        @NotBlank @Size(max = 60) String department,
        @NotNull @Future LocalDateTime appointmentTime,
        @Size(max = 500) String notes) {
}
