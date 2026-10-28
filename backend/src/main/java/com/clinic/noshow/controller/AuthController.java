package com.clinic.noshow.controller;

import com.clinic.noshow.security.AccountPrincipal;
import com.clinic.noshow.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password,
                               @NotBlank @Pattern(regexp = "DOCTOR|PATIENT") String role) {}
    public record DoctorRegistration(
            @NotBlank @Size(max = 100) String name,
            @Email @NotBlank @Size(max = 120) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 80) String specialty) {}
    public record PatientRegistration(
            @NotBlank @Size(max = 100) String name,
            @Email @NotBlank @Size(max = 120) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 30) String phone) {}

    @PostMapping("/login")
    public AuthService.AuthResult login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request.email(), request.password(), request.role());
    }

    @PostMapping("/register/doctor")
    public AuthService.AuthResult registerDoctor(@Valid @RequestBody DoctorRegistration request) {
        return auth.registerDoctor(request.name(), request.email(), request.password(), request.specialty());
    }

    @PostMapping("/register/patient")
    public AuthService.AuthResult registerPatient(@Valid @RequestBody PatientRegistration request) {
        return auth.registerPatient(request.name(), request.email(), request.password(), request.phone());
    }

    @GetMapping("/me")
    public AuthService.AuthResult me(@AuthenticationPrincipal AccountPrincipal principal) {
        return auth.current(principal);
    }
}
