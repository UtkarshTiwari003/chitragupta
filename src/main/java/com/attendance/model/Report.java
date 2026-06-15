package com.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Monthly report - aggregated data per student per month
 */
@Entity
@Table(name = "reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "year_month"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long studentId;
    
    @Column(nullable = false, length = 20)
    private String yearMonth;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal attendancePercentage;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal averageScore;
    
    @Column(nullable = false)
    private Integer outstandingFeesCount;
    
    @Column(nullable = false)
    private Integer starRating;
    
    @Column(nullable = false)
    private Integer performanceRank;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;
}
