package com.attendance.repository;

import com.attendance.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Attendance repository - data access for attendance records
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByStudentIdAndAttendanceDateBetween(Long studentId, LocalDate from, LocalDate to);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.status = 'PRESENT' AND a.attendanceDate BETWEEN :from AND :to")
    Long countPresentDays(@Param("studentId") Long studentId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND (a.status = 'PRESENT' OR a.status = 'ABSENT' OR a.status = 'LEAVE') " +
           "AND a.attendanceDate BETWEEN :from AND :to")
    Long countTotalDays(@Param("studentId") Long studentId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
