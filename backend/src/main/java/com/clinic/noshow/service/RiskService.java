package com.clinic.noshow.service;

import com.clinic.noshow.model.Appointment;
import com.clinic.noshow.model.Patient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Rule-based no-show risk model. Returns a score from 1 to 95 (percent chance of a no-show).
 * Each rule adds or subtracts points so the logic is easy to explain to clinic staff.
 * Replace with a trained model later if you collect enough history.
 */
@Service
public class RiskService {

    private static final int BASE = 10;

    public int score(Appointment a, LocalDateTime now) {
        Patient p = a.getPatient();
        double score = BASE;

        // 1. Patient history: the strongest signal.
        if (p.getTotalVisits() >= 3) {
            score += p.getNoShowRate() * 60;
        } else {
            score += 8; // new patient, little history, slightly riskier
        }

        // 2. Lead time: long gaps between booking and visit increase forgetting.
        long days = Duration.between(a.getCreatedAt(), a.getAppointmentTime()).toDays();
        if (days > 14) score += 15;
        else if (days > 7) score += 8;
        else if (days <= 1) score -= 4;

        // 3. Confirmation: a confirmed appointment is much less likely to be missed.
        if (a.getStatus() == Appointment.Status.CONFIRMED) score -= 12;

        // 4. Reminders sent without a reply.
        if (a.getStatus() != Appointment.Status.CONFIRMED && a.getReminderCount() >= 2) score += 6;

        // 5. Timing: early morning and Monday slots are missed more often.
        int hour = a.getAppointmentTime().getHour();
        if (hour < 9) score += 5;
        if (a.getAppointmentTime().getDayOfWeek() == DayOfWeek.MONDAY) score += 4;

        // 6. Past the slot already: not useful, but keep the score bounded.
        if (a.getAppointmentTime().isBefore(now)) score = Math.max(score, 1);

        return (int) Math.max(1, Math.min(95, Math.round(score)));
    }
}
