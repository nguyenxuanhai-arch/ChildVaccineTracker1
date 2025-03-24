package com.vaccine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vaccines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String manufacturer;

    @Column(length = 1000)
    private String description;

    @Column(name = "min_age_months")
    private Integer minAgeMonths;

    @Column(name = "max_age_months")
    private Integer maxAgeMonths;

    @Column(name = "doses_required")
    private Integer dosesRequired;

    @Column(name = "days_between_doses")
    private Integer daysBetweenDoses;

    private Boolean required = false;

    @Column(name = "side_effects", length = 1000)
    private String sideEffects;

    @ManyToMany(mappedBy = "vaccines")
    private Set<Service> services = new HashSet<>();

    @OneToMany(mappedBy = "vaccine", cascade = CascadeType.ALL)
    private Set<Vaccination> vaccinations = new HashSet<>();
}
