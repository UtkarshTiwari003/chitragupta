# Attendance Tracking System - Spring Boot Backend

## 📋 Project Overview

A comprehensive, production-ready Spring Boot application for managing student attendance, academic records, fees, and automated reporting in educational institutions.

### 🎯 Key Features

✅ **Authentication & Authorization** - JWT-based security with role-based access control (RBAC)
✅ **Student Management** - Complete student profiles with enrollment tracking
✅ **Attendance Tracking** - Daily attendance recording with automated percentage calculation
✅ **Academic Integration** - Link to Google Sheets for test scores
✅ **Fee Management** - Track fees with payment reminders every 5+ days after due date
✅ **Automated Reports** - Monthly aggregated reports with attendance, scores, and fees
✅ **Gamification** - Star ratings (1-5) based on performance metrics
✅ **Email Notifications** - Automated notifications via SendGrid
✅ **API Documentation** - Interactive Swagger UI with OpenAPI 3.0
✅ **Production Ready** - Comprehensive error handling, logging, and monitoring

---

## 🏗️ Project Structure

```
attendance-system/
├── docs/
│   ├── PROJECT_PLAN.md              # Complete project plan and phases
│   ├── LLD.md                        # Low-Level Design with design patterns
│   ├── FLOW_DIAGRAMS.md             # Business process flow diagrams
│   ├── DATABASE_SCHEMA.md           # Database ERD and SQL schemas
│   ├── API_SPECIFICATION.md         # REST API endpoints and contracts
│   └── ARCHITECTURE.md              # System architecture and deployment
│
├── src/
│   ├── main/
│   │   ├── java/com/attendance/
│   │   │   ├── AttendanceSystemApplication.java  # Main Spring Boot class
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/             # Business Logic Services
│   │   │   ├── repository/          # Data Access Layer
│   │   │   ├── model/               # JPA Entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Custom Exception Classes
│   │   │   ├── config/              # Spring Configuration
│   │   │   └── util/                # Utility Classes
│   │   │
│   │   └── resources/
│   │       ├── application.yml      # Spring Boot Configuration
│   │       └── db/
│   │           └── migration/       # Flyway/Liquibase Migrations (future)
│   │
│   └── test/
│       └── java/com/attendance/     # Unit & Integration Tests
│
├── pom.xml                           # Maven Configuration
├── Dockerfile                        # Docker Configuration
├── docker-compose.yml                # Local Development Setup
└── README.md                         # This file
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Redis 7+**
- **Docker & Docker Compose** (optional, for containerization)

### Setup Instructions

#### 1. Clone the Repository

```bash
git clone https://github.com/your-org/attendance-system.git
cd attendance-system
```

#### 2. Configure Database

Edit `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/attendance_db
    username: postgres
    password: your_password
```

#### 3. Create Database

```bash
# Using PostgreSQL CLI
createdb attendance_db

# Apply initial schema (create tables.sql from docs)
psql -U postgres -d attendance_db -f docs/database/schema.sql
```

#### 4. Set Environment Variables

```bash
# JWT Secret (minimum 256-bit/32 bytes)
export JWT_SECRET="your-super-secret-key-at-least-32-bytes-long-here"

# SendGrid API Key
export SENDGRID_API_KEY="SG.your-api-key-here"

# Google Sheets API Key
export GOOGLE_SHEETS_API_KEY="your-google-api-key-here"
```

#### 5. Build Project

```bash
mvn clean install -DskipTests

# Or with tests
mvn clean install
```

#### 6. Run Application

```bash
# Using Maven
mvn spring-boot:run

# Or using Java directly
java -jar target/attendance-system-1.0.0.jar

# With specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### 7. Access Application

- **API Base URL:** `http://localhost:8080/api/v1`
- **Swagger UI:** `http://localhost:8080/api/v1/swagger-ui.html`
- **API Docs JSON:** `http://localhost:8080/api/v1/v3/api-docs`
- **Health Check:** `http://localhost:8080/actuator/health`

---

## 🐳 Docker Setup (Recommended for Development)

### Using Docker Compose

```bash
# Start all services (PostgreSQL, Redis, Application)
docker-compose up -d

# View logs
docker-compose logs -f attendance-app

# Stop services
docker-compose down
```

### Using Individual Docker Commands

```bash
# Build Docker image
docker build -t attendance-system:1.0.0 .

# Run container with network
docker network create attendance-network
docker run -d --name postgres --network attendance-network \
  -e POSTGRES_DB=attendance_db \
  postgres:14-alpine

docker run -d --name redis --network attendance-network \
  redis:7-alpine

docker run -d --name attendance-app --network attendance-network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/attendance_db \
  attendance-system:1.0.0
```

---

## 📝 API Usage Examples

### 1. User Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@school.com",
    "password": "SecurePassword123!"
  }'

# Response:
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

### 2. Get Students

```bash
curl -X GET "http://localhost:8080/api/v1/students?board=CBSE&standard=10" \
  -H "Authorization: Bearer <access_token>"
```

### 3. Mark Attendance

```bash
curl -X POST http://localhost:8080/api/v1/attendance/mark \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 123,
    "date": "2026-06-15",
    "status": "PRESENT"
  }'
```

### 4. Record Payment

```bash
curl -X POST http://localhost:8080/api/v1/fees/payment \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "feeId": 789,
    "amount": 2500.00,
    "paymentMethod": "ONLINE",
    "transactionId": "TXN123456789"
  }'
```

### 5. Get Monthly Report

```bash
curl -X GET "http://localhost:8080/api/v1/reports/monthly/123?month=2026-06" \
  -H "Authorization: Bearer <access_token>"
```

---

## 🧪 Testing

### Run All Tests

```bash
# Unit and Integration Tests
mvn test

# With coverage report
mvn test jacoco:report

# View coverage: target/site/jacoco/index.html
```

### Run Specific Test

```bash
mvn test -Dtest=StudentServiceTest
mvn test -Dtest=StudentServiceTest#testCreateStudent_Success
```

### Integration Tests

```bash
# Spring Boot Test with @SpringBootTest
mvn test -Dgroups=integration
```

---

## 🔐 Security

### Password Requirements

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (!@#$%^&*)

### JWT Token Structure

```
Header.Payload.Signature

Header: {"alg": "HS256", "typ": "JWT"}
Payload: {
  "sub": "user@example.com",
  "userId": 123,
  "roles": ["TEACHER"],
  "iat": 1623456789,
  "exp": 1623460389
}
```

### CORS Configuration

Default allowed origins:
- `http://localhost:4200` (Angular dev server)
- `http://localhost:4201` (Alternate dev port)

Update in `application.yml` to add production origins.

---

## 📊 Database Schema

Key tables:
- **users** - Authentication and user profiles
- **students** - Student profiles with enrollment details
- **teachers** - Teacher information
- **subjects** - Academic subjects
- **enrollment** - Student-subject mapping
- **attendance** - Daily attendance records
- **fees** - Fee structure
- **fee_payments** - Payment transactions
- **reports** - Monthly aggregated reports
- **notifications** - Email/notification history

See `docs/DATABASE_SCHEMA.md` for complete ERD and SQL creation scripts.

---

## 🔄 Scheduled Tasks

The application runs scheduled tasks for automated operations:

### Daily Tasks (8 AM)
- Send fee reminders for overdue fees (5+ days past due date)

### Monthly Tasks (11 PM on last day of month)
- Generate monthly reports for all students
- Send aggregated reports via email
- Update performance rankings and leaderboards

Configure in `application.yml` under `app.notifications` and `app.fee`.

---

## 📊 Monitoring & Health Checks

### Health Endpoints

```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Detailed health info (with authorization)
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/actuator/health?details=when-authorized

# Metrics
curl http://localhost:8080/actuator/metrics
```

### Important Metrics

- `http_requests_total` - Total requests
- `http_requests_duration_seconds` - Response time
- `jpa_hibernate_sessions_open` - Database connections
- `redis_commands_duration_seconds` - Cache performance

---

## 📝 Logging

Logs are written to:
- **Console:** INFO level by default
- **File:** `logs/application.log` with daily rotation

### Log Levels

```yaml
logging:
  level:
    root: INFO
    com.attendance: DEBUG              # Application code
    org.springframework.security: DEBUG # Security details
    org.hibernate.SQL: DEBUG          # SQL queries
```

---

## 🚨 Error Codes

Common error codes and HTTP status:

| Code | Status | Meaning |
|------|--------|---------|
| AUTH_001 | 401 | Invalid credentials |
| AUTH_002 | 401 | Token expired |
| AUTH_003 | 403 | Access denied |
| STU_001 | 404 | Student not found |
| VAL_001 | 400 | Validation error |
| FEE_001 | 400 | Payment amount invalid |
| INT_001 | 500 | Internal server error |

See `docs/API_SPECIFICATION.md` for complete error reference.

---

## 📚 Additional Documentation

- **Project Plan:** `docs/PROJECT_PLAN.md` - Overall strategy and phases
- **Low-Level Design:** `docs/LLD.md` - Design patterns and component details
- **Flow Diagrams:** `docs/FLOW_DIAGRAMS.md` - Business process flows
- **Database Schema:** `docs/DATABASE_SCHEMA.md` - ERD and SQL
- **API Specification:** `docs/API_SPECIFICATION.md` - All endpoints
- **Architecture:** `docs/ARCHITECTURE.md` - System design and deployment

---

## 🔧 Development Tips

### Hot Reload

Spring DevTools is enabled. Changes to files automatically restart the application:

```bash
# Rebuild on file changes
mvn spring-boot:run

# Or use IDE: File → Project → Rebuild
```

### Database Migrations

To add schema changes:

1. Create migration file: `src/main/resources/db/migration/V001__initial_schema.sql`
2. Flyway will auto-apply on startup

### Cache Management

```bash
# View cached data
redis-cli keys "*"

# Clear specific cache
redis-cli DEL students

# Clear all caches
redis-cli FLUSHDB
```

---

## 🐛 Troubleshooting

### PostgreSQL Connection Error

```
Problem: Connection refused on localhost:5432
Solution:
1. Check if PostgreSQL is running: sudo systemctl status postgresql
2. Verify port: netstat -an | grep 5432
3. Restart if needed: sudo systemctl restart postgresql
```

### Redis Connection Error

```
Problem: Cannot connect to Redis
Solution:
1. Check if Redis running: redis-cli ping
2. Start Redis: redis-server
3. Verify port 6379: netstat -an | grep 6379
```

### JWT Secret Too Short

```
Problem: JWT processing fails during login
Solution:
export JWT_SECRET=$(openssl rand -base64 32)  # Generate new 256-bit secret
```

### Port Already in Use

```
Problem: Port 8080 already in use
Solution:
1. Find process: lsof -i :8080
2. Kill process: kill -9 <PID>
3. Or use different port: mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am "Add feature"`
3. Push branch: `git push origin feature/your-feature`
4. Create Pull Request with tests

### Code Standards

- Follow Google Java Style Guide
- Maintain 80%+ test coverage
- Add Javadoc for public methods
- No code smells (SonarQube clean)

---

## 📄 License

Apache License 2.0 - See LICENSE file for details

---

## 📧 Support

For issues, questions, or suggestions:
- **Email:** support@attendance-system.com
- **Documentation:** `docs/` folder
- **Issue Tracker:** GitHub Issues

---

## 🎯 Roadmap

### Phase 1 (Current)
✅ Authentication and authorization
✅ Student management
✅ Attendance tracking
✅ Fee management
✅ Reports and gamification

### Phase 2 (Q3 2026)
⏳ SMS notifications
⏳ Mobile app (Android/iOS)
⏳ Advanced analytics
⏳ Payment gateway integration

### Phase 3 (Q4 2026)
⏳ Microservices architecture
⏳ Real-time notifications (WebSocket)
⏳ Multi-tenant support
⏳ Advanced permissions

---

## 📊 Performance Targets

- API Response Time: < 200ms (p95)
- Database Query: < 100ms
- Throughput: 1000 req/sec
- Uptime: 99.5%
- Error Rate: < 0.1%

---

**Version:** 1.0.0  
**Last Updated:** 2026-06-15  
**Status:** Production Ready ✅
