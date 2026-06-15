package com.attendance.service;

import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.ValidationException;
import com.attendance.model.Fee;
import com.attendance.model.FeePayment;
import com.attendance.model.Notification;
import com.attendance.repository.FeePaymentRepository;
import com.attendance.repository.FeeRepository;
import com.attendance.repository.NotificationRepository;
import com.attendance.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fee service - manages fee records, payments, and reminders
 */
@Slf4j
@Service
public class FeeService {
    
    private final FeeRepository feeRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    
    private static final int FEE_REMINDER_DAYS = 5;
    
    public FeeService(FeeRepository feeRepository,
                     FeePaymentRepository feePaymentRepository,
                     NotificationRepository notificationRepository,
                     StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
    }
    
    /**
     * Record a fee payment
     */
    @Transactional
    public void recordPayment(Long feeId, BigDecimal amountPaid, String paymentMethod, String transactionId) {
        Fee fee = feeRepository.findById(feeId)
            .orElseThrow(() -> new ResourceNotFoundException("Fee", "id", feeId));
        
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be greater than zero");
        }
        
        if (amountPaid.compareTo(fee.getOutstandingAmount()) > 0) {
            throw new ValidationException("Payment amount exceeds outstanding amount");
        }
        
        // Record payment
        FeePayment payment = FeePayment.builder()
            .feeId(feeId)
            .amountPaid(amountPaid)
            .paymentMethod(paymentMethod)
            .transactionId(transactionId)
            .build();
        feePaymentRepository.save(payment);
        
        // Update fee outstanding amount and status
        BigDecimal newOutstanding = fee.getOutstandingAmount().subtract(amountPaid);
        fee.setOutstandingAmount(newOutstanding);
        
        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            fee.setStatus(Fee.Status.PAID);
        } else if (fee.getStatus() == Fee.Status.OUTSTANDING) {
            fee.setStatus(Fee.Status.PARTIAL);
        }
        
        fee.setUpdatedAt(LocalDateTime.now());
        feeRepository.save(fee);
        
        log.info("Payment recorded for fee {}: {} via {}", feeId, amountPaid, paymentMethod);
    }
    
    /**
     * Get student's fee status
     */
    @Transactional(readOnly = true)
    public List<Fee> getStudentFees(Long studentId) {
        return feeRepository.findByStudentId(studentId);
    }
    
    /**
     * Get count of outstanding fees for a student
     */
    @Transactional(readOnly = true)
    public Integer getOutstandingFeesCount(Long studentId) {
        return feeRepository.countOutstandingFees(studentId);
    }
    
    /**
     * Scheduled task: Send fee reminders at 8 AM daily for fees 5+ days overdue
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void sendFeeReminders() {
        LocalDate reminderDate = LocalDate.now().minusDays(FEE_REMINDER_DAYS);
        List<Fee> overdueFees = feeRepository.findOverdueFees(reminderDate);
        
        for (Fee fee : overdueFees) {
            if (fee.getStatus() != Fee.Status.PAID) {
                createFeeReminderNotification(fee);
            }
        }
        
        log.info("Fee reminders scheduled for {} overdue fees", overdueFees.size());
    }
    
    private void createFeeReminderNotification(Fee fee) {
        String message = String.format(
            "Your fee of %.2f is overdue. Please pay immediately to avoid penalties.",
            fee.getOutstandingAmount()
        );
        
        Notification notification = Notification.builder()
            .studentId(fee.getStudentId())
            .notificationType("FEE_REMINDER")
            .message(message)
            .status(Notification.Status.PENDING)
            .attempts(0)
            .nextRetryAt(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
        log.info("Fee reminder notification created for student {}", fee.getStudentId());
    }
}
