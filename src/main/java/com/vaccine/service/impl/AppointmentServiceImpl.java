package com.vaccine.service.impl;

import com.vaccine.dto.AppointmentDTO;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.Child;
import com.vaccine.entity.Service;
import com.vaccine.entity.User;
import com.vaccine.repository.AppointmentRepository;
import com.vaccine.repository.ChildRepository;
import com.vaccine.repository.ServiceRepository;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentDTO appointmentDTO, User user) {
        Child child = childRepository.findById(appointmentDTO.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + appointmentDTO.getChildId()));
        
        // Verify the user is the parent of the child
        if (!child.getParent().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to book appointments for this child");
        }
        
        Service service = serviceRepository.findById(appointmentDTO.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + appointmentDTO.getServiceId()));
        
        if (!isValidAppointmentTime(appointmentDTO.getAppointmentDate())) {
            throw new RuntimeException("Invalid appointment time. Please choose a time during business hours.");
        }
        
        Appointment appointment = new Appointment();
        appointment.setChild(child);
        appointment.setService(service);
        appointment.setAppointmentDate(appointmentDTO.getAppointmentDate());
        appointment.setStatus(Appointment.Status.SCHEDULED);
        appointment.setNotes(appointmentDTO.getNotes());
        appointment.setCreatedAt(LocalDateTime.now());
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send notification to user
        notificationService.sendAppointmentConfirmation(user, savedAppointment);
        
        return savedAppointment;
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Override
    public List<Appointment> findByChildId(Long childId) {
        return appointmentRepository.findByChildId(childId);
    }

    @Override
    public List<Appointment> findByParentId(Long parentId) {
        return appointmentRepository.findByParentId(parentId);
    }

    @Override
    public List<Appointment> findByStatus(Appointment.Status status) {
        return appointmentRepository.findByStatus(status);
    }

    @Override
    public List<Appointment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<Appointment> findAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Long id, AppointmentDTO appointmentDTO) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        
        if (appointmentDTO.getChildId() != null) {
            Child child = childRepository.findById(appointmentDTO.getChildId())
                    .orElseThrow(() -> new RuntimeException("Child not found with id: " + appointmentDTO.getChildId()));
            appointment.setChild(child);
        }
        
        if (appointmentDTO.getServiceId() != null) {
            Service service = serviceRepository.findById(appointmentDTO.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service not found with id: " + appointmentDTO.getServiceId()));
            appointment.setService(service);
        }
        
        if (appointmentDTO.getAppointmentDate() != null) {
            if (!isValidAppointmentTime(appointmentDTO.getAppointmentDate())) {
                throw new RuntimeException("Invalid appointment time. Please choose a time during business hours.");
            }
            appointment.setAppointmentDate(appointmentDTO.getAppointmentDate());
        }
        
        if (appointmentDTO.getStatus() != null) {
            appointment.setStatus(appointmentDTO.getStatus());
        }
        
        if (appointmentDTO.getNotes() != null) {
            appointment.setNotes(appointmentDTO.getNotes());
        }
        
        appointment.setUpdatedAt(LocalDateTime.now());
        
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment updateAppointmentStatus(Long id, Appointment.Status status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        
        appointment.setStatus(status);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        // Send notification based on status change
        if (status == Appointment.Status.CONFIRMED) {
            notificationService.sendAppointmentConfirmation(appointment.getChild().getParent(), updatedAppointment);
        } else if (status == Appointment.Status.COMPLETED) {
            notificationService.sendVaccinationCompleted(appointment.getChild().getParent(), updatedAppointment);
        }
        
        return updatedAppointment;
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        
        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed appointment");
        }
        
        appointment.setStatus(Appointment.Status.CANCELLED);
        appointment.setCancelledReason(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        
        // Notify the user about cancellation
        notificationService.sendAppointmentCancellation(appointment.getChild().getParent(), cancelledAppointment);
        
        return cancelledAppointment;
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    public boolean isValidAppointmentTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        // Check if it's a weekday
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        // Check if it's within business hours (8 AM - 5 PM)
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(17, 0));
    }

    @Override
    public boolean canCancelAppointment(Long appointmentId, User user) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return false;
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Check if the user is the parent of the child
        boolean isParent = appointment.getChild().getParent().getId().equals(user.getId());
        
        // Check if the appointment is not yet completed or already cancelled
        boolean isNotCompletedOrCancelled = appointment.getStatus() != Appointment.Status.COMPLETED && 
                                           appointment.getStatus() != Appointment.Status.CANCELLED;
        
        // Check if the appointment is at least 24 hours in the future
        boolean is24HoursInFuture = appointment.getAppointmentDate().isAfter(LocalDateTime.now().plusHours(24));
        
        return isParent && isNotCompletedOrCancelled && is24HoursInFuture;
    }
    
    @Override
    public Integer countByStatusAndDateRange(Appointment.Status status, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.countByStatusAndDateRange(status, startDate, endDate);
    }
}
