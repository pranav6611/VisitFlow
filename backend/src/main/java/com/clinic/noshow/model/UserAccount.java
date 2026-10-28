package com.clinic.noshow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class UserAccount {
    public enum Role { DOCTOR, PATIENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Role role;

    @Column(nullable = false, length = 100)
    private String displayName;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", unique = true)
    private Doctor doctor;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", unique = true)
    private Patient patient;
}
