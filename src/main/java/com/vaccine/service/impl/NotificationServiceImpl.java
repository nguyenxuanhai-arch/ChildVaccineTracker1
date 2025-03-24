package com.vaccine.service.impl;

import com.vaccine.entity.Appointment;
import com.vaccine.entity.Notification;
import com.vaccine.entity.User;
import com.vaccine.entity.Vaccination;
import com.vaccine.repository.NotificationRepository;
import com.vaccine.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(User user, String title, String message, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> findUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndRead(userId, false);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndRead(userId, false);
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void sendAppointmentConfirmation(User user, Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String formattedDate = appointment.getAppointmentDate().format(formatter);
        
        String title = "Appointment Confirmed";
        String message = "Your appointment for " + appointment.getChild().getFullName() + 
                         " has been confirmed for " + formattedDate + " for service: " + 
                         appointment.getService().getName() + ".";
        
        createNotification(user, title, message, Notification.NotificationType.APPOINTMENT_REMINDER);
    }

    @Override
    @Transactional
    public void sendAppointmentReminder(User user, Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String formattedDate = appointment.getAppointmentDate().format(formatter);
        
        String title = "Appointment Reminder";
        String message = "Reminder: You have an appointment for " + appointment.getChild().getFullName() + 
                         " tomorrow at " + formattedDate + " for service: " + 
                         appointment.getService().getName() + ".";
        
        createNotification(user, title, message, Notification.NotificationType.APPOINTMENT_REMINDER);
    }

    @Override
    @Transactional
    public void sendAppointmentCancellation(User user, Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String formattedDate = appointment.getAppointmentDate().format(formatter);
        
        String title = "Appointment Cancelled";
        String message = "Your appointment for " + appointment.getChild().getFullName() + 
                         " scheduled for " + formattedDate + " has been cancelled.";
        
        if (appointment.getCancelledReason() != null && !appointment.getCancelledReason().isEmpty()) {
            message += " Reason: " + appointment.getCancelledReason();
        }
        
        createNotification(user, title, message, Notification.NotificationType.SCHEDULE_CHANGE);
    }

    @Override
    @Transactional
    public void sendVaccinationReminder(User user, Vaccination vaccination) {
        String title = "Vaccination Due";
        String message = "Your child " + vaccination.getChild().getFullName() + 
                         " is due for " + vaccination.getVaccine().getName() + 
                         " vaccine on " + vaccination.getNextDoseDue().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + ".";
        
        createNotification(user, title, message, Notification.NotificationType.VACCINATION_DUE);
    }

    @Override
    @Transactional
    public void sendVaccinationCompleted(User user, Appointment appointment) {
        String title = "Vaccination Completed";
        String message = "Vaccination for " + appointment.getChild().getFullName() + 
                         " has been completed. You can view the details in your child's vaccination history.";
        
        createNotification(user, title, message, Notification.NotificationType.SYSTEM_NOTIFICATION);
    }

    @Override
    @Transactional
    public void scheduleVaccinationReminder(User user, Vaccination vaccination) {
        // This method would be called when creating a vaccination record
        // It doesn't immediately create a notification but "schedules" it
        // The scheduled task below will later check and send the actual notification
        // We might store some metadata to help the scheduled task if needed
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
    
    // Scheduled tasks for automatic notifications
    
    @Scheduled(cron = "0 0 8 * * ?") // Every day at 8 AM
    @Transactional
    public void sendDailyAppointmentReminders() {
        // This would check for appointments tomorrow and send reminders
        // Implementation would depend on external services and application context
    }
    
    @Scheduled(cron = "0 0 8 * * ?") // Every day at 8 AM
    @Transactional
    public void sendDueVaccinationReminders() {
        // Check for vaccinations coming due in the next 7 days
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);
        
        // Implementation would get vaccinations with next dose due date in this range
        // and send reminders to parents
    }
}
