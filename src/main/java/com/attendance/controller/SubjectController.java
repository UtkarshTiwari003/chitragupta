package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.model.Subject;
import com.attendance.repository.SubjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Subject controller - manages subjects
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class SubjectController {
    
    private final SubjectRepository subjectRepository;
    
    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }
    
    /**
     * Get all active subjects
     */
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<Subject>>> getActiveSubjects() {
        List<Subject> subjects = subjectRepository.findByIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }
    
    /**
     * Create subject (teacher only)
     */
    @PostMapping("/teacher/subjects")
    public ResponseEntity<ApiResponse<Subject>> createSubject(@RequestBody Subject subject) {
        subject.setIsActive(true);
        Subject saved = subjectRepository.save(subject);
        return ResponseEntity.ok(ApiResponse.success(201, "Subject created", saved));
    }
}
