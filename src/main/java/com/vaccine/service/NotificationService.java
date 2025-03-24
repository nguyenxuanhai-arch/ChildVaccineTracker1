package com.vaccine.service;

import com.vaccine.entity.Appointment;
import com.vaccine.entity.Notification;
import com.vaccine.entity.User;
import com.vaccine.entity.Vaccination;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    
    Notification createNotification(User user, String title, String message, Notification.NotificationType type);
    
    Optional<Notification> findById(Long id);
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findUnreadByUserId(Long userId);
    
    void markAsRead(Long notificationId);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long id);
    
    void sendAppointmentConfirmation(User user, Appointment appointment);
    
    void sendAppointmentReminder(User user, Appointment appointment);
    
    void sendAppointmentCancellation(User user, Appointment appointment);
    
    void sendVaccinationReminder(User user, Vaccination vaccination);
    
    void sendVaccinationCompleted(User user, Appointment appointment);
    
    void scheduleVaccinationReminder(User user, Vaccination vaccination);
    
    int getUnreadCount(Long userId);
}
