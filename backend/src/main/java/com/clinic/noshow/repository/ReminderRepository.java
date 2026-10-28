package com.clinic.noshow.repository;

import com.clinic.noshow.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findTop50ByOrderBySentAtDesc();

    void deleteByAppointmentId(Long appointmentId);
}
