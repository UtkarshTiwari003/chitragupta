package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.MonthlyReportDTO;
import com.attendance.service.ReportService;
import com.attendance.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

/**
 * Report controller - retrieves monthly reports
 */
@Slf4j
@RestController
@RequestMapping
public class ReportController {
    
    private final ReportService reportService;
    private final StudentService studentService;
    
    public ReportController(ReportService reportService, StudentService studentService) {
        this.reportService = reportService;
        this.studentService = studentService;
    }
    
    /**
     * Get student's monthly report
     */
    @GetMapping("/student/reports/monthly")
    public ResponseEntity<ApiResponse<MonthlyReportDTO>> getMyMonthlyReport(
            @RequestParam(required = false) String yearMonth,
            @RequestAttribute("userId") Long userId) {
        
        String month = yearMonth != null ? yearMonth : YearMonth.now().minusMonths(1).toString();
        Long studentId = studentService.getStudentIdForUserId(userId);
        MonthlyReportDTO report = reportService.getMonthlyReport(studentId, month);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
