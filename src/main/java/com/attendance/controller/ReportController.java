package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.MonthlyReportDTO;
import com.attendance.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

/**
 * Report controller - retrieves monthly reports
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ReportController {
    
    private final ReportService reportService;
    
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    /**
     * Get student's monthly report
     */
    @GetMapping("/student/reports/monthly")
    public ResponseEntity<ApiResponse<MonthlyReportDTO>> getMyMonthlyReport(
            @RequestParam(required = false) String yearMonth,
            @RequestAttribute("userId") Long userId) {
        
        String month = yearMonth != null ? yearMonth : YearMonth.now().minusMonths(1).toString();
        MonthlyReportDTO report = reportService.getMonthlyReport(userId, month);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
