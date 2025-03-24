package com.vaccine.repository;

import com.vaccine.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByUserIdAndRead(Long userId, boolean read);
    
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);
    
    Integer countByUserIdAndRead(Long userId, boolean read);
}
