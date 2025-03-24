package com.vaccine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RoleName name;

    private String description;

    public enum RoleName {
        ROLE_ADMIN,
        ROLE_STAFF,
        ROLE_CUSTOMER,
        ROLE_GUEST
    }
    
    public Role(RoleName name) {
        this.name = name;
    }
}
