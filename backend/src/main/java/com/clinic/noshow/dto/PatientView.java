package com.clinic.noshow.dto;

public record PatientView(
        Long id,
        String name,
        String phone,
        String email,
        int visits,
        int noShows,
        double noShowRate,
        int upcoming,
        String reliability) {
}
