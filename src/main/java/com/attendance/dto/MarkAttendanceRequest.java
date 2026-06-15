package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Mark attendance request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkAttendanceRequest {
    private Long studentId;
    private LocalDate attendanceDate;
    private String status;
}

/**
 * Attendance response
 */
@Data
class AttendanceDTO {
    public Long id;
    public Long studentId;
    public LocalDate attendanceDate;
    public String status;
}
