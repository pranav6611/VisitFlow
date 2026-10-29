package com.clinic.noshow.dto;

import java.util.List;

public record DashboardView(
        int totalAppointments,
        int completed,
        int noShows,
        int cancelled,
        double noShowRate,
        int todayCount,
        int upcomingCount,
        int highRiskUpcoming,
        int remindersSent,
        /** Upcoming appointments within 48h that are unconfirmed and medium/high risk. */
        int remindable,
        RiskMix riskMix,
        List<WeekdayStat> byWeekday,
        List<WeekStat> weeklyTrend,
        List<AppointmentView> nextUp) {

    public record RiskMix(int low, int medium, int high) {
    }

    public record WeekdayStat(String day, int total, int noShows, double rate) {
    }

    public record WeekStat(String label, int total, int noShows, double rate) {
    }
}
