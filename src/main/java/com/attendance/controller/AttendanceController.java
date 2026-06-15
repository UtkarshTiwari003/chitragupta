package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.MarkAttendanceRequest;
import com.attendance.model.Attendance;
import com.attendance.service.AttendanceService;
import com.attendance.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attendance controller - marks and retrieves attendance
 */
@Slf4j
@RestController
@RequestMapping
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    
    public AttendanceController(AttendanceService attendanceService, StudentService studentService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
    }
    
    /**
     * Mark attendance
     */
    @PostMapping("/teacher/attendance/mark")
    public ResponseEntity<ApiResponse<Map<String, String>>> markAttendance(
            @RequestBody MarkAttendanceRequest request) {
        attendanceService.markAttendance(
            request.getStudentId(),
            request.getAttendanceDate(),
            request.getStatus()
        );
        
        Map<String, String> result = new HashMap<>();
        result.put("message", "Attendance marked successfully");
        result.put("studentId", request.getStudentId().toString());
        result.put("date", request.getAttendanceDate().toString());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get student's attendance records
     */
    @GetMapping("/student/attendance/my-records")
    public ResponseEntity<ApiResponse<List<Attendance>>> getMyAttendance(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestAttribute("userId") Long userId) {
        
        LocalDate fromDate = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = to != null ? to : LocalDate.now();
        
        Long studentId = studentService.getStudentIdForUserId(userId);
        List<Attendance> records = attendanceService.getAttendanceRecords(studentId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
    
    /**
     * Get student's attendance percentage
     */
    @GetMapping("/student/attendance/percentage")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendancePercentage(
            @RequestAttribute("userId") Long userId) {
        
        Map<String, Object> result = new HashMap<>();
        Long studentId = studentService.getStudentIdForUserId(userId);
        result.put("percentage", attendanceService.calculateAttendancePercentage(studentId));
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
