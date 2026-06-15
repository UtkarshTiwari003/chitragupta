package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enrollment DTOs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private Long subjectId;
    private String subjectName;
    private String status;
}

/**
 * Enrollment request
 */
class EnrollmentRequest {
    public Long studentId;
    public Long subjectId;
}
