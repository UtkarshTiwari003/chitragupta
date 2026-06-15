package com.attendance.service;

import com.attendance.dto.EnrollmentDTO;
import com.attendance.dto.StudentDTO;
import com.attendance.dto.StudentRequest;
import com.attendance.dto.UserDTO;
import com.attendance.exception.DuplicateEntityException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.ValidationException;
import com.attendance.model.*;
import com.attendance.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Student service - manages student profiles and enrollments
 */
@Slf4j
@Service
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubjectRepository subjectRepository;
    private final AuthenticationService authenticationService;
    
    public StudentService(StudentRepository studentRepository,
                        UserRepository userRepository,
                        EnrollmentRepository enrollmentRepository,
                        SubjectRepository subjectRepository,
                        AuthenticationService authenticationService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.subjectRepository = subjectRepository;
        this.authenticationService = authenticationService;
    }
    
    /**
     * Add new student (Teacher only)
     */
    @Transactional
    public StudentDTO addStudent(StudentRequest request) {
        UserDTO userDTO = authenticationService.registerUser(
            request.getEmail(),
            request.getName(),
            request.getPhone(),
            request.getPassword(),
            User.UserRole.STUDENT
        );
        
        Student student = Student.builder()
            .userId(userDTO.getId())
            .rollNumber(request.getRollNumber())
            .standard(request.getStandard())
            .batchName(request.getBatchName())
            .googleSheetsLink(request.getGoogleSheetsLink())
            .enrollmentStatus(Student.EnrollmentStatus.ACTIVE)
            .build();
        
        Student saved = studentRepository.save(student);
        log.info("Student added: {} (ID: {})", request.getEmail(), saved.getId());
        
        return mapToDTO(saved, userDTO);
    }
    
    /**
     * Update student profile
     */
    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentRequest request) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        
        if (request.getStandard() != null) {
            student.setStandard(request.getStandard());
        }
        if (request.getBatchName() != null) {
            student.setBatchName(request.getBatchName());
        }
        if (request.getGoogleSheetsLink() != null) {
            student.setGoogleSheetsLink(request.getGoogleSheetsLink());
        }
        
        Student updated = studentRepository.save(student);
        User user = userRepository.findById(student.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", student.getUserId()));
        
        log.info("Student updated: {} (ID: {})", user.getEmail(), studentId);
        return mapToDTO(updated, UserDTO.from(user));
    }
    
    /**
     * Get single student
     */
    @Transactional(readOnly = true)
    public StudentDTO getStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        
        User user = userRepository.findById(student.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", student.getUserId()));
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        StudentDTO dto = mapToDTO(student, UserDTO.from(user));
        dto.setEnrollments(enrollments.stream().map(e -> {
            Subject subject = subjectRepository.findById(e.getSubjectId()).orElse(null);
            return EnrollmentDTO.builder()
                .id(e.getId())
                .studentId(e.getStudentId())
                .subjectId(e.getSubjectId())
                .subjectName(subject != null ? subject.getName() : "Unknown")
                .status(e.getStatus().name())
                .build();
        }).collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * Get all students
     */
    @Transactional(readOnly = true)
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAll(pageable);
        List<StudentDTO> dtos = students.getContent().stream()
            .map(student -> {
                User user = userRepository.findById(student.getUserId()).orElse(null);
                return mapToDTO(student, user != null ? UserDTO.from(user) : null);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, students.getTotalElements());
    }

    /**
     * Resolve the authenticated user's Student profile id for student-owned endpoints.
     */
    @Transactional(readOnly = true)
    public Long getStudentIdForUserId(Long userId) {
        return studentRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId))
            .getId();
    }

    /**
     * Get the external test/score link configured for the authenticated student.
     */
    @Transactional(readOnly = true)
    public String getTestLinkForUserId(Long userId) {
        return studentRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId))
            .getGoogleSheetsLink();
    }
    
    /**
     * Enroll student in subject
     */
    @Transactional
    public EnrollmentDTO enrollStudent(Long studentId, Long subjectId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));
        
        if (!subject.getIsActive()) {
            throw new ValidationException("Subject is not active for enrollment");
        }
        
        if (enrollmentRepository.findByStudentIdAndSubjectId(studentId, subjectId).isPresent()) {
            throw new DuplicateEntityException("Student already enrolled in this subject");
        }
        
        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .subjectId(subjectId)
            .status(Enrollment.Status.ACTIVE)
            .build();
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Student {} enrolled in subject {}", studentId, subjectId);
        
        return EnrollmentDTO.builder()
            .id(saved.getId())
            .studentId(saved.getStudentId())
            .subjectId(saved.getSubjectId())
            .subjectName(subject.getName())
            .status(saved.getStatus().name())
            .build();
    }
    
    private StudentDTO mapToDTO(Student student, UserDTO userDTO) {
        return StudentDTO.builder()
            .id(student.getId())
            .userId(student.getUserId())
            .email(userDTO != null ? userDTO.getEmail() : "N/A")
            .name(userDTO != null ? userDTO.getName() : "N/A")
            .phone(userDTO != null ? userDTO.getPhone() : "N/A")
            .rollNumber(student.getRollNumber())
            .standard(student.getStandard())
            .batchName(student.getBatchName())
            .googleSheetsLink(student.getGoogleSheetsLink())
            .enrollmentStatus(student.getEnrollmentStatus().name())
            .build();
    }
}
