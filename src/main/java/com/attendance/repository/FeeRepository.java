package com.attendance.repository;

import com.attendance.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Fee repository - data access for fees
 */
@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentId(Long studentId);
    List<Fee> findByStatus(Fee.Status status);
    
    @Query("SELECT f FROM Fee f WHERE f.status != 'PAID' AND f.dueDate < :date")
    List<Fee> findOverdueFees(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(f) FROM Fee f WHERE f.studentId = :studentId AND f.status != 'PAID'")
    Integer countOutstandingFees(@Param("studentId") Long studentId);
}
