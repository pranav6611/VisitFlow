package com.clinic.noshow.model;

public enum AppointmentStatus {
    SCHEDULED,   // booked, patient has not confirmed
    CONFIRMED,   // patient confirmed attendance
    COMPLETED,   // patient attended
    NO_SHOW,     // patient did not turn up
    CANCELLED
}
