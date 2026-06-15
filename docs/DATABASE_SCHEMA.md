# Database Schema & Entity Relationship Diagram

## 1. Database Overview

### Purpose
PostgreSQL database to store all application data with ACID compliance, referential integrity, and optimal indexing for query performance.

### Key Characteristics
- **Database Type:** PostgreSQL 14+
- **Character Set:** UTF-8 (for international support)
- **Time Zone:** UTC (all times stored as UTC, converted to local on client)
- **Constraints:** Comprehensive referential integrity and unique constraints

---

## 2. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o{ STUDENTS : "1:1"
    USERS ||--o{ TEACHERS : "1:1"
    USERS ||--o{ NOTIFICATIONS : "1:*"
    
    STUDENTS ||--o{ ENROLLMENTS : "1:*"
    STUDENTS ||--o{ ATTENDANCE : "1:*"
    STUDENTS ||--o{ FEES : "1:*"
    STUDENTS ||--o{ FEE_PAYMENTS : "1:*"
    STUDENTS ||--o{ REPORTS : "1:*"
    STUDENTS ||--o{ PERFORMANCE_METRICS : "1:*"
    
    SUBJECTS ||--o{ ENROLLMENTS : "1:*"
    SUBJECTS ||--o{ TEACHERS : "*:*"
    
    FEES ||--o{ FEE_PAYMENTS : "1:*"
    
    REPORTS ||--o{ PERFORMANCE_METRICS : "1:1"

    USERS {
        bigint id PK
        string email UK
        string password_hash
        string first_name
        string last_name
        string phone
        enum user_type
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    STUDENTS {
        bigint id PK
        bigint user_id FK UK
        string roll_number UK
        enum board
        string standard
        enum enrollment_status
        string google_doc_link
        timestamp created_at
    }

    TEACHERS {
        bigint id PK
        bigint user_id FK UK
        string employee_id UK
        string qualification
        timestamp created_at
    }

    SUBJECTS {
        bigint id PK
        string name UK
        enum board
        string standard
        string description
        boolean is_active
        timestamp created_at
    }

    ENROLLMENTS {
        bigint id PK
        bigint student_id FK
        bigint subject_id FK
        enum board
        string standard
        enum status
        date enrollment_date
        date completion_date
        timestamp created_at
    }

    ATTENDANCE {
        bigint id PK
        bigint student_id FK
        date attendance_date
        enum status
        string remarks
        bigint marked_by FK
        timestamp created_at
    }

    FEES {
        bigint id PK
        bigint student_id FK
        string fee_type
        decimal amount
        decimal outstanding_amount
        date due_date
        enum status
        timestamp created_at
    }

    FEE_PAYMENTS {
        bigint id PK
        bigint fee_id FK
        decimal amount_paid
        date payment_date
        enum payment_method
        string transaction_id
        timestamp created_at
    }

    REPORTS {
        bigint id PK
        bigint student_id FK
        date month
        decimal attendance_percentage
        decimal average_score
        integer star_rating
        integer performance_rank
        timestamp created_at
    }

    PERFORMANCE_METRICS {
        bigint id PK
        bigint student_id FK
        bigint report_id FK
        integer present_days
        integer absent_days
        integer leave_days
        integer outstanding_fees
        timestamp created_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint student_id FK
        enum type
        text message
        enum status
        integer attempts
        timestamp sent_at
        timestamp retry_at
        string error_message
        timestamp created_at
    }
```

---

## 3. Detailed Table Schemas

### 3.1 Users Table

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    user_type VARCHAR(50) NOT NULL CHECK (user_type IN ('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- Trigger to update updated_at timestamp
CREATE TRIGGER update_users_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();
```

**Columns:**
- `id`: Unique user identifier (auto-generated)
- `email`: Unique email address
- `password_hash`: Bcrypt hashed password (never plain text)
- `first_name`, `last_name`: User name
- `phone`: Optional contact number
- `user_type`: Role (ADMIN, TEACHER, STUDENT, PARENT)
- `is_active`: Soft-delete flag (false = inactive)
- `created_at`, `updated_at`: Audit timestamps
- `created_by`: User who created this record

---

### 3.2 Students Table

```sql
CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    board VARCHAR(50) NOT NULL CHECK (board IN ('CBSE', 'ICSE', 'STATE_BOARD', 'IGCSE')),
    standard VARCHAR(10) NOT NULL CHECK (standard ~ '^\d{1,2}$'),
    enrollment_status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (enrollment_status IN ('ACTIVE', 'INACTIVE', 'GRADUATED', 'DROPPED')),
    google_doc_link VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT board_standard_valid CHECK (standard::INTEGER BETWEEN 1 AND 12)
);

-- Indexes
CREATE INDEX idx_students_roll_number ON students(roll_number);
CREATE INDEX idx_students_board_standard ON students(board, standard);
CREATE INDEX idx_students_enrollment_status ON students(enrollment_status);
CREATE INDEX idx_students_user_id ON students(user_id);

-- Unique constraint: one student per user, no duplicate enrollments
CREATE UNIQUE INDEX idx_students_user_id_uniq ON students(user_id);
```

**Columns:**
- `id`: Student unique identifier
- `user_id`: Reference to users table (1:1 relationship)
- `roll_number`: School roll number (unique per board/standard/session)
- `board`: Academic board (CBSE, ICSE, STATE_BOARD, IGCSE)
- `standard`: Class/Grade (1-12)
- `enrollment_status`: Current enrollment status
- `google_doc_link`: Link to Google Sheet with test scores

---

### 3.3 Teachers Table

```sql
CREATE TABLE IF NOT EXISTS teachers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    qualification VARCHAR(255),
    specialization VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_teachers_employee_id ON teachers(employee_id);
CREATE INDEX idx_teachers_user_id ON teachers(user_id);
```

---

### 3.4 Subjects Table

```sql
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    board VARCHAR(50) NOT NULL,
    standard VARCHAR(10) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT subject_board_standard_name UNIQUE (board, standard, name),
    CONSTRAINT board_check CHECK (board IN ('CBSE', 'ICSE', 'STATE_BOARD', 'IGCSE')),
    CONSTRAINT standard_check CHECK (standard::INTEGER BETWEEN 1 AND 12)
);

-- Indexes
CREATE INDEX idx_subjects_board_standard ON subjects(board, standard);
CREATE INDEX idx_subjects_is_active ON subjects(is_active);
CREATE INDEX idx_subjects_name ON subjects(name);

-- Sample data
INSERT INTO subjects (name, board, standard, description, is_active) VALUES
('Mathematics', 'CBSE', '10', 'Algebra, Geometry, Trigonometry', true),
('Physics', 'CBSE', '10', 'Mechanics, Thermodynamics, Optics', true),
('Chemistry', 'CBSE', '10', 'Periodic Table, Bonding, Reactions', true),
('Biology', 'CBSE', '10', 'Cell Biology, Genetics, Evolution', true),
('English', 'CBSE', '10', 'Literature and Comprehension', true),
('Hindi', 'CBSE', '10', 'Language and Literature', true);
```

---

### 3.5 Enrollments Table

```sql
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT,
    board VARCHAR(50) NOT NULL,
    standard VARCHAR(10) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'COMPLETED', 'DROPPED')),
    enrollment_date DATE NOT NULL,
    completion_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT enrollment_dates CHECK (completion_date IS NULL OR completion_date >= enrollment_date),
    CONSTRAINT student_subject_board_standard UNIQUE (student_id, subject_id, board, standard)
);

-- Indexes
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_subject_id ON enrollments(subject_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_board_standard ON enrollments(board, standard);
```

**Key Relationships:**
- One student can enroll in multiple subjects
- Each enrollment is unique per (student, subject, board, standard)
- Status tracks lifecycle (ACTIVE → COMPLETED or DROPPED)

---

### 3.6 Attendance Table

```sql
CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LEAVE')),
    remarks VARCHAR(500),
    marked_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT attendance_date_future CHECK (attendance_date <= CURRENT_DATE),
    CONSTRAINT student_date_unique UNIQUE (student_id, attendance_date)
);

-- Indexes (critical for query performance)
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date DESC);
CREATE INDEX idx_attendance_date ON attendance(attendance_date DESC);
CREATE INDEX idx_attendance_student_month ON attendance(student_id, DATE_TRUNC('month', attendance_date));
CREATE INDEX idx_attendance_status ON attendance(status);

-- Partitioning by year for large tables (optional, for monthly/yearly reports)
-- CREATE TABLE attendance_2026 PARTITION OF attendance FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

**Key Features:**
- Unique constraint prevents duplicate attendance records
- Future date validation at DB level
- Indexed for fast monthly/range queries
- Remarks field for special cases (medical leave, etc.)

---

### 3.7 Fees Table

```sql
CREATE TABLE IF NOT EXISTS fees (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    fee_type VARCHAR(50) NOT NULL,  -- 'TUITION', 'LIBRARY', 'TRANSPORT', 'LAB_FEE', 'EXAM_FEE'
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    outstanding_amount DECIMAL(10, 2) NOT NULL CHECK (outstanding_amount >= 0 AND outstanding_amount <= amount),
    due_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'OUTSTANDING' CHECK (status IN ('OUTSTANDING', 'PARTIAL', 'PAID')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_fees_student_status ON fees(student_id, status);
CREATE INDEX idx_fees_due_date ON fees(due_date);
CREATE INDEX idx_fees_student_due_date ON fees(student_id, due_date DESC);
CREATE INDEX idx_fees_outstanding ON fees(outstanding_amount) WHERE status = 'OUTSTANDING';

-- View for outstanding fees older than 5 days
CREATE VIEW outstanding_fees_overdue AS
SELECT f.*, u.email, u.first_name
FROM fees f
JOIN students s ON f.student_id = s.id
JOIN users u ON s.user_id = u.id
WHERE f.status = 'OUTSTANDING' 
  AND f.due_date < CURRENT_DATE - INTERVAL '5 days'
ORDER BY f.due_date ASC;
```

**Status Logic:**
- `OUTSTANDING`: No payments made (outstanding_amount = amount)
- `PARTIAL`: Some payments made (0 < outstanding_amount < amount)
- `PAID`: Fully paid (outstanding_amount = 0)

---

### 3.8 Fee Payments Table

```sql
CREATE TABLE IF NOT EXISTS fee_payments (
    id BIGSERIAL PRIMARY KEY,
    fee_id BIGINT NOT NULL REFERENCES fees(id) ON DELETE CASCADE,
    amount_paid DECIMAL(10, 2) NOT NULL CHECK (amount_paid > 0),
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) CHECK (payment_method IN ('CASH', 'CHECK', 'BANK_TRANSFER', 'ONLINE')),
    transaction_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_fee_payments_fee_id ON fee_payments(fee_id);
CREATE INDEX idx_fee_payments_payment_date ON fee_payments(payment_date DESC);
CREATE INDEX idx_fee_payments_transaction_id ON fee_payments(transaction_id);

-- Trigger to update parent fee status and outstanding amount
CREATE TRIGGER update_fee_on_payment
AFTER INSERT ON fee_payments
FOR EACH ROW
EXECUTE FUNCTION update_fee_on_payment_func();
```

---

### 3.9 Reports Table

```sql
CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    month DATE NOT NULL,  -- First day of month (e.g., 2026-06-01)
    attendance_percentage DECIMAL(5, 2) NOT NULL CHECK (attendance_percentage >= 0 AND attendance_percentage <= 100),
    average_score DECIMAL(5, 2) NOT NULL CHECK (average_score >= 0 AND average_score <= 100),
    star_rating INTEGER NOT NULL CHECK (star_rating BETWEEN 1 AND 5),
    performance_rank INTEGER NOT NULL CHECK (performance_rank > 0),
    total_students_in_rank INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT student_month_unique UNIQUE (student_id, month)
);

-- Indexes
CREATE INDEX idx_reports_student_month ON reports(student_id, month DESC);
CREATE INDEX idx_reports_month ON reports(month DESC);
CREATE INDEX idx_reports_star_rating ON reports(star_rating DESC);
CREATE INDEX idx_reports_performance_rank ON reports(performance_rank);

-- View for top performers
CREATE VIEW top_performers_current_month AS
SELECT 
    r.student_id,
    u.first_name || ' ' || u.last_name as name,
    s.roll_number,
    r.average_score,
    r.attendance_percentage,
    r.star_rating,
    r.performance_rank
FROM reports r
JOIN students s ON r.student_id = s.id
JOIN users u ON s.user_id = u.id
WHERE r.month = DATE_TRUNC('month', CURRENT_DATE)
  AND r.star_rating = 5
ORDER BY r.performance_rank ASC;
```

---

### 3.10 Performance Metrics Table

```sql
CREATE TABLE IF NOT EXISTS performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    report_id BIGINT NOT NULL UNIQUE REFERENCES reports(id) ON DELETE CASCADE,
    present_days INTEGER NOT NULL CHECK (present_days >= 0),
    absent_days INTEGER NOT NULL CHECK (absent_days >= 0),
    leave_days INTEGER NOT NULL CHECK (leave_days >= 0),
    outstanding_fees INTEGER NOT NULL CHECK (outstanding_fees >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_performance_metrics_student_id ON performance_metrics(student_id);
CREATE INDEX idx_performance_metrics_report_id ON performance_metrics(report_id);
```

---

### 3.11 Notifications Table

```sql
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,  -- 'EMAIL', 'SMS', 'PUSH'
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'BOUNCED')),
    attempts INTEGER DEFAULT 0 CHECK (attempts >= 0 AND attempts <= 3),
    sent_at TIMESTAMP,
    retry_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT max_retries CHECK (attempts <= 3)
);

-- Indexes
CREATE INDEX idx_notifications_student_status ON notifications(student_id, status);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_retry_at ON notifications(retry_at) WHERE status = 'PENDING' AND retry_at IS NOT NULL;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- View for notifications pending retry
CREATE VIEW notifications_pending_retry AS
SELECT * FROM notifications
WHERE status = 'PENDING' 
  AND retry_at IS NOT NULL 
  AND retry_at <= CURRENT_TIMESTAMP
  AND attempts < 3
ORDER BY retry_at ASC;
```

---

## 4. Key Constraints & Validations

### Primary Key Strategy
- All tables use `BIGSERIAL` for auto-increment IDs
- Supports up to 9,223,372,036,854,775,807 records per table

### Foreign Key Constraints
- `ON DELETE CASCADE`: Student deletion cascades to attendance, fees, reports, etc.
- `ON DELETE RESTRICT`: Subject deletion prevented if enrollments exist
- `ON DELETE SET NULL`: Audit fields (created_by) can be nullified

### Unique Constraints
- `email` in users (case-insensitive comparison in app)
- `roll_number` in students per board/standard/session
- `student_id, subject_id` in enrollments (no duplicate enrollments)
- `student_id, attendance_date` in attendance (one record per day per student)

### Check Constraints
- Email format validation
- Percentage ranges (0-100)
- Date comparisons (attendance date ≤ today)
- Fee amount validations (> 0, outstanding ≤ amount)

---

## 5. Indexing Strategy

### Query Performance Optimization

| Table | Index | Columns | Purpose |
|-------|-------|---------|---------|
| **users** | idx_users_email | email | Fast login lookups |
| **students** | idx_students_roll_number | roll_number | Student search |
| **students** | idx_students_board_standard | board, standard | Class enrollment queries |
| **attendance** | idx_attendance_student_date | student_id, attendance_date | Monthly reports |
| **attendance** | idx_attendance_date | attendance_date | School-wide attendance view |
| **fees** | idx_fees_student_status | student_id, status | Fee status reports |
| **fees** | idx_fees_outstanding | outstanding_amount | Reminder generation |
| **reports** | idx_reports_star_rating | star_rating | Top performers query |
| **notifications** | idx_notifications_retry_at | retry_at | Scheduled retry processing |

### Indexing Best Practices Applied
✅ Composite indexes on frequently joined columns  
✅ Partial indexes on status columns (WHERE clause)  
✅ DESC ordering for date columns (recent first)  
✅ Separate indexes for different query patterns  

---

## 6. Data Dictionary

### Enumerations

**user_type:**
- `ADMIN`: Full system access
- `TEACHER`: Can manage own subject, mark attendance, view assigned students
- `STUDENT`: Can view own data
- `PARENT`: Can view child's data

**board:**
- `CBSE`: Central Board of Secondary Education
- `ICSE`: Indian Certificate of Secondary Education
- `STATE_BOARD`: State-specific curriculum
- `IGCSE`: International General Certificate

**status (Attendance):**
- `PRESENT`: Student attended class
- `ABSENT`: Student did not attend
- `LEAVE`: Authorized absence

**status (Fee):**
- `OUTSTANDING`: Not paid
- `PARTIAL`: Partially paid
- `PAID`: Fully paid

**status (Notification):**
- `PENDING`: Queued for sending
- `SENT`: Successfully delivered
- `FAILED`: Failed after retries
- `BOUNCED`: Email bounced

---

## 7. Sample SQL Queries

### Get Monthly Attendance Report

```sql
SELECT 
    s.id,
    u.first_name,
    u.last_name,
    COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present_days,
    COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) as absent_days,
    COUNT(CASE WHEN a.status = 'LEAVE' THEN 1 END) as leave_days,
    ROUND(
        (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0) / 
        COUNT(*), 
        2
    ) as attendance_percentage
FROM students s
JOIN users u ON s.user_id = u.id
LEFT JOIN attendance a ON s.id = a.student_id 
    AND DATE_TRUNC('month', a.attendance_date) = '2026-06-01'
WHERE s.board = 'CBSE' AND s.standard = '10'
GROUP BY s.id, u.first_name, u.last_name
ORDER BY attendance_percentage DESC;
```

### Get Overdue Fees

```sql
SELECT 
    f.id,
    s.roll_number,
    u.first_name || ' ' || u.last_name as student_name,
    f.fee_type,
    f.outstanding_amount,
    f.due_date,
    CURRENT_DATE - f.due_date as days_overdue
FROM fees f
JOIN students s ON f.student_id = s.id
JOIN users u ON s.user_id = u.id
WHERE f.status IN ('OUTSTANDING', 'PARTIAL')
  AND f.due_date < CURRENT_DATE - INTERVAL '5 days'
ORDER BY f.due_date ASC;
```

### Get Top Performers

```sql
SELECT 
    r.student_id,
    u.first_name || ' ' || u.last_name as name,
    s.roll_number,
    r.average_score,
    r.attendance_percentage,
    r.star_rating,
    r.performance_rank
FROM reports r
JOIN students s ON r.student_id = s.id
JOIN users u ON s.user_id = u.id
WHERE r.month = DATE_TRUNC('month', CURRENT_DATE)
  AND r.star_rating >= 5
ORDER BY r.performance_rank ASC
LIMIT 10;
```

---

## 8. Database Setup Instructions

### Prerequisites
- PostgreSQL 14 or higher
- psql command-line tool
- Database admin credentials

### Create Database

```bash
# As PostgreSQL admin
sudo -u postgres psql

# Create database
CREATE DATABASE attendance_db 
    OWNER postgres 
    ENCODING 'UTF8' 
    LC_COLLATE 'en_US.UTF-8' 
    LC_CTYPE 'en_US.UTF-8';

# Connect to new database
\c attendance_db

# Enable UUID extension (if using UUIDs)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

# Enable advanced indexing
CREATE EXTENSION IF NOT EXISTS "btree_gin";
```

### Create All Tables

```bash
# Execute the comprehensive SQL script
psql -U postgres -d attendance_db -f schema.sql

# Verify tables created
\dt

# Verify indexes
\di
```

---

## 9. Backup & Recovery

### Backup Strategy

```bash
# Full database backup
pg_dump -U postgres -d attendance_db -Fc -f attendance_db_backup_$(date +%Y%m%d).dump

# Backup with compression and verbosity
pg_dump -U postgres -d attendance_db -Fc -v -f attendance_db_$(date +%Y%m%d_%H%M%S).dump

# Scheduled daily backup (cron)
0 2 * * * pg_dump -U postgres -d attendance_db -Fc -f /backups/attendance_$(date +\%Y\%m\%d).dump
```

### Recovery

```bash
# Restore from dump
pg_restore -U postgres -d attendance_db -v attendance_db_backup.dump

# Restore specific table only
pg_restore -U postgres -d attendance_db -t students attendance_db_backup.dump
```

---

## 10. Database Monitoring Queries

### Check Table Sizes

```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Check Index Usage

```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

### Check Long-Running Queries

```sql
SELECT 
    pid,
    usename,
    query,
    query_start,
    NOW() - query_start as duration
FROM pg_stat_activity
WHERE state != 'idle'
ORDER BY query_start ASC;
```

---

**Document Version:** 1.0  
**Last Updated:** 2026-06-15  
**Database Status:** Schema Design Complete ✅
