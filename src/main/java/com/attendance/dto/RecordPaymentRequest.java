package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Record fee payment request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordPaymentRequest {
    private Long feeId;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private String transactionId;
}

/**
 * Fee status response
 */
@Data
class FeeDTO {
    public Long id;
    public Long studentId;
    public String feeType;
    public BigDecimal amount;
    public BigDecimal outstandingAmount;
    public LocalDate dueDate;
    public String status;
}
