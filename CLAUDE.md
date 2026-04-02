# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

**Vantix HRM** is a full-stack Human Resource Management System with a Spring Boot backend and Vue.js frontend.

- **Backend**: Java 17, Spring Boot 4.0.3 (WAR packaging), MySQL, JWT authentication
- **Frontend**: Vue 3 + Vite, Bootstrap 5, Pinia state management
- **Architecture**: REST API with stateless JWT authentication, role-based access control (RBAC)

---

## Repository Structure

```
Vantix---HRM/
├── Vantix_HRM/                 # Spring Boot backend
│   ├── src/main/java/poly/edu/vantix_hrm/
│   │   ├── config/            # Spring configurations (Security, JPA audit)
│   │   ├── controller/        # REST controllers
│   │   ├── dto/               # Data transfer objects (organized by module)
│   │   ├── entity/            # JPA entities (all extend BaseEntity)
│   │   ├── exception/         # Global exception handling
│   │   ├── repository/        # Spring Data JPA repositories
│   │   ├── security/          # JWT filter, service, principal
│   │   ├── service/           # Business logic services
│   │   └── utils/             # Utilities (specifications, pagination)
│   └── src/main/resources/
│       └── application.properties
│
├── Vantix_Web/                 # Vue.js frontend
│   ├── src/
│   │   ├── components/        # Reusable Vue components
│   │   ├── config/            # Configuration files
│   │   ├── layouts/           # App layouts (AuthLayout, MainLayout)
│   │   ├── router/            # Vue Router configuration
│   │   ├── services/          # API service modules (axios wrapper + endpoints)
│   │   ├── stores/            # Pinia stores (auth, etc.)
│   │   ├── utils/             # Frontend utilities
│   │   └── views/             # Page components (organized by feature)
│   └── vite.config.js         # Vite configuration with proxy to backend
│
├── Vantix_Database.sql         # Database schema (SQL Server/MySQL)
└── README.md                   # Project overview (Vietnamese)
```

---

## Key Architecture Patterns

### Backend
- **Layered Architecture**: Controller → Service → Repository → Database
- **Stateless JWT Authentication**: All APIs (except `/api/auth/*`) require Bearer token
- **Soft Deletes**: All entities extend `BaseEntity` with `deleted` flag, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- **RBAC**: Users have Roles, Roles have Permissions. Use `@PreAuthorize("hasRole('ADMIN')")` or similar
- **Auditing**: Automatic population of audit fields via `JpaAuditConfig` + `AuditorAwareImpl`
- **Global Exception Handling**: `GlobalExceptionHandler` converts exceptions to standardized `ErrorResponse`
- **DTO Pattern**: Separate request/response DTOs from entities (no entity exposure in APIs)
- **Pagination**: Use `PageRequestDTO` and `PageResponseDTO` for list endpoints
- **MapStruct**: Version 1.6.0 configured for mapping (check service implementations)

### Frontend
- **Axios Wrapper**: `src/services/axios.js` auto-attaches JWT token & handles 401 redirects
- **Route Guards**: Navigation guard checks auth token & permissions
- **Layout System**: `AuthLayout` for login pages, `MainLayout` for authenticated pages
- **Service Modules**: One service file per domain (e.g., `user.service.js`, `employee.service.js`)
- **State Management**: Pinia stores (e.g., `auth.store.js`) for global state

---

## Common Development Commands

### Backend (Vantix_HRM)
```bash
# Build the project
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Run the application (from Vantix_HRM directory)
mvn spring-boot:run

# Run with debugging enabled
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=VantixHrmApplicationTests

# Run a single test method
mvn test -Dtest=VantixHrmApplicationTests#testMethodName

# Format code (if using spotless/formatter plugin)
mvn spotless:apply

# Clean target directory
mvn clean
```

### Frontend (Vantix_Web)
```bash
# Navigate to frontend directory
cd Vantix_Web

# Install dependencies
npm install

# Start development server (runs on http://localhost:5173, proxies /api to backend)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint (if configured)
npm run lint
```

---

## Database Configuration

- Default config in `application.properties`: MySQL at `localhost:3306/Vantix_HRM`
- To change: edit `Vantix_HRM/src/main/resources/application.properties`
- DDL auto: `update` (Hibernate auto-updates schema)
- To reset database: drop the database, recreate, then restart app (or change `ddl-auto` to `create-drop` temporarily)
- Schema available in `Vantix_Database.sql` (SQL Server syntax, may need adjustments for MySQL)

---

## API Conventions

- **Base URL**: `/api/*`
- **Authentication**: `Authorization: Bearer <jwt_token>` header
- **Success Response**: `{ data: ... }` or direct body (check individual controllers)
- **Error Response**: `{ message: string, errors?: { field: msg } }` (handled by `GlobalExceptionHandler`)
- **Pagination**: Accepts `page`, `size`, `sort` parameters. Returns `PageResponseDTO` with `content`, `page`, `size`, `totalElements`, `totalPages`
- **CRUD Standard**: 
  - `GET /resource` → list (paginated)
  - `GET /resource/{id}` → get one
  - `POST /resource` → create
  - `PUT /resource/{id}` → update
  - `DELETE /resource/{id}` → soft delete (sets `deleted=true`)

---

## Important Notes

1. **WAR Packaging**: Backend is configured to deploy as WAR to external Tomcat. For standalone running, Spring Boot embedded Tomcat is used (provided scope).
2. **Stateless by Design**: No HTTP sessions. All state in JWT or database.
3. **CORS**: If enabling frontend on different port, configure CORS in `SecurityConfig`.
4. **Email**: Mail config is hardcoded in `application.properties`. Consider using environment variables for production.
5. **JWT Secret**: Currently hardcoded. Use environment variable `JWT_SECRET` in production.
6. **Soft Delete Pattern**: All repositories should include `findByDeletedFalse()` methods. Queries automatically filter `deleted = false` if using `@SQLDelete`/`@Where` (check entity annotations).
7. **Fetch Type**: Entities use `LAZY` fetching for `@ManyToOne` and `@OneToOne` to avoid N+1. Use `@EntityGraph` or explicit joins in repository when needed.
8. **Role-Permission Model**: Permissions are granular (e.g., `USER_VIEW`, `USER_CREATE`). Controllers/services should check permissions using `@PreAuthorize("hasPermission('USER_VIEW')")`.

---

## Testing Strategy

- Backend tests use Spring Boot Test (JUnit 5)
- Place tests in `src/test/java/` mirroring package structure
- Use `@SpringBootTest` for integration tests
- For unit tests (service layer), mock repositories with Mockito
- Example: `VantixHrmApplicationTests.java` exists as a basic test template

---

## Environment Setup

1. **Prerequisites**:
   - Java 17+
   - Maven 3.8+
   - Node.js 18+ (for frontend)
   - MySQL or SQL Server (database)

2. **Database Setup**:
   - Create database: `Vantix_HRM`
   - Update credentials in `application.properties`
   - Run `Vantix_Database.sql` to create tables (or let Hibernate generate)

3. **Running Full Stack**:
   - Terminal 1: `cd Vantix_HRM && mvn spring-boot:run` (backend on port 8080)
   - Terminal 2: `cd Vantix_Web && npm install && npm run dev` (frontend on port 5173)
   - Open browser to `http://localhost:5173`

---

## Code Style Guidelines

- **Java**: Use Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`). Follow standard Spring Boot conventions.
- **Vue**: Use `<script setup>` SFCs. Follow Bootstrap 5 classes for styling.
- **Naming**: 
  - Java: `camelCase` for methods/variables, `PascalCase` for classes
  - Vue: `PascalCase` for components, `camelCase` for props/events
  - Entities: Table names snake_case (e.g., `users`, `employee_profiles`)
- **Package Structure**: Keep to existing structure (controller/service/repository/dto/entity).
- **API Endpoints**: Use plural nouns (`/api/users`, `/api/employees`, `/api/departments`)

---

## Troubleshooting

- **401 Unauthorized**: Check JWT token in localStorage, ensure backend is running
- **CORS Errors**: Backend proxy in Vite config handles dev CORS. For production, configure `CorsConfigurationSource` bean.
- **LazyInitializationException**: Entity lazy load outside transaction. Use `@Transactional` in service or fetch joins in repository.
- **Mail Sending Fails**: Check `application.properties` mail credentials (currently using Gmail, may require app password).
- **Port Conflicts**: Backend default 8080, frontend 5173. Change in `application.properties` or Vite config.

---

## Security Considerations

- Never commit real credentials. Use environment variables in production.
- JWT secret should be strong and kept secret.
- Passwords stored with BCrypt (cost factor 10-12).
- All user inputs validated with Jakarta Validation (`@Valid`).
- SQL Injection protected via JPA/Hibernate (use parameterized queries).

---
