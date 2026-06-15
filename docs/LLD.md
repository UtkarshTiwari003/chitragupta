# Low-Level Design (LLD) - Attendance Tracking System

## 1. System Architecture Overview

### Layered Architecture

```
┌───────────────────────────────────────────────────┐
│           PRESENTATION LAYER                      │
│   (REST Controllers, Request/Response DTOs)       │
├───────────────────────────────────────────────────┤
│           APPLICATION LAYER                       │
│   (Business Logic Services, Validators)           │
├───────────────────────────────────────────────────┤
│           DOMAIN LAYER                            │
│   (Entities, Domain Models, Repositories)         │
├───────────────────────────────────────────────────┤
│           PERSISTENCE LAYER                       │
│   (JPA/Hibernate, Database Queries)               │
├───────────────────────────────────────────────────┤
│           INFRASTRUCTURE LAYER                    │
│   (Email Service, Caching, Logging)               │
└───────────────────────────────────────────────────┘
```

---

## 2. Design Patterns Applied

### 2.1 Singleton Pattern
**Usage:** Spring Components (Services, Repositories)
- Spring IoC container ensures single instance across application
- Reduces memory overhead and ensures thread-safe operations

```java
@Service
public class StudentService {
    // Spring ensures singleton instance
}
```

### 2.2 Factory Pattern
**Usage:** Entity/DTO creation, Service instantiation
- ServiceFactory for creating appropriate notification service
- ResponseFactory for consistent API responses

```java
@Component
public class NotificationFactory {
    public NotificationService getNotificationService(NotificationType type) {
        switch(type) {
            case EMAIL: return new EmailNotificationService();
            case SMS: return new SMSNotificationService();
        }
    }
}
```

### 2.3 Builder Pattern
**Usage:** Complex object creation (Report, Query filters)
- Fluent API for constructing objects
- Better readability and immutability

```java
public class ReportBuilder {
    private ReportDTO report = new ReportDTO();
    
    public ReportBuilder withStudentId(Long id) { 
        report.setStudentId(id); 
        return this; 
    }
    public ReportDTO build() { return report; }
}
```

### 2.4 Strategy Pattern
**Usage:** Notification delivery, Report generation
- Different strategies for different notification types
- Enables switching between implementations at runtime

```java
public interface NotificationStrategy {
    void sendNotification(NotificationDTO notification);
}

@Service
public class EmailNotificationStrategy implements NotificationStrategy {
    @Override
    public void sendNotification(NotificationDTO notification) {
        // Email specific implementation
    }
}
```

### 2.5 Repository Pattern
**Usage:** Data access abstraction
- Decouples business logic from database implementation
- Easy to test with mock repositories

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    List<Student> findByEnrollmentStatus(EnrollmentStatus status);
}
```

### 2.6 Dependency Injection Pattern
**Usage:** Constructor injection for all dependencies
- Promotes loose coupling and testability
- Spring framework native support

```java
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    
    public AttendanceService(AttendanceRepository repo1, 
                            StudentRepository repo2) {
        this.attendanceRepository = repo1;
        this.studentRepository = repo2;
    }
}
```

### 2.7 Decorator Pattern
**Usage:** Request/Response interceptors, Logging
- Adds behavior to requests without modifying original

```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // Log request details
        return true;
    }
}
```

### 2.8 Observer Pattern
**Usage:** Event-driven notifications
- Spring ApplicationEvent for publishing domain events
- Multiple subscribers for same event

```java
@Component
public class AttendanceEventListener {
    @EventListener
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        // Handle attendance marked event
    }
}
```

---

## 3. Class Hierarchy & Structure

### 3.1 Entity Classes

```
Entity (Base)
├── User
│   ├── Student
│   └── Teacher
├── Subject
├── Enrollment
├── Attendance
├── Fee
├── FeePayment
├── Report
└── Notification
```

### 3.2 Service Classes

```
BaseService (Abstract)
├── StudentService
├── TeacherService
├── AttendanceService
├── FeeService
├── ReportService
└── NotificationService
```

### 3.3 Repository Classes

```
BaseRepository<T, ID> (Interface)
├── StudentRepository
├── TeacherRepository
├── AttendanceRepository
├── FeeRepository
├── SubjectRepository
├── EnrollmentRepository
├── ReportRepository
└── NotificationRepository
```

---

## 4. Detailed Component Design

### 4.1 Authentication & Authorization Service

**Responsibility:** JWT token generation, validation, user authentication

```java
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Authenticate user with credentials and generate JWT token.
     * 
     * @param email User email
     * @param password User password
     * @return JwtResponse with access token and refresh token
     * @throws AuthenticationException if credentials invalid
     */
    public JwtResponse authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
            
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        
        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        return new JwtResponse(accessToken, refreshToken);
    }
}
```

**Key Methods:**
- `authenticate(email, password)` → JwtResponse
- `validateToken(token)` → boolean
- `getUserFromToken(token)` → User
- `refreshAccessToken(refreshToken)` → JwtResponse

---

### 4.2 Student Service

**Responsibility:** Student lifecycle management, profile updates, enrollment

```java
@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Create new student and publish StudentCreatedEvent.
     * Validates enrollment details and board/standard mapping.
     * 
     * @param createStudentDTO Student creation request
     * @return Created StudentDTO
     * @throws InvalidEnrollmentException if enrollment invalid
     */
    public StudentDTO createStudent(CreateStudentDTO createStudentDTO) {
        Student student = new Student();
        student.setFirstName(createStudentDTO.getFirstName());
        student.setEmail(createStudentDTO.getEmail());
        
        Student savedStudent = studentRepository.save(student);
        eventPublisher.publishEvent(new StudentCreatedEvent(savedStudent));
        
        return mapToDTO(savedStudent);
    }
    
    /**
     * Get student attendance percentage for date range.
     * Calculation: (Present Days / Total School Days) * 100
     * 
     * @param studentId Student identifier
     * @param startDate Range start
     * @param endDate Range end
     * @return Attendance percentage (0-100)
     */
    public Double getAttendancePercentage(Long studentId, 
                                          LocalDate startDate, 
                                          LocalDate endDate) {
        List<Attendance> records = attendanceRepository
            .findByStudentAndDateBetween(studentId, startDate, endDate);
        
        long presentDays = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
            .count();
        
        return (presentDays * 100.0) / records.size();
    }
    
    /**
     * Enroll student in subject with board and standard.
     * 
     * @param studentId Student ID
     * @param enrollmentDTO Enrollment details
     * @return Saved EnrollmentDTO
     */
    public EnrollmentDTO enrollInSubject(Long studentId, 
                                         EnrollmentDTO enrollmentDTO) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSubject(enrollmentDTO.getSubject());
        enrollment.setBoard(enrollmentDTO.getBoard());
        enrollment.setStandard(enrollmentDTO.getStandard());
        
        return mapToDTO(enrollmentRepository.save(enrollment));
    }
}
```

**Key Methods:**
- `createStudent(createStudentDTO)` → StudentDTO
- `getStudentById(id)` → StudentDTO
- `updateStudent(id, updateDTO)` → StudentDTO
- `getAttendancePercentage(studentId, startDate, endDate)` → Double
- `enrollInSubject(studentId, enrollmentDTO)` → EnrollmentDTO
- `getEnrolledSubjects(studentId)` → List<SubjectDTO>

---

### 4.3 Attendance Service

**Responsibility:** Mark attendance, generate reports, calculate statistics

```java
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Mark attendance for student on given date.
     * Validates student exists and date is not in future.
     * Publishes AttendanceMarkedEvent for event subscribers.
     * 
     * @param markAttendanceDTO Attendance details
     * @return Saved AttendanceDTO
     * @throws StudentNotFoundException if student doesn't exist
     * @throws InvalidAttendanceDateException if future date
     */
    @Transactional
    public AttendanceDTO markAttendance(MarkAttendanceDTO markAttendanceDTO) {
        Student student = studentRepository
            .findById(markAttendanceDTO.getStudentId())
            .orElseThrow(() -> new StudentNotFoundException(
                markAttendanceDTO.getStudentId()));
        
        if (markAttendanceDTO.getDate().isAfter(LocalDate.now())) {
            throw new InvalidAttendanceDateException(
                "Future dates not allowed");
        }
        
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(markAttendanceDTO.getDate());
        attendance.setStatus(markAttendanceDTO.getStatus());
        
        Attendance saved = attendanceRepository.save(attendance);
        
        // Publish event for subscribers (e.g., low attendance notification)
        eventPublisher.publishEvent(
            new AttendanceMarkedEvent(saved));
        
        // Check if attendance is below threshold
        checkAttendanceThreshold(student);
        
        return mapToDTO(saved);
    }
    
    /**
     * Generate attendance report for student in date range.
     * 
     * @param studentId Student identifier
     * @param startDate Range start
     * @param endDate Range end
     * @return AttendanceReportDTO with statistics
     */
    public AttendanceReportDTO generateAttendanceReport(Long studentId,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {
        List<Attendance> records = attendanceRepository
            .findByStudentAndDateBetween(studentId, startDate, endDate);
        
        long presentDays = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
            .count();
        long absentDays = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
            .count();
        long leaveDays = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.LEAVE)
            .count();
        
        double percentage = (presentDays * 100.0) / records.size();
        
        AttendanceReportDTO report = new AttendanceReportDTO();
        report.setStudentId(studentId);
        report.setPresentDays(presentDays);
        report.setAbsentDays(absentDays);
        report.setLeaveDays(leaveDays);
        report.setAttendancePercentage(percentage);
        
        return report;
    }
    
    /**
     * Send notification if attendance falls below threshold.
     * Threshold: 75%
     * 
     * @param student Student entity
     */
    private void checkAttendanceThreshold(Student student) {
        LocalDate monthStart = LocalDate.now()
            .withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now()
            .withDayOfMonth(LocalDate.now()
                .lengthOfMonth());
        
        Double percentage = getMonthlyAttendancePercentage(
            student.getId(), monthStart, monthEnd);
        
        if (percentage < 75.0) {
            NotificationDTO notification = new NotificationDTO();
            notification.setStudentId(student.getId());
            notification.setType(NotificationType.LOW_ATTENDANCE);
            notification.setMessage(String.format(
                "Your attendance is %.1f%%. Please improve!", 
                percentage));
            
            notificationService.sendNotification(notification);
        }
    }
}
```

**Key Methods:**
- `markAttendance(markAttendanceDTO)` → AttendanceDTO
- `generateAttendanceReport(studentId, startDate, endDate)` → AttendanceReportDTO
- `getMonthlyAttendancePercentage(studentId, month)` → Double
- `getAttendanceByDateRange(studentId, startDate, endDate)` → List<AttendanceDTO>

---

### 4.4 Fee Service

**Responsibility:** Fee tracking, payment processing, reminder generation

```java
@Service
@RequiredArgsConstructor
public class FeeService {
    private final FeeRepository feeRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    
    /**
     * Record fee payment and mark fee as paid if fully settled.
     * Validates payment amount and date.
     * 
     * @param paymentDTO Payment details
     * @return FeePaymentDTO
     * @throws InvalidPaymentException if amount invalid
     */
    @Transactional
    public FeePaymentDTO recordPayment(RecordPaymentDTO paymentDTO) {
        Fee fee = feeRepository.findById(paymentDTO.getFeeId())
            .orElseThrow(() -> new FeeNotFoundException(
                paymentDTO.getFeeId()));
        
        if (paymentDTO.getAmount() <= 0) {
            throw new InvalidPaymentException(
                "Payment amount must be positive");
        }
        
        FeePayment payment = new FeePayment();
        payment.setFee(fee);
        payment.setAmountPaid(paymentDTO.getAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        
        FeePayment savedPayment = feePaymentRepository.save(payment);
        
        // Update fee outstanding amount
        fee.setOutstandingAmount(
            fee.getOutstandingAmount() - paymentDTO.getAmount());
        
        if (fee.getOutstandingAmount() <= 0) {
            fee.setStatus(FeeStatus.PAID);
        }
        
        feeRepository.save(fee);
        
        return mapToDTO(savedPayment);
    }
    
    /**
     * Check for outstanding fees and send reminders.
     * Sends reminder if fee is 5+ days overdue.
     * Should be run daily via scheduled task.
     */
    @Scheduled(cron = "0 0 8 * * ?")  // 8 AM daily
    public void sendOutstandingFeeReminders() {
        List<Fee> outstandingFees = feeRepository
            .findByStatusAndDueDateBefore(
                FeeStatus.OUTSTANDING,
                LocalDate.now().minusDays(5));
        
        for (Fee fee : outstandingFees) {
            Student student = fee.getStudent();
            NotificationDTO notification = new NotificationDTO();
            notification.setStudentId(student.getId());
            notification.setType(NotificationType.FEE_REMINDER);
            notification.setMessage(String.format(
                "Outstanding fee of Rs. %,.2f due since %s. " +
                "Please pay within 2 days.",
                fee.getOutstandingAmount(),
                fee.getDueDate()));
            
            notificationService.sendNotification(notification);
        }
    }
    
    /**
     * Get outstanding fees for student.
     * 
     * @param studentId Student ID
     * @return List of outstanding FeeDTO
     */
    public List<FeeDTO> getOutstandingFees(Long studentId) {
        return feeRepository
            .findByStudentAndStatus(studentId, FeeStatus.OUTSTANDING)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
}
```

**Key Methods:**
- `recordPayment(paymentDTO)` → FeePaymentDTO
- `sendOutstandingFeeReminders()` → void
- `getOutstandingFees(studentId)` → List<FeeDTO>
- `getFeesForStudent(studentId)` → List<FeeDTO>
- `createFeeStructure(createFeeDTO)` → FeeDTO

---

### 4.5 Report Service

**Responsibility:** Generate monthly reports, performance analytics

```java
@Service
@RequiredArgsConstructor
public class ReportService {
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeRepository feeRepository;
    private final NotificationService notificationService;
    
    /**
     * Generate comprehensive monthly report for student.
     * Includes: Attendance, test scores, fee status, performance rank.
     * 
     * @param studentId Student ID
     * @param month Report month
     * @return MonthlyReportDTO
     */
    public MonthlyReportDTO generateMonthlyReport(Long studentId, 
                                                   YearMonth month) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        
        // Calculate attendance
        Double attendancePercentage = 
            calculateAttendancePercentage(studentId, monthStart, monthEnd);
        
        // Get test scores from Google Doc link (mock implementation)
        List<TestScoreDTO> testScores = 
            fetchTestScoresFromGoogleDoc(student.getGoogleDocLink());
        
        // Calculate average score
        Double averageScore = testScores.stream()
            .mapToDouble(TestScoreDTO::getScore)
            .average()
            .orElse(0.0);
        
        // Get fee status
        Long outstandingFees = feeRepository
            .findByStudentAndStatus(studentId, FeeStatus.OUTSTANDING)
            .size();
        
        // Determine star rating
        Integer starRating = calculateStarRating(
            attendancePercentage, averageScore, outstandingFees);
        
        // Get performance rank
        Integer performanceRank = getPerformanceRank(
            studentId, averageScore);
        
        MonthlyReportDTO report = new MonthlyReportDTO();
        report.setStudentId(studentId);
        report.setMonth(month);
        report.setAttendancePercentage(attendancePercentage);
        report.setTestScores(testScores);
        report.setAverageScore(averageScore);
        report.setOutstandingFees(outstandingFees);
        report.setStarRating(starRating);
        report.setPerformanceRank(performanceRank);
        
        return report;
    }
    
    /**
     * Calculate star rating based on performance metrics.
     * 5 Stars: Attendance ≥ 95%, Score ≥ 90%, No outstanding fees
     * 4 Stars: Attendance ≥ 90%, Score ≥ 80%, Max 1 outstanding fee
     * 3 Stars: Attendance ≥ 75%, Score ≥ 70%, Max 2 outstanding fees
     * 2 Stars: Attendance ≥ 75%, Score ≥ 60%
     * 1 Star: Below 75% attendance or 60% score
     * 
     * @param attendance Attendance percentage
     * @param score Average test score
     * @param outstandingFees Count of outstanding fees
     * @return Star rating (1-5)
     */
    private Integer calculateStarRating(Double attendance, 
                                        Double score, 
                                        Long outstandingFees) {
        if (attendance >= 95 && score >= 90 && outstandingFees == 0) {
            return 5;
        } else if (attendance >= 90 && score >= 80 && 
                   outstandingFees <= 1) {
            return 4;
        } else if (attendance >= 75 && score >= 70 && 
                   outstandingFees <= 2) {
            return 3;
        } else if (attendance >= 75 && score >= 60) {
            return 2;
        } else {
            return 1;
        }
    }
    
    /**
     * Send monthly report to student via email.
     * Called via scheduled task on last day of month.
     * 
     * @param studentId Student ID
     */
    @Scheduled(cron = "0 0 23 L * ?")  // 11 PM on last day of month
    public void sendMonthlyReport(Long studentId) {
        MonthlyReportDTO report = generateMonthlyReport(
            studentId, YearMonth.now());
        
        NotificationDTO notification = new NotificationDTO();
        notification.setStudentId(studentId);
        notification.setType(NotificationType.MONTHLY_REPORT);
        notification.setMessage(formatReportAsMessage(report));
        
        notificationService.sendNotification(notification);
    }
}
```

**Key Methods:**
- `generateMonthlyReport(studentId, month)` → MonthlyReportDTO
- `sendMonthlyReport(studentId)` → void
- `calculateStarRating(attendance, score, fees)` → Integer
- `getPerformanceRank(studentId, score)` → Integer
- `getHighScorers(month)` → List<StudentDTO>

---

### 4.6 Notification Service

**Responsibility:** Email dispatch, notification logging, retry logic

```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EmailSender emailSender;
    private final NotificationRepository notificationRepository;
    private final NotificationStrategyFactory strategyFactory;
    private static final Logger logger = 
        LoggerFactory.getLogger(NotificationService.class);
    
    /**
     * Send notification using appropriate strategy.
     * Logs notification and retries on failure (max 3 attempts).
     * 
     * @param notificationDTO Notification to send
     */
    @Transactional
    public void sendNotification(NotificationDTO notificationDTO) {
        NotificationStrategy strategy = 
            strategyFactory.getStrategy(notificationDTO.getType());
        
        Notification notification = new Notification();
        notification.setStudentId(notificationDTO.getStudentId());
        notification.setType(notificationDTO.getType());
        notification.setMessage(notificationDTO.getMessage());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setAttempts(0);
        
        try {
            strategy.send(notificationDTO);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            logger.info("Notification sent successfully to student: {}",
                       notificationDTO.getStudentId());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            logger.error("Failed to send notification to student: {}",
                        notificationDTO.getStudentId(), e);
            
            // Retry logic for failed notifications
            retryNotification(notification);
        }
        
        notificationRepository.save(notification);
    }
    
    /**
     * Retry failed notifications with exponential backoff.
     * Max 3 retry attempts with 5min, 15min, 60min delays.
     * 
     * @param notification Notification to retry
     */
    @Transactional
    public void retryNotification(Notification notification) {
        if (notification.getAttempts() < 3) {
            int delaySeconds = (int) Math.pow(5 * 60, 
                notification.getAttempts() + 1);
            
            notification.setAttempts(notification.getAttempts() + 1);
            notification.setRetryAt(
                LocalDateTime.now()
                    .plusSeconds(delaySeconds));
            
            notificationRepository.save(notification);
        }
    }
}

/**
 * Email notification implementation.
 * Uses SendGrid for reliable email delivery.
 */
@Component
public class EmailNotificationStrategy implements NotificationStrategy {
    private final SendGridEmailSender emailSender;
    
    @Override
    public void send(NotificationDTO notificationDTO) {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(notificationDTO.getStudentEmail());
        emailRequest.setSubject(getSubject(notificationDTO.getType()));
        emailRequest.setBody(notificationDTO.getMessage());
        
        emailSender.sendEmail(emailRequest);
    }
}
```

**Key Methods:**
- `sendNotification(notificationDTO)` → void
- `retryNotification(notification)` → void
- `getNotificationHistory(studentId)` → List<NotificationDTO>

---

## 5. Database Schema Details

### 5.1 User Entity

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    user_type ENUM('ADMIN', 'TEACHER', 'STUDENT', 'PARENT') NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_type ON users(user_type);
```

### 5.2 Student Entity

```sql
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    roll_number VARCHAR(50) UNIQUE NOT NULL,
    board ENUM('CBSE', 'ICSE', 'STATE_BOARD') NOT NULL,
    standard VARCHAR(10) NOT NULL,
    enrollment_status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED') 
        DEFAULT 'ACTIVE',
    google_doc_link VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_students_roll_number ON students(roll_number);
CREATE INDEX idx_students_board_standard ON students(board, standard);
```

### 5.3 Attendance Entity

```sql
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LEAVE') NOT NULL,
    remarks VARCHAR(500),
    marked_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(id),
    UNIQUE(student_id, attendance_date)
);

CREATE INDEX idx_attendance_student_date ON attendance(
    student_id, attendance_date);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
```

### 5.4 Fee Entity

```sql
CREATE TABLE fees (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    fee_type VARCHAR(50) NOT NULL,  -- 'TUITION', 'LIBRARY', etc.
    amount DECIMAL(10, 2) NOT NULL,
    outstanding_amount DECIMAL(10, 2) NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('OUTSTANDING', 'PARTIAL', 'PAID') 
        DEFAULT 'OUTSTANDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_fees_student_status ON fees(student_id, status);
CREATE INDEX idx_fees_due_date ON fees(due_date);
```

### 5.5 Notification Entity

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,  -- 'EMAIL', 'SMS'
    message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    attempts INTEGER DEFAULT 0,
    sent_at TIMESTAMP,
    retry_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_student_status ON notifications(
    student_id, status);
```

---

## 6. API Response Contracts

### 6.1 Standard Response Wrapper

```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;              // HTTP status code
    private String message;          // Success/error message
    private T data;                  // Response payload
    private long timestamp;          // Response timestamp
    private String path;             // Request path
    
    // Success response: status 200-299
    // Error response: status 400-599
}
```

### 6.2 Error Response Structure

```java
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;            // Error code: "STU_001"
    private String message;         // User-friendly message
    private String details;         // Technical details
    private List<FieldError> fieldErrors;  // Validation errors
}

@Data
public class FieldError {
    private String field;
    private String value;
    private String message;
}
```

---

## 7. Validation Strategy

### Input Validation
- **Controller Level:** @Valid annotation on request DTOs
- **Service Level:** Business rule validation
- **Database Level:** Constraints and triggers

### Validation Rules
```java
@Data
public class CreateStudentDTO {
    @NotEmpty(message = "First name is required")
    @Size(min = 2, max = 100)
    private String firstName;
    
    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotEmpty(message = "Board is required")
    @Pattern(regexp = "CBSE|ICSE|STATE_BOARD")
    private String board;
    
    @NotNull(message = "Standard is required")
    @Min(1)
    @Max(12)
    private Integer standard;
}
```

---

## 8. Error Handling Strategy

### Exception Hierarchy

```
ApplicationException (Base)
├── ResourceNotFoundException
│   ├── StudentNotFoundException
│   ├── TeacherNotFoundException
│   └── SubjectNotFoundException
├── ValidationException
│   ├── InvalidAttendanceDateException
│   ├── InvalidPaymentException
│   └── InvalidEnrollmentException
├── AuthenticationException
│   ├── InvalidCredentialsException
│   └── TokenExpiredException
├── AuthorizationException
│   └── AccessDeniedException
└── ExternalServiceException
    ├── EmailServiceException
    └── GoogleDocsFetchException
```

### Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException e) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("NOT_FOUND");
        error.setMessage(e.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException e) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("VALIDATION_ERROR");
        error.setMessage(e.getMessage());
        error.setFieldErrors(e.getFieldErrors());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
}
```

---

## 9. Transaction Management

### Transaction Boundaries

```java
// Service methods handling multiple operations
@Transactional
public StudentDTO createStudent(CreateStudentDTO dto) {
    // All operations succeed or all rollback
}

// Read-only optimization
@Transactional(readOnly = true)
public StudentDTO getStudent(Long id) {
    // Better performance for queries
}

// Custom propagation
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logAuditEvent(AuditEvent event) {
    // Logs independently even if parent rolls back
}
```

---

## 10. Security Implementation Details

### JWT Token Structure

```
Header: {"alg": "HS256", "typ": "JWT"}
Payload: {
  "sub": "user@example.com",
  "userId": 123,
  "roles": ["TEACHER"],
  "iat": 1623456789,
  "exp": 1623460389
}
Signature: HMACSHA256(header.payload, secret_key)
```

### CORS Configuration

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200")  // Angular dev server
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## 11. Logging Strategy

### Log Levels
- **ERROR:** System errors, exceptions, failed operations
- **WARN:** Unusual but recoverable situations
- **INFO:** Significant business events (login, attendance marked)
- **DEBUG:** Variable states, method entry/exit
- **TRACE:** Low-level details (rarely used)

### Structured Logging Example

```java
logger.info("Student attendance marked",
    new StructuredArgument[]{
        kv("studentId", 123),
        kv("date", "2026-06-15"),
        kv("status", "PRESENT"),
        kv("markedBy", 456),
        kv("duration", "145ms")
    });
```

---

## 12. Performance Considerations

### Query Optimization
- Use `@NamedQuery` for frequently used queries
- Leverage database indexes on search columns
- Implement pagination for large result sets
- Use projection (DTO) instead of full entities when possible

### Caching Strategy

```java
@Cacheable(value = "students", key = "#studentId")
public StudentDTO getStudent(Long studentId) {
    return studentRepository.findById(studentId);
}

@CacheEvict(value = "students", key = "#id")
public void updateStudent(Long id, UpdateStudentDTO dto) {
    // Update logic
}
```

### Batch Processing

```java
@Scheduled(cron = "0 0 1 * * ?")  // 1 AM daily
public void processDailyTasks() {
    List<Student> allStudents = studentRepository.findAll();
    
    // Batch send notifications
    allStudents.stream()
        .filter(s -> needsReminder(s))
        .forEach(s -> notificationService.sendNotification(s));
}
```

---

## 13. Testing Strategy

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;
    
    @InjectMocks
    private StudentService studentService;
    
    @Test
    void testCreateStudent_Success() {
        CreateStudentDTO dto = new CreateStudentDTO();
        dto.setFirstName("John");
        dto.setEmail("john@example.com");
        
        Student student = new Student();
        student.setId(1L);
        
        when(studentRepository.save(any(Student.class)))
            .thenReturn(student);
        
        StudentDTO result = studentService.createStudent(dto);
        
        assertEquals(1L, result.getId());
        verify(studentRepository, times(1)).save(any());
    }
}
```

### Integration Testing

```java
@SpringBootTest
@ActiveProfiles("test")
class StudentServiceIntegrationTest {
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @Transactional
    void testCreateAndRetrieveStudent() {
        CreateStudentDTO dto = new CreateStudentDTO();
        // ... set properties
        
        StudentDTO created = studentService.createStudent(dto);
        StudentDTO retrieved = studentService.getStudent(created.getId());
        
        assertEquals(created.getEmail(), retrieved.getEmail());
    }
}
```

---

## 14. Future Extensibility

### Plugin Architecture for Notifications

```java
// Easy to add SMS, Push notifications later
public interface NotificationStrategy {
    void send(NotificationDTO notification);
}

// New SMS implementation
@Component
public class SMSNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(NotificationDTO notification) {
        // SMS specific code
    }
}
```

### Event-Driven Architecture Ready

```java
// Events published for subscribers
@Component
public class StudentEventListener {
    @EventListener
    public void onStudentCreated(StudentCreatedEvent event) {
        // Send welcome email
    }
    
    @EventListener
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        // Update analytics
    }
}
```

---

## Summary

This LLD document provides:
✅ Clear component responsibilities  
✅ Design pattern applications with SOLID principles  
✅ Detailed service logic with docstrings  
✅ Database schema with proper indexing  
✅ Error handling and validation strategies  
✅ Security implementation details  
✅ Transaction and concurrency handling  
✅ Performance optimization guidelines  
✅ Testing strategy with examples  
✅ Future extensibility considerations  

**Document Version:** 1.0  
**Last Updated:** 2026-06-15
