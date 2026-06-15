package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Student request DTO for create/update
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {
    private String email;
    private String name;
    private String phone;
    private String password;
    private String rollNumber;
    private Integer standard;
    private String batchName;
    private String googleSheetsLink;
}
