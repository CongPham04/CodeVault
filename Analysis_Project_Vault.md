# CodeVault - Project Analysis

## 1. Project Overview & Purpose

**CodeVault** is a RESTful Spring Boot backend service for storing and managing source code repositories without authentication or version control. It acts as a lightweight, file-based source code storage server focused exclusively on repository CRUD operations, ZIP-based upload/download, and file listing capabilities. The system stores repository metadata in MySQL and actual source code files in an external file system directory.

---

## 2. Complete File & Directory Structure

```
D:\MyProject\CodeVault/
├── pom.xml                                          # Maven project configuration
├── mvnw / mvnw.cmd                                  # Maven wrapper for cross-platform builds
├── Dockerfile                                       # Multi-stage Docker build (Builder + Runtime)
├── docker-compose.yml                               # Docker composition for full stack (MySQL + App)
├── .dockerignore                                    # Docker build exclusions
├── .gitattributes / .gitignore                      # Git configuration
├── HELP.md                                          # Spring Boot help reference
├── decription_project.md                            # Project overview document
├── plan_project.md                                  # Comprehensive v1.0 backend plan (1300+ lines)
├── plan_project_fe.md                               # Comprehensive v1.0 frontend plan (1400+ lines)
│
├── .mvn/wrapper/
│   └── maven-wrapper.properties                     # Maven wrapper configuration
│
├── .vscode/
│   └── settings.json                                # VS Code editor settings
│
├── src/
│   ├── main/
│   │   ├── java/com/build/CodeVault/
│   │   │   ├── CodeVaultApplication.java            # Spring Boot entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── StorageConfig.java               # Type-safe config binding (storage.location)
│   │   │   │   └── SwaggerConfig.java               # OpenAPI/Swagger UI configuration
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   └── Repository.java                  # JPA entity (repositories table)
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java                 # Generic response wrapper <T>
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateRepositoryRequest.java # DTO for create request
│   │   │   │   └── response/
│   │   │   │       ├── RepositoryResponse.java      # DTO for repository metadata
│   │   │   │       ├── FileEntry.java               # DTO for file tree entry
│   │   │   │       └── ErrorResponse.java           # DTO for structured error responses
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java      # @ControllerAdvice with 6 handlers
│   │   │   │   ├── RepositoryNotFoundException.java
│   │   │   │   ├── RepositoryAlreadyExistsException.java
│   │   │   │   ├── InvalidFileFormatException.java
│   │   │   │   └── StorageException.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── RepositoryService.java           # Business logic interface
│   │   │   │   ├── RepositoryServiceImpl.java       # Implementation (orchestrates JPA + FileStorage)
│   │   │   │   ├── FileStorageService.java          # File operations interface
│   │   │   │   └── FileStorageServiceImpl.java      # Local file system implementation
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   └── RepositoryJpaRepository.java     # Spring Data JPA repository
│   │   │   │
│   │   │   └── controller/
│   │   │       └── RepositoryController.java        # REST API endpoints with Swagger annotations
│   │   │
│   │   └── resources/
│   │       └── application.yaml                     # Spring Boot configuration
│   │
│   └── test/
│       └── java/com/build/CodeVault/
│           └── CodeVaultApplicationTests.java       # Basic Spring Boot test
│
└── target/                                          # Build artifacts
```

---

## 3. Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.3 |
| ORM | Spring Data JPA / Hibernate | - |
| Database | MySQL | 8.x+ |
| API Docs | SpringDoc OpenAPI (Swagger UI) | 2.8.6 |
| File Utils | Apache Commons IO | 2.18.0 |
| Code Gen | Lombok | - |
| Build Tool | Apache Maven | 3.9+ |
| Containerization | Docker + Docker Compose | - |
| Testing | JUnit 5 + Spring Boot Test | - |
| Dev Tools | Spring Boot DevTools | - |

---

## 4. Architecture & Design Patterns

### Layered Architecture

```
Controller Layer (HTTP requests / REST endpoints)
                ↓
Service Layer (Business logic / orchestration)
       ↙              ↘
JPA Repository     FileStorageService
(Database CRUD)       (File I/O)
       ↓                  ↓
MySQL Database     External File System
```

### Design Patterns Implemented

1. **Repository Pattern** - JPA repository abstraction for database operations
2. **Service-Oriented Architecture** - Clean separation between business logic and persistence
3. **DTO Pattern** - ApiResponse<T>, RepositoryResponse, CreateRepositoryRequest decouple API from entities
4. **Dependency Injection** - Constructor injection (@RequiredArgsConstructor) for loose coupling
5. **Factory Methods** - ApiResponse.success(), .created(), .error() static factories
6. **Global Exception Handling** - @ControllerAdvice for consistent error responses
7. **Configuration Properties** - StorageConfig for type-safe property binding
8. **Interface-Based Services** - FileStorageService & RepositoryService interfaces enable implementation swapping
9. **Transactional Consistency** - @Transactional for database operations
10. **Zip Slip Protection** - Path normalization & validation to prevent directory traversal attacks

---

## 5. Key Components & Their Roles

| Component | File | Responsibility |
|-----------|------|---------------|
| **Controller** | RepositoryController.java | 7 REST endpoints: CRUD, upload, download, file listing |
| **Service Layer** | RepositoryServiceImpl.java | Orchestrates JPA + FileStorageService; validates unique names |
| **File Storage** | FileStorageServiceImpl.java | All file operations: create/delete dirs, zip/unzip, size calculation |
| **Entity** | Repository.java | JPA mapping for `repositories` table |
| **DTOs** | ApiResponse, RepositoryResponse, FileEntry, etc. | Request/response data transfer objects |
| **Exception Handler** | GlobalExceptionHandler.java | Maps custom exceptions to HTTP status codes |
| **Config** | StorageConfig.java, SwaggerConfig.java | Storage path binding; OpenAPI configuration |
| **JPA Repo** | RepositoryJpaRepository.java | Custom queries: findByName(), existsByName() |

---

## 6. API Endpoints

| Method | Path | Action | Status Code |
|--------|------|--------|-------------|
| POST | /api/repos | Create repository | 201 Created |
| GET | /api/repos | List all repositories | 200 OK |
| GET | /api/repos/{id} | Get single repository | 200 OK |
| DELETE | /api/repos/{id} | Delete repo & files | 204 No Content |
| POST | /api/repos/{id}/upload | Upload ZIP source code | 200 OK |
| GET | /api/repos/{id}/download | Download as ZIP | 200 OK (stream) |
| GET | /api/repos/{id}/files | List files recursively | 200 OK |

---

## 7. Database Design

### Single Table: `repositories`

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique repository ID |
| name | VARCHAR(150) | NOT NULL, UNIQUE | Human-readable name |
| description | TEXT | NULLABLE | Optional description |
| folder_path | VARCHAR(255) | NOT NULL | Path to physical storage |
| size_in_bytes | BIGINT | DEFAULT 0 | Total size (updated on upload) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | DATETIME | NULLABLE | Last upload time |

---

## 8. Request Flow (Example: Upload Source Code)

```
1. User POST /api/repos/{id}/upload (multipart ZIP)
                    ↓
2. RepositoryController.uploadSourceCode()
   → Validates input (file parameter)
   → Calls repositoryService.uploadSourceCode(id, file)
                    ↓
3. RepositoryServiceImpl.uploadSourceCode()
   → Finds repository by ID via JPA
   → Gets repository folderPath
   → Calls fileStorageService.deleteDirectoryRecursively(repoPath)
   → Calls fileStorageService.unzipFileSafely(file, repoPath)
                    ↓
4. FileStorageServiceImpl.unzipFileSafely()
   → Validates .zip extension
   → Iterates through ZIP entries
   → Normalizes paths + checks for Zip Slip attack
   → Creates directories and extracts files safely
                    ↓
5. Back in RepositoryServiceImpl
   → Calculates new sizeInBytes
   → Updates repository metadata
   → Persists to database via jpaRepository.save()
                    ↓
6. Returns ResponseEntity with ApiResponse<RepositoryResponse>
```

---

## 9. Configuration & Environment Variables

### application.yaml Defaults (overridable via environment)

```yaml
spring.datasource.url:      jdbc:mysql://localhost:3306/codevault_db
spring.datasource.username:  root (via DB_USERNAME)
spring.datasource.password:  123456 (via DB_PASSWORD)
storage.location:            D:/codevault-storage (via STORAGE_LOCATION)
server.port:                 8080
multipart.max-file-size:     100MB
multipart.max-request-size:  100MB
jpa.ddl-auto:                update (auto schema creation)
```

### Docker-Compose Overrides

- DB_HOST=mysql-db (container service name)
- STORAGE_LOCATION=/var/codevault-storage (container volume)

---

## 10. Security Features

1. **Zip Slip Vulnerability Protection** - Path normalization + validation in unzipFileSafely()
2. **Input Validation** - @NotBlank, @Size annotations on DTOs
3. **Unique Repository Names** - Prevents naming collisions at database level
4. **Directory Traversal Prevention** - Path.normalize() + startsWith() checks
5. **Multipart Size Limits** - 100MB limits prevent denial-of-service
6. **Absolute Path Rejection** - ZIP entries with absolute paths are rejected

---

## 11. Deployment

### Docker Setup (Included)

- **Multi-stage Dockerfile**: Builder stage (JDK 21 + Maven) -> Runtime stage (JRE 21, minimal image)
- **Health check**: `curl -f http://localhost:8080/api/repos || exit 1`
- **docker-compose.yml**: MySQL 8.0 + Spring Boot app with named volumes
- **Storage persistence**: Docker volume `codevault_storage`

### Build & Run Commands

```bash
# Build
./mvnw clean package -DskipTests

# Run JAR
java -jar target/CodeVault-0.0.1-SNAPSHOT.jar

# Docker
docker-compose up --build
```

---

## 12. Dependencies Summary (pom.xml)

| Dependency | Purpose |
|-----------|---------|
| spring-boot-starter-data-jpa | ORM / Hibernate integration |
| spring-boot-starter-validation | Bean validation (Jakarta) |
| spring-boot-starter-webmvc | Spring MVC for REST APIs |
| commons-io:2.18.0 | File utilities (delete, size calculation) |
| springdoc-openapi-starter-webmvc-ui:2.8.6 | Swagger / OpenAPI 3 auto-docs |
| mysql-connector-j | MySQL JDBC driver |
| lombok | Annotation-based boilerplate reduction |
| spring-boot-devtools | Hot reload in development |
| spring-boot-starter-test | JUnit 5, Mockito |

---

## 13. Strengths

- Well-structured layered architecture with clean separation of concerns
- Interface-driven services enabling easy swapping (e.g., local storage to S3)
- Comprehensive exception handling with custom exception hierarchy
- Swagger/OpenAPI documentation auto-generated and ready to use
- Docker + Docker Compose ready for containerized deployment
- Zip Slip security protection implemented
- Generic ApiResponse<T> wrapper for consistent API responses
- Transactional boundaries properly defined
- Extensive Javadoc documentation throughout the codebase
- Configuration externalized via environment variables

---

## 14. Areas for Future Enhancement

1. **Authentication & Authorization** - No auth in v1.0; can add Spring Security
2. **Version Control** - Currently replaces entire repo on upload; no version history
3. **Multi-Tenancy** - Single database, shared storage; user namespaces needed
4. **Unit/Integration Tests** - Only basic context test exists; needs comprehensive test coverage
5. **Rate Limiting** - No request throttling
6. **Search Functionality** - No code/repository search capability
7. **Frontend Application** - Plan exists (plan_project_fe.md) for React + Vite frontend
8. **Cloud Storage** - Interface-based design allows migration to S3/MinIO
9. **CI/CD Pipeline** - No automated build/deploy pipeline configured
10. **Monitoring & Logging** - No structured logging or metrics collection

---

## 15. Summary

| Aspect | Details |
|--------|---------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.3 |
| **Database** | MySQL 8.x (1 table: repositories) |
| **Storage** | External file system (configurable path) |
| **API Style** | REST + JSON |
| **Documentation** | Swagger UI + OpenAPI 3 |
| **Containerization** | Docker + Docker Compose |
| **Authentication** | None (v1.0) |
| **Versioning** | Single version per repo (replace on upload) |
| **File Handling** | ZIP-only format |
| **Primary Pattern** | Layered + Service-Oriented |
| **Key Security** | Zip Slip protection, path validation, input validation |
| **Extensibility** | Interface-driven services, DTO layer, external config |

---

*Analysis generated on: 2026-03-23*
