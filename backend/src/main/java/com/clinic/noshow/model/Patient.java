package com.clinic.noshow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String phone;

    @Size(max = 120)
    @Column(length = 120)
    private String email;

    /** Completed + no-show visits (past appointments that were not cancelled). */
    private int totalVisits;

    private int noShows;

    /** Derived value, included in the JSON response. */
    public double getNoShowRate() {
        return totalVisits == 0 ? 0 : (double) noShows / totalVisits;
    }
}
