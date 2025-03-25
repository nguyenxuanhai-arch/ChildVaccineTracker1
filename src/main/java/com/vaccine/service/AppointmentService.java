package com.vaccine.service;

import com.vaccine.dto.AppointmentDTO;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    
    Appointment createAppointment(AppointmentDTO appointmentDTO, User user);
    
    Optional<Appointment> findById(Long id);
    
    List<Appointment> findByChildId(Long childId);
    
    List<Appointment> findByParentId(Long parentId);
    
    List<Appointment> findByStatus(Appointment.Status status);
    
    List<Appointment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Appointment> findAllAppointments();
    
    Appointment updateAppointment(Long id, AppointmentDTO appointmentDTO);
    
    Appointment updateAppointmentStatus(Long id, Appointment.Status status);
    
    Appointment cancelAppointment(Long id, String reason);
    
    void deleteAppointment(Long id);
    
    boolean isValidAppointmentTime(LocalDateTime dateTime);
    
    boolean canCancelAppointment(Long appointmentId, User user);
    
    Integer countByStatusAndDateRange(Appointment.Status status, LocalDateTime startDate, LocalDateTime endDate);
}
