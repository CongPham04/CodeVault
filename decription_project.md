Project Title

CodeVault – Source Code Storage Server (No Authentication, No Versioning)

1. Project Overview

CodeVault is a RESTful backend service built with Spring Boot that allows users to store and manage source code repositories in a simplified way.

The system does NOT include:

Authentication

User management

Version control

Git integration

Each repository stores only one current version of source code.
When a new source archive is uploaded, it replaces the previous content.

The system stores:

Repository metadata in a relational database (MySQL)

Source code files in an external file system directory (not inside the project source)

2. Technical Stack

Backend:

Java 17 or 21

Spring Boot 3.x

Spring Web

Spring Data JPA

MySQL

Lombok (optional)

Apache Commons IO (recommended for file operations)

Not included:

Spring Security

JWT

Docker (initial version)

Git libraries

3. System Architecture

The application follows a layered architecture:

Controller Layer
→ Service Layer
→ Repository Layer (JPA)
→ FileStorageService
→ Database + File System

Separation of concerns:

Controllers handle HTTP requests.

Services handle business logic.

Repository layer handles database operations.

FileStorageService handles file system operations.

Database stores metadata only.

File system stores actual source code.

4. Database Design

The system uses a single table:

Table: repositories

Fields:

id (BIGINT, Primary Key, Auto Increment)

name (VARCHAR 150, NOT NULL, UNIQUE)

description (TEXT)

folder_path (VARCHAR 255, NOT NULL)

size_in_bytes (BIGINT, DEFAULT 0)

created_at (DATETIME, DEFAULT CURRENT_TIMESTAMP)

updated_at (DATETIME, nullable)

Purpose:

Store metadata of repositories.

Do NOT store any file content in the database.

The folder_path field points to the physical storage location.

5. File Storage Design

Source code must NOT be stored inside the Spring Boot project directory.

Instead, it must be stored in an external directory configured in application.yml:

Example:

Windows:
D:/codevault-storage/

Linux:
/var/codevault-storage/

Configuration example:

storage.location=/external/path

Folder Structure

<storage-location>/
└── repos/
└── {repoId}/
├── src/
├── pom.xml
└── ...

Each repository has its own folder identified by repoId.

When uploading new source code:

Delete existing repository folder content.

Extract uploaded ZIP file into that folder.

Update repository metadata (size, updated_at).

6. REST API Design

Base path: /api

6.1 Repository Management
1. Create Repository

POST /api/repos

Request body:
{
"name": "my-project",
"description": "Spring Boot application"
}

Process:

Validate unique repository name.

Insert record into database.

Create physical folder using repoId.

Store folder path in database.

Return repository metadata.

2. Get All Repositories

GET /api/repos

Returns a list of repository metadata.

3. Get Repository Details

GET /api/repos/{id}

Returns metadata of a single repository.

4. Delete Repository

DELETE /api/repos/{id}

Process:

Delete physical folder recursively.

Delete database record.

6.2 Upload Source Code

POST /api/repos/{id}/upload

Request:

Multipart file (.zip only)

Process:

Validate repository exists.

Validate uploaded file is ZIP format.

Delete existing repository folder content.

Extract ZIP file safely into repository folder.

Calculate total directory size.

Update size_in_bytes and updated_at in database.

Important:

Protect against Zip Slip vulnerability.

Prevent path traversal attacks.

6.3 Download Repository

GET /api/repos/{id}/download

Process:

Compress repository folder into a temporary ZIP file.

Stream ZIP file in HTTP response.

Delete temporary file after sending.

Response headers:
Content-Disposition: attachment

6.4 List Files in Repository

GET /api/repos/{id}/files

Process:

Recursively scan repository folder.

Return structured JSON response.

Example:

[
{
"name": "src",
"type": "directory",
"path": "src"
},
{
"name": "pom.xml",
"type": "file",
"path": "pom.xml"
}
]

No file metadata is stored in the database.

7. File Handling Requirements

FileStorageService must implement:

createDirectoryIfNotExists

deleteDirectoryRecursively

unzipFileSafely

zipDirectory

calculateDirectorySize

listFilesRecursively

Security considerations:

Validate canonical paths when extracting ZIP.

Reject files that escape the target directory.

Do not allow absolute paths inside ZIP.

Handle large file uploads properly.

8. Exception Handling

Custom exceptions required:

RepositoryNotFoundException

RepositoryAlreadyExistsException

InvalidFileFormatException

StorageException

Use @ControllerAdvice for global exception handling.

Return proper HTTP status codes:

404 for not found

400 for invalid request

409 for duplicate repository

500 for server/storage errors

9. Design Principles

Do not store files in database.

Do not store files inside project source directory.

Separate application logic from file storage.

Use configuration for storage path (no hardcoding).

Follow SOLID principles.

Keep the architecture extensible.

10. Future Extensibility

The system must be designed in a way that allows future expansion:

Add authentication

Add multi-user support

Add version control

Add Git integration

Convert into microservices

Add CI/CD features

The architecture is intentionally minimal but designed to evolve toward systems similar to large-scale source hosting platforms like:

GitHub

GitLab

However, this project focuses only on file-based storage functionality.

Final Summary

CodeVault is a RESTful Spring Boot application that manages repositories by storing metadata in MySQL and storing actual source code files in an external file system directory. It supports repository creation, upload (ZIP replacement), download, listing files, and deletion, without authentication or version control.