package Notifications.Notifications.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Notifications.Notifications.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipient(String recipient);
    
    List<Notification> findByType(String type);
    
    List<Notification> findBySentAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Notification> findBySuccess(boolean success);
} 