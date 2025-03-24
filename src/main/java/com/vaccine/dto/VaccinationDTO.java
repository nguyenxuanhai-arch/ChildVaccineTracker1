package com.vaccine.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class VaccinationDTO {

    private Long id;

    @NotNull(message = "Child ID is required")
    private Long childId;

    @NotNull(message = "Vaccine ID is required")
    private Long vaccineId;

    private String vaccineName;

    @NotNull(message = "Vaccination date is required")
    @PastOrPresent(message = "Vaccination date cannot be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vaccinationDate;

    private Integer doseNumber;

    private String batchNumber;

    private String administeredBy;

    private String administeredAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDoseDue;

    private String sideEffectsReported;
}
