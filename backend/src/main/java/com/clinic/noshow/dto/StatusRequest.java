package com.clinic.noshow.dto;

import com.clinic.noshow.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull AppointmentStatus status) {
}
