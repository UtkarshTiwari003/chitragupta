package com.attendance.service;

import com.attendance.dto.MonthlyReportDTO;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.model.Report;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.FeeRepository;
import com.attendance.repository.ReportRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Report service - generates monthly reports with gamification
 */
@Slf4j
@Service
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeRepository feeRepository;
    private final UserRepository userRepository;
    
    public ReportService(ReportRepository reportRepository,
                        StudentRepository studentRepository,
                        AttendanceRepository attendanceRepository,
                        FeeRepository feeRepository,
                        UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.feeRepository = feeRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Get student's monthly report
     */
    @Transactional(readOnly = true)
    public MonthlyReportDTO getMonthlyReport(Long studentId, String yearMonth) {
        Report report = reportRepository.findByStudentIdAndYearMonth(studentId, yearMonth)
            .orElseThrow(() -> new ResourceNotFoundException("Report", "studentId", studentId));
        
        String starDescription = getStarDescription(report.getStarRating());
        
        return MonthlyReportDTO.builder()
            .studentId(report.getStudentId())
            .yearMonth(report.getYearMonth())
            .attendancePercentage(report.getAttendancePercentage())
            .averageScore(report.getAverageScore())
            .outstandingFeesCount(report.getOutstandingFeesCount())
            .starRating(report.getStarRating())
            .performanceRank(report.getPerformanceRank())
            .starDescription(starDescription)
            .build();
    }
    
    /**
     * Scheduled task: Generate monthly reports at 11 PM on last day of month
     */
    @Scheduled(cron = "0 23 L * * ?")
    @Transactional
    public void generateMonthlyReports() {
        YearMonth yearMonth = YearMonth.now().minusMonths(1);
        String yearMonthStr = yearMonth.toString();
        
        List<Long> studentIds = studentRepository.findAll()
            .stream()
            .map(s -> s.getId())
            .toList();
        
        for (Long studentId : studentIds) {
            generateReportForStudent(studentId, yearMonthStr);
        }
        
        log.info("Monthly reports generated for {} students in {}", studentIds.size(), yearMonthStr);
    }
    
    private void generateReportForStudent(Long studentId, String yearMonth) {
        LocalDate from = YearMonth.parse(yearMonth).atDay(1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        
        // Calculate attendance percentage
        Long presentDays = attendanceRepository.countPresentDays(studentId, from, to);
        Long totalDays = attendanceRepository.countTotalDays(studentId, from, to);
        BigDecimal attendancePercentage = totalDays > 0
            ? BigDecimal.valueOf(presentDays * 100).divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // Get outstanding fees count
        Integer outstandingFeesCount = feeRepository.countOutstandingFees(studentId);
        
        // Calculate star rating (gamification)
        Integer starRating = calculateStarRating(attendancePercentage, outstandingFeesCount);
        
        Report report = Report.builder()
            .studentId(studentId)
            .yearMonth(yearMonth)
            .attendancePercentage(attendancePercentage)
            .averageScore(BigDecimal.ZERO) // Would be fetched from Google Sheets in full implementation
            .outstandingFeesCount(outstandingFeesCount)
            .starRating(starRating)
            .performanceRank(0) // Would be calculated based on all students
            .build();
        
        reportRepository.save(report);
        log.debug("Report generated for student {} in {}", studentId, yearMonth);
    }
    
    /**
     * Calculate star rating based on criteria
     */
    private Integer calculateStarRating(BigDecimal attendance, Integer outstandingFees) {
        // 5-star: ≥95% attendance AND 0 outstanding fees
        if (attendance.compareTo(BigDecimal.valueOf(95)) >= 0 && outstandingFees == 0) {
            return 5;
        }
        
        // 4-star: ≥90% attendance AND ≤1 outstanding fees
        if (attendance.compareTo(BigDecimal.valueOf(90)) >= 0 && outstandingFees <= 1) {
            return 4;
        }
        
        // 3-star: ≥75% attendance AND ≤2 outstanding fees
        if (attendance.compareTo(BigDecimal.valueOf(75)) >= 0 && outstandingFees <= 2) {
            return 3;
        }
        
        // 2-star: ≥75% attendance
        if (attendance.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return 2;
        }
        
        // 1-star: below thresholds
        return 1;
    }
    
    private String getStarDescription(Integer stars) {
        return switch (stars) {
            case 5 -> "⭐⭐⭐⭐⭐ Excellent! Outstanding performance!";
            case 4 -> "⭐⭐⭐⭐ Great! Keep up the good work!";
            case 3 -> "⭐⭐⭐ Good! Room for improvement!";
            case 2 -> "⭐⭐ Fair! Need to improve attendance or fee status!";
            default -> "⭐ Poor! Urgent action needed!";
        };
    }
}
