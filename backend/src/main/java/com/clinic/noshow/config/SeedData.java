package com.clinic.noshow.config;

import com.clinic.noshow.model.Appointment;
import com.clinic.noshow.model.Appointment.Status;
import com.clinic.noshow.model.Doctor;
import com.clinic.noshow.model.Patient;
import com.clinic.noshow.repository.AppointmentRepository;
import com.clinic.noshow.repository.DoctorRepository;
import com.clinic.noshow.repository.PatientRepository;
import com.clinic.noshow.service.RiskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Demo data on the first run only, so the dashboard isn't empty. */
@Configuration
public class SeedData {

    @SuppressWarnings("null")
    @Bean
    CommandLineRunner seed(DoctorRepository doctors, PatientRepository patients,
                           AppointmentRepository appts, RiskService risk) {
        return args -> {
            if (doctors.count() > 0) return;

            Doctor d1 = doctors.save(new Doctor("Dr. Meera Iyer", "General Practice"));
            Doctor d2 = doctors.save(new Doctor("Dr. Arjun Rao", "Dermatology"));

            Patient p1 = patients.save(patient("Ravi Kumar", "+91 98100 11111", 12, 5));
            Patient p2 = patients.save(patient("Sneha Gupta", "+91 98100 22222", 8, 0));
            Patient p3 = patients.save(patient("Imran Sheikh", "+91 98100 33333", 6, 4));
            Patient p4 = patients.save(patient("Lakshmi Nair", "+91 98100 44444", 1, 0));
            Patient p5 = patients.save(patient("Tarun Bose", "+91 98100 55555", 0, 0));
            Patient p6 = patients.save(patient("Ananya Das", "+91 98100 66666", 15, 1));

            LocalDate today = LocalDate.now();
            Object[][] plan = {
                    { p1, d1, 9, 0, Status.CONFIRMED, 14 },
                    { p2, d1, 9, 30, Status.SCHEDULED, 2 },
                    { p3, d1, 10, 0, Status.SCHEDULED, 9 },
                    { p4, d1, 11, 0, Status.CONFIRMED, 1 },
                    { p5, d2, 11, 30, Status.SCHEDULED, 0 },
                    { p6, d2, 14, 0, Status.SCHEDULED, 16 },
                    { p1, d2, 15, 30, Status.SCHEDULED, 3 },
                    { p2, d1, 16, 0, Status.SCHEDULED, 5 },
            };

            for (Object[] row : plan) {
                Appointment a = new Appointment();
                a.setPatient((Patient) row[0]);
                a.setDoctor((Doctor) row[1]);
                a.setAppointmentTime(LocalDateTime.of(today,
                        LocalTime.of((Integer) row[2], (Integer) row[3])));
                a.setStatus((Status) row[4]);
                a.setReason("Follow-up");
                a.setCreatedAt(LocalDateTime.now().minusDays((Integer) row[5]));
                a.setRiskScore(risk.score(a, LocalDateTime.now()));
                appts.save(a);
            }
        };
    }

    private Patient patient(String name, String phone, int visits, int noShows) {
        Patient p = new Patient();
        p.setName(name);
        p.setPhone(phone);
        p.setTotalVisits(visits);
        p.setNoShows(noShows);
        return p;
    }
}
