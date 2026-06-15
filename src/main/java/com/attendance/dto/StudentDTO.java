package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Student response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {
    private Long id;
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String rollNumber;
    private Integer standard;
    private String batchName;
    private String googleSheetsLink;
    private String enrollmentStatus;
    private List<EnrollmentDTO> enrollments;
}
