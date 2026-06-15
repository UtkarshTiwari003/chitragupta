package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Monthly report response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportDTO {
    private Long studentId;
    private String studentName;
    private String yearMonth;
    private BigDecimal attendancePercentage;
    private BigDecimal averageScore;
    private Integer outstandingFeesCount;
    private Integer starRating;
    private Integer performanceRank;
    private String starDescription;
}
