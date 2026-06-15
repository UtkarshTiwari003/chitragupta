package com.attendance.repository;

import com.attendance.model.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Fee payment repository - data access for fee payments
 */
@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByFeeId(Long feeId);
}
