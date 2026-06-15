package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.EnrollmentDTO;
import com.attendance.dto.StudentDTO;
import com.attendance.dto.StudentRequest;
import com.attendance.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Student controller - manages students
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class StudentController {
    
    private final StudentService studentService;
    
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    
    /**
     * Add new student
     */
    @PostMapping("/teacher/students")
    public ResponseEntity<ApiResponse<StudentDTO>> addStudent(@RequestBody StudentRequest request) {
        StudentDTO student = studentService.addStudent(request);
        return ResponseEntity.ok(ApiResponse.success(201, "Student added successfully", student));
    }
    
    /**
     * Update student
     */
    @PutMapping("/teacher/students/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentRequest request) {
        StudentDTO student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(student));
    }
    
    /**
     * Get single student
     */
    @GetMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudent(@PathVariable Long id) {
        StudentDTO student = studentService.getStudent(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }
    
    /**
     * Get all students
     */
    @GetMapping("/teacher/students")
    public ResponseEntity<ApiResponse<Page<StudentDTO>>> getAllStudents(Pageable pageable) {
        Page<StudentDTO> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(ApiResponse.success(students));
    }
    
    /**
     * Enroll student in subject
     */
    @PostMapping("/teacher/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> enrollStudent(
            @RequestParam Long studentId,
            @RequestParam Long subjectId) {
        EnrollmentDTO enrollment = studentService.enrollStudent(studentId, subjectId);
        return ResponseEntity.ok(ApiResponse.success(201, "Enrollment successful", enrollment));
    }
}
