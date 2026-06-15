package com.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fee payment - payment transactions
 */
@Entity
@Table(name = "fee_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long feeId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;
    
    @Column(nullable = false, length = 50)
    private String paymentMethod;
    
    @Column(length = 100)
    private String transactionId;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime paymentDate;
}
