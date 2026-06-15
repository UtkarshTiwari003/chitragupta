package com.attendance.repository;

import com.attendance.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Enrollment repository - data access for enrollments
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findBySubjectId(Long subjectId);
    Optional<Enrollment> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
}
