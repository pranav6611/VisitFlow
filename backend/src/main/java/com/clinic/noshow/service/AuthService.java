package com.clinic.noshow.service;

import com.clinic.noshow.model.Doctor;
import com.clinic.noshow.model.Patient;
import com.clinic.noshow.model.UserAccount;
import com.clinic.noshow.repository.DoctorRepository;
import com.clinic.noshow.repository.PatientRepository;
import com.clinic.noshow.repository.UserAccountRepository;
import com.clinic.noshow.security.AccountPrincipal;
import com.clinic.noshow.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {
    public record AuthResult(String token, Long id, Long actorId, String email, String name, String role) {}

    private final UserAccountRepository accounts;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthService(UserAccountRepository accounts, DoctorRepository doctors, PatientRepository patients,
                       PasswordEncoder passwords, JwtService jwt) {
        this.accounts = accounts;
        this.doctors = doctors;
        this.patients = patients;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResult registerDoctor(String name, String email, String password, String specialty) {
        ensureEmailAvailable(email);
        Doctor doctor = doctors.save(new Doctor(name.trim(), specialty.trim()));
        UserAccount account = newAccount(name, email, password, UserAccount.Role.DOCTOR);
        account.setDoctor(doctor);
        return response(accounts.save(account));
    }

    @Transactional
    public AuthResult registerPatient(String name, String email, String password, String phone) {
        ensureEmailAvailable(email);
        Patient patient = new Patient();
        patient.setName(name.trim());
        patient.setEmail(normalizeEmail(email));
        patient.setPhone(phone.trim());
        Patient savedPatient = patients.save(patient);
        UserAccount account = newAccount(name, email, password, UserAccount.Role.PATIENT);
        account.setPatient(savedPatient);
        return response(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password, String role) {
        UserAccount account = accounts.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(a -> passwords.matches(password, a.getPasswordHash())
                        && a.getRole().name().equals(role))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
        return response(account);
    }

    @Transactional(readOnly = true)
    public AuthResult current(AccountPrincipal principal) {
        @SuppressWarnings("null")
        UserAccount account = accounts.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        return response(account);
    }

    private UserAccount newAccount(String name, String email, String password, UserAccount.Role role) {
        UserAccount account = new UserAccount();
        account.setDisplayName(name.trim());
        account.setEmail(normalizeEmail(email));
        account.setPasswordHash(passwords.encode(password));
        account.setRole(role);
        return account;
    }

    private void ensureEmailAvailable(String email) {
        if (accounts.existsByEmailIgnoreCase(normalizeEmail(email))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
    }

    private AuthResult response(UserAccount account) {
        Long actorId = account.getRole() == UserAccount.Role.DOCTOR
                ? account.getDoctor().getId() : account.getPatient().getId();
        return new AuthResult(jwt.issue(account), account.getId(), actorId, account.getEmail(),
                account.getDisplayName(), account.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
