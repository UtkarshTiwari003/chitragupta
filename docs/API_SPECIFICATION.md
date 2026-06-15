# API Specification - Attendance Tracking System

## 1. API Overview

### Base URL
```
https://api.attendance-system.com/api/v1
```

### API Versioning
- Current Version: v1
- Backward compatibility maintained for minor versions
- Major version changes documented with migration guide

### Authentication
- **Type:** Bearer Token (JWT)
- **Header:** `Authorization: Bearer <access_token>`
- **Token Expiry:** 1 hour
- **Refresh URL:** `POST /auth/refresh`

---

## 2. Standard Response Format

### Success Response

```json
{
  "status": 200,
  "message": "Operation successful",
  "data": {
    // Response payload
  },
  "timestamp": 1623456789,
  "path": "/api/v1/students/123"
}
```

### Error Response

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": "Invalid enrollment data provided",
  "fieldErrors": [
    {
      "field": "email",
      "value": "invalid-email",
      "message": "Email format is invalid"
    },
    {
      "field": "board",
      "value": null,
      "message": "Board is required"
    }
  ],
  "timestamp": 1623456789,
  "path": "/api/v1/students"
}
```

---

## 3. Error Codes Reference

| Code | HTTP Status | Description | Retry |
|------|-------------|-------------|-------|
| `AUTH_001` | 401 | Invalid credentials | No |
| `AUTH_002` | 401 | Token expired | Yes (use refresh) |
| `AUTH_003` | 403 | Access denied - insufficient permissions | No |
| `AUTH_004` | 401 | Token invalid/malformed | No |
| `STU_001` | 404 | Student not found | No |
| `STU_002` | 409 | Student already exists | No |
| `ATT_001` | 400 | Attendance date is in future | No |
| `ATT_002` | 409 | Attendance already marked for this date | No |
| `FEE_001` | 400 | Payment amount must be positive | No |
| `FEE_002` | 400 | Payment exceeds outstanding amount | No |
| `FEE_003` | 404 | Fee not found | No |
| `VAL_001` | 400 | Validation error | No |
| `RES_001` | 404 | Resource not found | No |
| `SVC_001` | 503 | Service temporarily unavailable | Yes |
| `DB_001` | 500 | Database error | Yes |
| `INT_001` | 500 | Internal server error | Yes |

---

## 4. Authentication Endpoints

### 4.1 User Login

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "email": "teacher@school.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "teacher@school.com",
      "firstName": "Rajesh",
      "lastName": "Kumar",
      "userType": "TEACHER",
      "roles": ["TEACHER"]
    },
    "expiresIn": 3600
  },
  "timestamp": 1623456789
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": 401,
  "code": "AUTH_001",
  "message": "Invalid credentials",
  "timestamp": 1623456789
}
```

**cURL Example:**
```bash
curl -X POST https://api.attendance-system.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@school.com",
    "password": "SecurePassword123!"
  }'
```

---

### 4.2 Refresh Token

**Endpoint:** `POST /auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Token refreshed",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  },
  "timestamp": 1623456789
}
```

---

### 4.3 Logout

**Endpoint:** `POST /auth/logout`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Logout successful",
  "timestamp": 1623456789
}
```

---

## 5. Student Endpoints

### 5.1 Get All Students (Teacher/Admin Only)

**Endpoint:** `GET /students`

**Query Parameters:**
```
board=CBSE&standard=10&enrollmentStatus=ACTIVE&page=0&size=20&sort=lastName,asc
```

**Request Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Students retrieved",
  "data": {
    "content": [
      {
        "id": 123,
        "email": "student@example.com",
        "firstName": "Arjun",
        "lastName": "Singh",
        "rollNumber": "CSE001",
        "board": "CBSE",
        "standard": "10",
        "enrollmentStatus": "ACTIVE",
        "googleDocLink": "https://docs.google.com/spreadsheets/d/...",
        "createdAt": "2026-01-15T08:30:00Z"
      }
    ],
    "page": 0,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": 1623456789
}
```

**Access Control:**
- TEACHER: Can view students they teach
- ADMIN: Can view all students

---

### 5.2 Get Student By ID

**Endpoint:** `GET /students/{studentId}`

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Student retrieved",
  "data": {
    "id": 123,
    "email": "student@example.com",
    "firstName": "Arjun",
    "lastName": "Singh",
    "rollNumber": "CSE001",
    "board": "CBSE",
    "standard": "10",
    "enrollmentStatus": "ACTIVE",
    "googleDocLink": "https://docs.google.com/spreadsheets/d/...",
    "createdAt": "2026-01-15T08:30:00Z",
    "enrolledSubjects": [
      {
        "id": 1,
        "name": "Mathematics",
        "board": "CBSE",
        "standard": "10",
        "status": "ACTIVE"
      },
      {
        "id": 2,
        "name": "Physics",
        "board": "CBSE",
        "standard": "10",
        "status": "ACTIVE"
      }
    ]
  },
  "timestamp": 1623456789
}
```

---

### 5.3 Create Student

**Endpoint:** `POST /students`

**Request:**
```json
{
  "email": "newstudent@example.com",
  "password": "SecurePassword123!",
  "firstName": "Priya",
  "lastName": "Sharma",
  "phone": "+91-9876543210",
  "rollNumber": "CSE002",
  "board": "CBSE",
  "standard": "10",
  "googleDocLink": "https://docs.google.com/spreadsheets/d/..."
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Student created successfully",
  "data": {
    "id": 124,
    "email": "newstudent@example.com",
    "firstName": "Priya",
    "lastName": "Sharma",
    "rollNumber": "CSE002",
    "board": "CBSE",
    "standard": "10",
    "enrollmentStatus": "ACTIVE",
    "createdAt": "2026-06-15T10:30:00Z"
  },
  "timestamp": 1623456789
}
```

**Validation Rules:**
- Email format must be valid and unique
- Password: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
- Standard: 1-12
- Roll number: Must be unique per board/standard

---

### 5.4 Update Student

**Endpoint:** `PUT /students/{studentId}`

**Request:**
```json
{
  "firstName": "Priya",
  "lastName": "Sharma",
  "phone": "+91-9876543210",
  "board": "CBSE",
  "standard": "10",
  "googleDocLink": "https://docs.google.com/spreadsheets/d/...",
  "enrollmentStatus": "ACTIVE"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Student updated successfully",
  "data": {
    "id": 124,
    "email": "newstudent@example.com",
    "firstName": "Priya",
    "lastName": "Sharma",
    "rollNumber": "CSE002",
    "board": "CBSE",
    "standard": "10",
    "enrollmentStatus": "ACTIVE",
    "googleDocLink": "https://docs.google.com/spreadsheets/d/...",
    "updatedAt": "2026-06-15T14:30:00Z"
  },
  "timestamp": 1623456789
}
```

---

## 6. Attendance Endpoints

### 6.1 Mark Attendance

**Endpoint:** `POST /attendance/mark`

**Request:**
```json
{
  "studentId": 123,
  "date": "2026-06-15",
  "status": "PRESENT",
  "remarks": "Present in class"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Attendance marked successfully",
  "data": {
    "id": 456,
    "studentId": 123,
    "date": "2026-06-15",
    "status": "PRESENT",
    "remarks": "Present in class",
    "markedBy": 1,
    "createdAt": "2026-06-15T09:00:00Z"
  },
  "timestamp": 1623456789
}
```

**Status Options:**
- `PRESENT`: Student attended
- `ABSENT`: Student absent
- `LEAVE`: Authorized leave

**Validation:**
- Date must not be in future
- One record per student per date
- Student must exist
- Teacher must be authenticated

---

### 6.2 Get Attendance Records

**Endpoint:** `GET /attendance/student/{studentId}`

**Query Parameters:**
```
startDate=2026-06-01&endDate=2026-06-30&page=0&size=50
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Attendance records retrieved",
  "data": {
    "content": [
      {
        "id": 456,
        "studentId": 123,
        "date": "2026-06-15",
        "status": "PRESENT",
        "remarks": "Present in class",
        "markedBy": 1
      },
      {
        "id": 457,
        "studentId": 123,
        "date": "2026-06-16",
        "status": "ABSENT",
        "remarks": null,
        "markedBy": 1
      }
    ],
    "totalRecords": 20,
    "page": 0,
    "totalPages": 1
  },
  "timestamp": 1623456789
}
```

---

### 6.3 Get Attendance Report

**Endpoint:** `GET /attendance/report`

**Query Parameters:**
```
studentId=123&startDate=2026-06-01&endDate=2026-06-30&format=JSON
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Attendance report generated",
  "data": {
    "studentId": 123,
    "studentName": "Arjun Singh",
    "period": {
      "startDate": "2026-06-01",
      "endDate": "2026-06-30"
    },
    "summary": {
      "presentDays": 18,
      "absentDays": 2,
      "leaveDays": 1,
      "totalDays": 21,
      "attendancePercentage": 85.71
    },
    "details": [
      {
        "date": "2026-06-01",
        "status": "PRESENT",
        "remarks": "Regular"
      }
    ]
  },
  "timestamp": 1623456789
}
```

---

## 7. Fee Management Endpoints

### 7.1 Record Payment

**Endpoint:** `POST /fees/payment`

**Request:**
```json
{
  "feeId": 789,
  "amount": 2500.00,
  "paymentMethod": "ONLINE",
  "transactionId": "TXN123456789"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Payment recorded successfully",
  "data": {
    "id": 1001,
    "feeId": 789,
    "amount": 2500.00,
    "paymentDate": "2026-06-15",
    "paymentMethod": "ONLINE",
    "transactionId": "TXN123456789",
    "feeStatus": "PARTIAL",
    "outstandingAmount": 2500.00,
    "createdAt": "2026-06-15T11:30:00Z"
  },
  "timestamp": 1623456789
}
```

**Validation:**
- Amount must be positive
- Amount cannot exceed outstanding amount
- Fee must exist and be outstanding/partial

---

### 7.2 Get Student Fees

**Endpoint:** `GET /fees/student/{studentId}`

**Query Parameters:**
```
status=OUTSTANDING&sortBy=dueDate&order=asc
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Student fees retrieved",
  "data": [
    {
      "id": 789,
      "studentId": 123,
      "feeType": "TUITION",
      "amount": 5000.00,
      "outstandingAmount": 2500.00,
      "dueDate": "2026-06-30",
      "status": "PARTIAL",
      "lastPaymentDate": "2026-06-15",
      "createdAt": "2026-06-01T08:00:00Z"
    },
    {
      "id": 790,
      "studentId": 123,
      "feeType": "LIBRARY",
      "amount": 500.00,
      "outstandingAmount": 500.00,
      "dueDate": "2026-07-15",
      "status": "OUTSTANDING",
      "createdAt": "2026-06-01T08:00:00Z"
    }
  ],
  "totalOutstanding": 3000.00,
  "timestamp": 1623456789
}
```

---

### 7.3 Get Outstanding Fees (Admin/Teacher)

**Endpoint:** `GET /fees/outstanding`

**Query Parameters:**
```
board=CBSE&standard=10&daysOverdue=5&page=0&size=20
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Outstanding fees retrieved",
  "data": {
    "content": [
      {
        "id": 789,
        "studentId": 123,
        "studentName": "Arjun Singh",
        "rollNumber": "CSE001",
        "feeType": "TUITION",
        "outstandingAmount": 2500.00,
        "dueDate": "2026-06-30",
        "daysOverdue": 15,
        "lastReminderSent": "2026-07-10T08:00:00Z"
      }
    ],
    "totalOutstandingAmount": 125000.00,
    "totalStudentsAffected": 35,
    "page": 0,
    "totalPages": 2
  },
  "timestamp": 1623456789
}
```

---

## 8. Report Endpoints

### 8.1 Get Monthly Report

**Endpoint:** `GET /reports/monthly/{studentId}`

**Query Parameters:**
```
month=2026-06&format=JSON
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Monthly report retrieved",
  "data": {
    "studentId": 123,
    "studentName": "Arjun Singh",
    "month": "2026-06",
    "summary": {
      "attendancePercentage": 85.71,
      "averageScore": 78.50,
      "outstandingFees": 1,
      "starRating": 3,
      "performanceRank": 25
    },
    "attendance": {
      "presentDays": 18,
      "absentDays": 2,
      "leaveDays": 1,
      "attendancePercentage": 85.71
    },
    "testScores": [
      {
        "testName": "Unit Test 1",
        "score": 85.0,
        "subject": "Mathematics",
        "date": "2026-06-05"
      },
      {
        "testName": "Unit Test 2",
        "score": 72.0,
        "subject": "Physics",
        "date": "2026-06-12"
      }
    ],
    "fees": {
      "totalFees": 5500.00,
      "paidAmount": 3000.00,
      "outstandingAmount": 2500.00,
      "outstandingCount": 1
    },
    "starRatingDetails": {
      "rating": 3,
      "badge": "⭐⭐⭐ Good",
      "criteria": {
        "attendance": "85.71% (≥75%)",
        "score": "78.50% (≥70%)",
        "fees": "1 outstanding (≤2)"
      }
    },
    "leaderboard": {
      "rank": 25,
      "totalStudents": 150,
      "topScorers": [
        {
          "rank": 1,
          "name": "Priya Sharma",
          "score": 95.0,
          "starRating": 5
        }
      ]
    },
    "generatedAt": "2026-07-01T23:00:00Z"
  },
  "timestamp": 1623456789
}
```

---

### 8.2 Get Performance Report

**Endpoint:** `GET /reports/performance`

**Query Parameters:**
```
board=CBSE&standard=10&month=2026-06&topN=10
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Performance report retrieved",
  "data": {
    "month": "2026-06",
    "board": "CBSE",
    "standard": "10",
    "topPerformers": [
      {
        "rank": 1,
        "studentId": 456,
        "name": "Priya Sharma",
        "rollNumber": "CSE002",
        "averageScore": 95.0,
        "attendancePercentage": 98.0,
        "outstandingFees": 0,
        "starRating": 5
      },
      {
        "rank": 2,
        "studentId": 789,
        "name": "Rahul Verma",
        "rollNumber": "CSE003",
        "averageScore": 92.5,
        "attendancePercentage": 95.0,
        "outstandingFees": 0,
        "starRating": 5
      }
    ],
    "statistics": {
      "totalStudents": 150,
      "averageScore": 72.3,
      "averageAttendance": 82.1,
      "fiveStarCount": 15,
      "fourStarCount": 35,
      "threeStarCount": 60,
      "twoStarCount": 35,
      "oneStarCount": 5
    }
  },
  "timestamp": 1623456789
}
```

---

## 9. Subject & Enrollment Endpoints

### 9.1 Get All Subjects

**Endpoint:** `GET /subjects`

**Query Parameters:**
```
board=CBSE&standard=10&isActive=true
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Subjects retrieved",
  "data": [
    {
      "id": 1,
      "name": "Mathematics",
      "board": "CBSE",
      "standard": "10",
      "description": "Algebra, Geometry, Trigonometry",
      "isActive": true
    },
    {
      "id": 2,
      "name": "Physics",
      "board": "CBSE",
      "standard": "10",
      "description": "Mechanics, Thermodynamics, Optics",
      "isActive": true
    }
  ],
  "timestamp": 1623456789
}
```

---

### 9.2 Enroll Student in Subject

**Endpoint:** `POST /enrollments`

**Request:**
```json
{
  "studentId": 123,
  "subjectId": 1,
  "board": "CBSE",
  "standard": "10"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Enrollment successful",
  "data": {
    "id": 501,
    "studentId": 123,
    "subjectId": 1,
    "subjectName": "Mathematics",
    "board": "CBSE",
    "standard": "10",
    "status": "ACTIVE",
    "enrollmentDate": "2026-06-15",
    "createdAt": "2026-06-15T10:00:00Z"
  },
  "timestamp": 1623456789
}
```

**Validation:**
- No duplicate enrollments for same subject
- Student board must match subject board
- Student must be in correct standard

---

### 9.3 Get Student Enrollments

**Endpoint:** `GET /enrollments/student/{studentId}`

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Enrollments retrieved",
  "data": [
    {
      "id": 501,
      "studentId": 123,
      "subjectId": 1,
      "subjectName": "Mathematics",
      "board": "CBSE",
      "standard": "10",
      "status": "ACTIVE",
      "enrollmentDate": "2026-06-15"
    },
    {
      "id": 502,
      "studentId": 123,
      "subjectId": 2,
      "subjectName": "Physics",
      "board": "CBSE",
      "standard": "10",
      "status": "ACTIVE",
      "enrollmentDate": "2026-06-15"
    }
  ],
  "timestamp": 1623456789
}
```

---

## 10. Rate Limiting & Throttling

### Rate Limits

| Endpoint Category | Limit | Window |
|---|---|---|
| Authentication | 5 requests | 1 minute |
| Read operations | 100 requests | 1 minute |
| Write operations | 20 requests | 1 minute |
| Reports (heavy) | 5 requests | 1 minute |

### Rate Limit Headers

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1623456849
```

### Rate Limit Exceeded Response (429)

```json
{
  "status": 429,
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests",
  "details": "Rate limit exceeded. Retry after 60 seconds",
  "retryAfter": 60,
  "timestamp": 1623456789
}
```

---

## 11. Pagination

### Query Parameters

```
page=0&size=20&sort=lastName,asc&sort=firstName,asc
```

### Paginated Response

```json
{
  "status": 200,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false,
    "isFirst": true,
    "isLast": false,
    "numberOfElements": 20
  }
}
```

---

## 12. Filtering & Sorting

### Common Filter Parameters

```
?board=CBSE
?standard=10
?status=ACTIVE
?createdFrom=2026-06-01&createdTo=2026-06-30
?email=student@example.com
?search=Arjun  # Search by name or email
```

### Sorting

```
?sort=lastName,asc
?sort=createdAt,desc
?sort=attendancePercentage,desc&sort=lastName,asc
```

---

## 13. Webhook Events (Future)

### Event Types (Phase 2)

```json
{
  "events": [
    {
      "type": "student.created",
      "timestamp": "2026-06-15T10:00:00Z",
      "data": { "studentId": 123 }
    },
    {
      "type": "attendance.marked",
      "timestamp": "2026-06-15T09:00:00Z",
      "data": { "studentId": 123, "status": "PRESENT" }
    },
    {
      "type": "fee.payment_received",
      "timestamp": "2026-06-15T14:30:00Z",
      "data": { "feeId": 789, "amount": 2500 }
    },
    {
      "type": "report.generated",
      "timestamp": "2026-07-01T23:00:00Z",
      "data": { "studentId": 123, "month": "2026-06" }
    }
  ]
}
```

---

## 14. CORS Configuration

### Allowed Origins
```
http://localhost:4200         # Angular dev server
https://frontend.school.com   # Production frontend
```

### Allowed Methods
```
GET, POST, PUT, DELETE, OPTIONS
```

### Allowed Headers
```
Content-Type, Authorization, X-Requested-With
```

### Exposed Headers
```
Content-Length, Content-Type, X-RateLimit-Limit, X-RateLimit-Remaining
```

---

## 15. API Documentation

### Interactive API Docs
- **Swagger UI:** `https://api.attendance-system.com/swagger-ui.html`
- **OpenAPI JSON:** `https://api.attendance-system.com/v3/api-docs`

### Postman Collection
- Available at: `https://api.attendance-system.com/postman/collection.json`
- Import into Postman for easy testing

---

**Document Version:** 1.0  
**Last Updated:** 2026-06-15  
**API Status:** Specification Complete ✅
