package com.attendance.repository;

import com.attendance.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Report repository - data access for monthly reports
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByStudentIdAndYearMonth(Long studentId, String yearMonth);
    List<Report> findByYearMonth(String yearMonth);
    List<Report> findByStudentId(Long studentId);
}
