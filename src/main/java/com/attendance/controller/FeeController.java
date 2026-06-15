package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.RecordPaymentRequest;
import com.attendance.model.Fee;
import com.attendance.service.FeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fee controller - manages fees and payments
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class FeeController {
    
    private final FeeService feeService;
    
    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }
    
    /**
     * Record a fee payment
     */
    @PostMapping("/teacher/fees/payment")
    public ResponseEntity<ApiResponse<Map<String, String>>> recordPayment(
            @RequestBody RecordPaymentRequest request) {
        feeService.recordPayment(
            request.getFeeId(),
            request.getAmountPaid(),
            request.getPaymentMethod(),
            request.getTransactionId()
        );
        
        Map<String, String> result = new HashMap<>();
        result.put("message", "Payment recorded successfully");
        result.put("feeId", request.getFeeId().toString());
        result.put("amount", request.getAmountPaid().toString());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get student's fee status
     */
    @GetMapping("/student/fees/my-status")
    public ResponseEntity<ApiResponse<List<Fee>>> getMyFeeStatus(
            @RequestAttribute("userId") Long userId) {
        List<Fee> fees = feeService.getStudentFees(userId);
        return ResponseEntity.ok(ApiResponse.success(fees));
    }
    
    /**
     * Get all outstanding fees (teacher view)
     */
    @GetMapping("/teacher/fees/outstanding")
    public ResponseEntity<ApiResponse<List<Fee>>> getOutstandingFees() {
        // This would return all outstanding fees across all students
        // Implementation depends on requirements
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
