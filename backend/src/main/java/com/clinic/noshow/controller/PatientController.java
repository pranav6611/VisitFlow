package com.clinic.noshow.controller;

import com.clinic.noshow.model.Patient;
import com.clinic.noshow.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository repo;

    public PatientController(PatientRepository repo) {
        this.repo = repo;
    }

    /** All patients, highest no-show rate first. */
    @SuppressWarnings("null")
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Patient> list() {
        return repo.findAll().stream()
                .sorted(Comparator.comparingDouble(Patient::getNoShowRate).reversed())
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@Valid @RequestBody Patient p) {
        p.setId(null);
        p.setTotalVisits(0);
        p.setNoShows(0);
        return repo.save(p);
    }
}
