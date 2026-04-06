# Finance Dashboard Backend

A Spring Boot REST API backend for a role-based finance dashboard system.
Supports financial record management, role-based access control, dashboard analytics, record filtering, and JWT authentication.

---

## Tech Stack

| Layer         | Technology                          |
|---------------|-------------------------------------|
| Language      | Java 17                             |
| Framework     | Spring Boot 3.2                     |
| Security      | Spring Security + JWT (jjwt 0.11.5) |
| Database      | MySQL 8.x                           |
| ORM           | Spring Data JPA / Hibernate         |
| Validation    | Jakarta Bean Validation             |
| Build         | Maven                               |
| Utilities     | Lombok                              |

---

## Project Folder Structure

```
finance-dashboard/
├── pom.xml                                          # Maven dependencies
└── src/
    ├── main/
    │   ├── java/com/finance/dashboard/
    │   │   │
    │   │   ├── FinanceDashboardApplication.java     # Entry point (@SpringBootApplication)
    │   │   │
    │   │   ├── config/                              # Spring configuration beans
    │   │   │   ├── SecurityConfig.java              # Filter chain, route permissions, CORS
    │   │   │   └── DataSeeder.java                  # Seeds default admin/analyst/viewer users on startup
    │   │   │
    │   │   ├── controller/                          # REST controllers (HTTP layer)
    │   │   │   ├── AuthController.java              # POST /api/auth/login
    │   │   │   ├── UserController.java              # CRUD for users (ADMIN only)
    │   │   │   ├── FinancialRecordController.java   # CRUD + filtering for records
    │   │   │   └── DashboardController.java         # GET /api/dashboard/summary
    │   │   │
    │   │   ├── dto/                                 # Data Transfer Objects (request/response shapes)
    │   │   │   ├── LoginRequest.java                # { username, password }
    │   │   │   ├── AuthResponse.java                # { token, username, role }
    │   │   │   ├── CreateUserRequest.java           # { username, email, password, role }
    │   │   │   ├── UpdateUserRequest.java           # { email?, role?, active? }
    │   │   │   ├── UserResponse.java                # Safe user shape (no password)
    │   │   │   ├── CreateRecordRequest.java         # { amount, type, category, date, notes }
    │   │   │   ├── UpdateRecordRequest.java         # All fields optional (partial update)
    │   │   │   ├── RecordResponse.java              # Record shape returned to client
    │   │   │   ├── RecordFilterRequest.java         # Filter + pagination params bundle
    │   │   │   ├── DashboardSummary.java            # Full dashboard payload
    │   │   │   ├── MonthlyTrendItem.java            # { year, month, income, expenses, net }
    │   │   │   ├── ApiResponse.java                 # Generic wrapper { success, message, data }
    │   │   │   └── PagedResponse.java               # Paginated result wrapper
    │   │   │
    │   │   ├── entity/                              # JPA entities (database tables)
    │   │   │   ├── User.java                        # users table
    │   │   │   └── FinancialRecord.java             # financial_records table
    │   │   │
    │   │   ├── enums/                               # Enum types
    │   │   │   ├── Role.java                        # VIEWER | ANALYST | ADMIN
    │   │   │   └── TransactionType.java             # INCOME | EXPENSE
    │   │   │
    │   │   ├── exception/                           # Error handling
    │   │   │   ├── GlobalExceptionHandler.java      # @RestControllerAdvice — catches all exceptions
    │   │   │   ├── ResourceNotFoundException.java   # 404
    │   │   │   ├── AccessDeniedException.java       # 403
    │   │   │   └── DuplicateResourceException.java  # 409
    │   │   │
    │   │   ├── repository/                          # Spring Data JPA repositories (DB queries)
    │   │   │   ├── UserRepository.java              # CRUD + findByUsername/Email/Role
    │   │   │   └── FinancialRecordRepository.java   # CRUD + aggregate queries + JpaSpecificationExecutor
    │   │   │
    │   │   ├── security/                            # JWT and Spring Security
    │   │   │   ├── JwtUtil.java                     # Generate / validate / parse JWT tokens
    │   │   │   ├── JwtAuthFilter.java               # OncePerRequestFilter — reads Bearer token
    │   │   │   └── CustomUserDetailsService.java    # Loads user from DB for Spring Security
    │   │   │
    │   │   ├── service/                             # Business logic layer
    │   │   │   ├── AuthService.java                 # Login → authenticate → issue JWT
    │   │   │   ├── UserService.java                 # User CRUD, role assignment, deactivation
    │   │   │   ├── FinancialRecordService.java      # Record CRUD, dynamic filtering, soft delete
    │   │   │   └── DashboardService.java            # Aggregate totals, trends, recent activity
    │   │   │
    │   │   └── util/
    │   │       └── FinancialRecordSpecification.java # JPA Specification for dynamic WHERE clauses
    │   │
    │   └── resources/
    │       └── application.properties               # DB URL, JWT secret, JPA config
    │
    └── test/
        └── java/com/finance/dashboard/
            └── FinancialRecordServiceTest.java      # Unit tests for record service
```

---

## What Each Layer Does

### `entity/` — Database Tables
JPA-mapped classes that become MySQL tables via Hibernate.
- **User** → `users` table. Stores credentials, role, active flag.
- **FinancialRecord** → `financial_records` table. Has soft-delete (`deleted` flag + `deletedAt`).

### `repository/` — Data Access
Spring Data JPA interfaces. No SQL boilerplate needed for standard CRUD.
- Custom `@Query` methods for aggregates (SUM, GROUP BY) used by the dashboard.
- `JpaSpecificationExecutor` on `FinancialRecordRepository` enables dynamic filter queries.

### `util/FinancialRecordSpecification` — Dynamic Filtering
Builds a JPA `Specification` (SQL WHERE clause) at runtime based on whichever filter parameters were passed in. Null parameters are simply skipped — no empty `AND` clauses.

### `service/` — Business Logic
All logic lives here, not in controllers.
- **AuthService** — delegates to `AuthenticationManager`, then issues a JWT.
- **UserService** — enforces uniqueness, hashes passwords, manages roles/status.
- **FinancialRecordService** — wires together the Specification + pageable for filtered listing; resolves the current user from SecurityContext for record ownership.
- **DashboardService** — calls aggregate repository queries and assembles a single summary response, including monthly trend grouping by year/month.

### `controller/` — HTTP Interface
Thin layer: validates input (`@Valid`), calls service, wraps result in `ApiResponse`.
- Route-level access control declared with `@PreAuthorize`.

### `security/` — Auth & JWT
- `JwtUtil` creates and parses HS256-signed tokens.
- `JwtAuthFilter` runs on every request, reads `Authorization: Bearer <token>`, validates it, and populates the `SecurityContextHolder`.
- `CustomUserDetailsService` loads the user from DB and maps role → Spring `GrantedAuthority` (`ROLE_ADMIN`, `ROLE_ANALYST`, etc.).

### `dto/` — API Contracts
Separate request and response objects keep entities private and allow independent validation rules. `ApiResponse<T>` wraps every response with `success`, `message`, and `data`.

### `exception/` — Error Handling
`GlobalExceptionHandler` catches all exception types and converts them to structured JSON with the correct HTTP status. No try/catch needed in controllers or services.

### `config/` — Spring Config
- `SecurityConfig` defines the filter chain and declares which roles can hit which routes.
- `DataSeeder` runs once at startup and creates default users if they don't exist.

---

## Database Setup

### 1. Create the database
```sql
CREATE DATABASE finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'financeuser'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON finance_db.* TO 'financeuser'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Update `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root          # change to your MySQL username
spring.datasource.password=your_password # change to your MySQL password
```

> `spring.jpa.hibernate.ddl-auto=update` means Hibernate will auto-create/update tables on startup. No migration scripts needed for development.

---

## Running the Application

```bash
# 1. Clone / extract the project
cd finance-dashboard

# 2. Build
mvn clean package -DskipTests

# 3. Run
mvn spring-boot:run
# OR
java -jar target/dashboard-1.0.0.jar
```

The server starts on **http://localhost:8080**

---

## Default Users (seeded on startup)

| Username | Password    | Role    |
|----------|-------------|---------|
| admin    | admin123    | ADMIN   |
| analyst  | analyst123  | ANALYST |
| viewer   | viewer123   | VIEWER  |

---

## API Reference

### Authentication

#### `POST /api/auth/login`
```json
// Request
{ "username": "admin", "password": "admin123" }

// Response 200
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

Use the token in all subsequent requests:
```
Authorization: Bearer <token>
```

---

### Users — ADMIN only

| Method | Endpoint                      | Description                    |
|--------|-------------------------------|--------------------------------|
| POST   | /api/users                    | Create user                    |
| GET    | /api/users                    | List all users (filter by role)|
| GET    | /api/users/{id}               | Get user by ID                 |
| PUT    | /api/users/{id}               | Update email / role / status   |
| PATCH  | /api/users/{id}/deactivate    | Soft-deactivate user           |
| DELETE | /api/users/{id}               | Permanently delete user        |

**Create user request:**
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "role": "ANALYST"
}
```

---

### Financial Records

| Method | Endpoint           | Roles Allowed       | Description                         |
|--------|--------------------|---------------------|-------------------------------------|
| POST   | /api/records       | ADMIN               | Create record                       |
| GET    | /api/records       | ANALYST, ADMIN      | List with filters + pagination      |
| GET    | /api/records/{id}  | ANALYST, ADMIN      | Get by ID                           |
| PUT    | /api/records/{id}  | ADMIN               | Update record                       |
| DELETE | /api/records/{id}  | ADMIN               | Soft-delete record                  |

**Create record request:**
```json
{
  "amount": 3500.00,
  "type": "INCOME",
  "category": "Salary",
  "transactionDate": "2024-06-01",
  "notes": "June salary"
}
```

**Filtering — GET /api/records**

All query parameters are optional and combinable:

```
GET /api/records?type=EXPENSE&category=food&dateFrom=2024-01-01&dateTo=2024-06-30&amountMin=100&amountMax=5000&page=0&size=20&sortBy=transactionDate&sortDir=desc
```

| Param      | Type          | Example            | Description                          |
|------------|---------------|--------------------|--------------------------------------|
| type       | INCOME/EXPENSE| EXPENSE            | Filter by transaction type           |
| category   | string        | food               | Partial, case-insensitive match      |
| dateFrom   | yyyy-MM-dd    | 2024-01-01         | Records on or after this date        |
| dateTo     | yyyy-MM-dd    | 2024-06-30         | Records on or before this date       |
| amountMin  | decimal       | 100.00             | Minimum amount                       |
| amountMax  | decimal       | 5000.00            | Maximum amount                       |
| page       | int           | 0                  | Page number (zero-indexed)           |
| size       | int           | 20                 | Records per page                     |
| sortBy     | string        | amount             | transactionDate, amount, category    |
| sortDir    | asc/desc      | asc                | Sort direction                       |

---

### Dashboard

| Method | Endpoint                   | Roles Allowed              | Description         |
|--------|----------------------------|----------------------------|---------------------|
| GET    | /api/dashboard/summary     | VIEWER, ANALYST, ADMIN     | Full summary        |

**Response:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 15000.00,
    "totalExpenses": 8500.00,
    "netBalance": 6500.00,
    "totalRecords": 42,
    "categoryTotals": { "Salary": 12000.00, "Rent": 4000.00 },
    "incomeCategoryTotals": { "Salary": 12000.00, "Freelance": 3000.00 },
    "expenseCategoryTotals": { "Rent": 4000.00, "Food": 1500.00 },
    "monthlyTrend": [
      { "year": 2024, "month": 1, "monthLabel": "Jan 2024",
        "income": 5000.00, "expenses": 2000.00, "net": 3000.00 }
    ],
    "recentActivity": [ ... ]
  }
}
```

---

## Role Permission Matrix

| Endpoint                         | VIEWER | ANALYST | ADMIN |
|----------------------------------|--------|---------|-------|
| POST   /api/auth/login           | ✅     | ✅      | ✅    |
| GET    /api/dashboard/summary    | ✅     | ✅      | ✅    |
| GET    /api/records              | ❌     | ✅      | ✅    |
| GET    /api/records/{id}         | ❌     | ✅      | ✅    |
| POST   /api/records              | ❌     | ❌      | ✅    |
| PUT    /api/records/{id}         | ❌     | ❌      | ✅    |
| DELETE /api/records/{id}         | ❌     | ❌      | ✅    |
| GET    /api/users                | ❌     | ❌      | ✅    |
| POST   /api/users                | ❌     | ❌      | ✅    |
| PUT    /api/users/{id}           | ❌     | ❌      | ✅    |
| DELETE /api/users/{id}           | ❌     | ❌      | ✅    |

---

## Validation Rules

| Field           | Rules                                            |
|-----------------|--------------------------------------------------|
| username        | Required, 3–50 chars                             |
| email           | Required, valid email format                     |
| password        | Required, min 6 chars                            |
| amount          | Required, > 0.00, max 13 integer digits, 2 decimal|
| type            | Required, must be INCOME or EXPENSE              |
| category        | Required, max 100 chars                          |
| transactionDate | Required, cannot be in the future                |
| notes           | Optional, max 500 chars                          |

Validation errors return HTTP **400** with a field-level error map:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "Amount must be greater than 0",
    "category": "Category is required"
  }
}
```

---

## Error Response Format

All errors follow the same shape:
```json
{ "success": false, "message": "Error description here", "data": null }
```

| Status | Meaning               |
|--------|-----------------------|
| 400    | Validation error      |
| 401    | Invalid credentials   |
| 403    | Insufficient role     |
| 404    | Resource not found    |
| 409    | Duplicate username/email |
| 500    | Unexpected server error |

---

## Design Decisions & Assumptions

1. **Soft delete** — Records are never permanently deleted; a `deleted` flag is set. This preserves dashboard history accuracy.
2. **Dynamic filtering** — JPA Specification pattern avoids N+1 method combinations for filtering. Only non-null parameters become SQL predicates.
3. **JWT stateless auth** — No session storage. Token expiry is 24 hours (configurable via `app.jwt.expiration-ms`).
4. **Password storage** — BCrypt with default strength (10 rounds).
5. **Pagination** — All list endpoints are paginated to prevent large response payloads.
6. **Category** is a free-text string, not a foreign key, to keep the schema simple and flexible.
7. **DataSeeder** creates default users only if they don't exist, so it's safe to run repeatedly.

---

## Running Tests

```bash
mvn test
```
