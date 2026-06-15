package com.attendance.repository;

import com.attendance.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification repository - data access for notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatus(Notification.Status status);
    List<Notification> findByStudentId(Long studentId);
    List<Notification> findByStatusAndNextRetryAtBefore(Notification.Status status, LocalDateTime time);
}
