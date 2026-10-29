package com.clinic.noshow.dto;

/** channel is SMS, WHATSAPP or EMAIL. Defaults to SMS when missing. */
public record ReminderRequest(String channel) {
}
