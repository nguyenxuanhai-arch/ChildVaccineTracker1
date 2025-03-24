package com.vaccine.dto;

import com.vaccine.entity.Appointment;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {

    private Long id;

    @NotNull(message = "Child ID is required")
    private Long childId;

    private String childName;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private String serviceName;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be in the present or future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime appointmentDate;

    private Appointment.Status status;

    private String notes;

    private String cancelledReason;
}
