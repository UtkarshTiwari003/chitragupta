package com.attendance.service;

import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.ValidationException;
import com.attendance.model.Attendance;
import com.attendance.model.Notification;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Attendance service - marks attendance and calculates percentages
 */
@Slf4j
@Service
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    
    private static final BigDecimal LOW_ATTENDANCE_THRESHOLD = BigDecimal.valueOf(75);
    
    public AttendanceService(AttendanceRepository attendanceRepository,
                            NotificationRepository notificationRepository) {
        this.attendanceRepository = attendanceRepository;
        this.notificationRepository = notificationRepository;
    }
    
    /**
     * Mark attendance for a student on a date
     */
    @Transactional
    public void markAttendance(Long studentId, LocalDate attendanceDate, String status) {
        // Validate date is not in future
        if (attendanceDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Cannot mark attendance for future dates");
        }
        
        Attendance.Status statusEnum;
        try {
            statusEnum = Attendance.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid attendance status: " + status);
        }
        
        Attendance attendance = attendanceRepository
            .findByStudentIdAndAttendanceDate(studentId, attendanceDate)
            .orElse(Attendance.builder()
                .studentId(studentId)
                .attendanceDate(attendanceDate)
                .build());
        
        attendance.setStatus(statusEnum);
        attendanceRepository.save(attendance);
        
        log.info("Attendance marked for student {} on {}: {}", studentId, attendanceDate, status);
        
        // Check if attendance is low and create notification
        if (statusEnum == Attendance.Status.PRESENT || statusEnum == Attendance.Status.ABSENT) {
            BigDecimal percentage = calculateAttendancePercentage(studentId);
            if (percentage.compareTo(LOW_ATTENDANCE_THRESHOLD) < 0) {
                createLowAttendanceNotification(studentId, percentage);
            }
        }
    }
    
    /**
     * Calculate attendance percentage for a student
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAttendancePercentage(Long studentId) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        
        Long presentDays = attendanceRepository.countPresentDays(studentId, monthStart, now);
        Long totalDays = attendanceRepository.countTotalDays(studentId, monthStart, now);
        
        if (totalDays == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(presentDays)
            .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Get attendance records for a student in a date range
     */
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceRecords(Long studentId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, from, to);
    }
    
    /**
     * Create low attendance notification
     */
    private void createLowAttendanceNotification(Long studentId, BigDecimal percentage) {
        String message = String.format(
            "Your attendance is low at %.2f%%. Please maintain minimum 75%% attendance.",
            percentage
        );
        
        Notification notification = Notification.builder()
            .studentId(studentId)
            .notificationType("LOW_ATTENDANCE")
            .message(message)
            .status(Notification.Status.PENDING)
            .attempts(0)
            .nextRetryAt(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
        log.info("Low attendance notification created for student {}", studentId);
    }
}
