package com.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Student profile linked to User entity.
 * Mixed standards, mixed batches, mixed subjects.
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    @Column(nullable = false, length = 20)
    private String rollNumber;
    
    @Column(nullable = false)
    private Integer standard;
    
    @Column(nullable = false, length = 50)
    private String batchName;
    
    @Column(length = 500)
    private String googleSheetsLink;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EnrollmentStatus enrollmentStatus = EnrollmentStatus.ACTIVE;
    
    public enum EnrollmentStatus {
        ACTIVE, INACTIVE, COMPLETED, DROPPED
    }
}
