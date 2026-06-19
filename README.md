# ConstructIQ Backend

ConstructIQ Backend is a Spring Boot API for a construction project management platform. It supports user authentication, project ownership, project team registration, tasks, progress reports, risks, document uploads, and dashboard statistics.

## Current Capabilities

### Authentication and Security

* User registration and login
* JWT-based stateless authentication
* BCrypt password hashing
* Protected API endpoints outside `/api/auth/**`
* CORS configured for `http://localhost:5173`

### Project Management

* Create, list, view, update, and delete projects
* Project ownership and access checks
* Project status tracking
* Project member registration records

### Work Tracking

* Task CRUD under projects
* Task status and priority management
* Assignee and due-date tracking
* Progress report CRUD under projects

### Risk Management

* Risk CRUD under projects
* Risk category, probability, impact, severity, level, and status tracking
* Mitigation plan, owner, and target-date fields

### Documents

* Multipart document upload by project
* Document listing and metadata lookup
* Document download
* Document deletion
* Local file storage under the configured upload directory

### Dashboard

* Current dashboard statistics
* Dashboard statistics snapshots
* Latest snapshot retrieval

## Technology Stack

* Java 17
* Spring Boot 3.5.14
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Actuator
* PostgreSQL
* Maven
* Lombok
* JJWT 0.12.6
* JUnit and Spring Security Test

## Project Structure

```text
.
|-- compose.yaml
|-- docs
|   |-- api-documentation.md
|   |-- dashboard-statistics-schema.sql
|   |-- document-upload-schema.sql
|   |-- risk-register-schema.sql
|   |-- script.sql
|   `-- ...
|-- pom.xml
|-- src
|   |-- main
|   |   |-- java/com/constructiq
|   |   |   |-- config
|   |   |   |-- controller
|   |   |   |-- dto
|   |   |   |   |-- projection
|   |   |   |   |-- request
|   |   |   |   `-- response
|   |   |   |-- entity
|   |   |   |-- enums
|   |   |   |-- exception
|   |   |   |-- repository
|   |   |   |-- security
|   |   |   |-- service
|   |   |   `-- util
|   |   `-- resources
|   |       `-- application.properties
|   `-- test/java/com/constructiq
|       `-- service
`-- uploads
```

## Main Packages

* `controller`: REST controllers for auth, users, projects, registrations, tasks, progress reports, risks, documents, and dashboard statistics.
* `service`: Business logic and access validation.
* `repository`: Spring Data JPA repositories.
* `entity`: JPA entities for persisted domain models.
* `dto.request`: Request payloads.
* `dto.response`: Response payloads.
* `dto.projection`: Repository projections.
* `security`: JWT filter, JWT service, and custom user details service.
* `config`: Spring Security and CORS configuration.
* `exception`: Global exception handling and API error response types.
* `enums`: Domain enums for users, projects, tasks, risks, and project membership.
* `util`: Shared utility code.

## Data Model

The current JPA model includes these main tables:

* `users`
* `projects`
* `tasks`
* `progress_reports`
* `risks`
* `documents`
* `user_project_registrations`
* `dashboard_statistics_snapshots`

Additional schema notes are available in `docs/*.sql`.

## API Endpoints

Most endpoints require an `Authorization: Bearer <token>` header. Authentication endpoints are public.

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

### Users

```http
GET /api/users
GET /api/users/{userId}
```

`GET /api/users` accepts optional `name` and `email` query parameters.

### Projects

```http
POST   /api/projects
GET    /api/projects
GET    /api/projects/{id}
PUT    /api/projects/{id}
DELETE /api/projects/{id}
```

### Project Registrations

```http
POST   /api/projects/{projectId}/registrations
GET    /api/projects/{projectId}/registrations
DELETE /api/projects/{projectId}/registrations/{registrationId}
```

### Tasks

```http
POST   /api/projects/{projectId}/tasks
GET    /api/projects/{projectId}/tasks
GET    /api/tasks/{taskId}
PUT    /api/tasks/{taskId}
DELETE /api/tasks/{taskId}
```

### Progress Reports

```http
POST   /api/projects/{projectId}/progressReports
GET    /api/projects/{projectId}/progressReports
GET    /api/progressReports/{progressReportId}
PUT    /api/progressReports/{progressReportId}
DELETE /api/progressReports/{progressReportId}
```

### Risks

```http
POST   /api/projects/{projectId}/risks
GET    /api/projects/{projectId}/risks
GET    /api/risks/{riskId}
PUT    /api/risks/{riskId}
DELETE /api/risks/{riskId}
```

### Documents

```http
POST   /api/projects/{projectId}/documents/upload
GET    /api/projects/{projectId}/documents
GET    /api/documents/{documentId}
GET    /api/documents/{documentId}/download
DELETE /api/documents/{documentId}
```

Document upload uses `multipart/form-data` with a `file` field.

### Dashboard

```http
GET  /api/dashboard/statistics
POST /api/dashboard/statistics/snapshots
GET  /api/dashboard/statistics/snapshots/latest
```

### Development Test Endpoint

```http
GET /test/helloworld
```

## Local Setup

### Prerequisites

* Java 17
* Maven or the included Maven wrapper
* PostgreSQL

### Database

Create a PostgreSQL database that matches `src/main/resources/application.properties`:

```sql
CREATE DATABASE constructiq;
```

Default local datasource settings:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/constructiq
spring.datasource.username=postgres
spring.datasource.password=123456
```

Update these values for your local PostgreSQL user before running the service.

### Application Settings

Key settings live in `src/main/resources/application.properties`:

```properties
jwt.secret=CHANGE_THIS_TO_A_LONG_SECRET_KEY_AT_LEAST_32_CHARS
jwt.expiration=86400000

app.upload-dir=uploads
spring.servlet.multipart.max-file-size=1024MB
spring.servlet.multipart.max-request-size=1024MB
```

Use a strong JWT secret for non-local environments.

### Run

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

### Test

On Windows:

```bash
mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

## Docker Compose Note

`compose.yaml` defines a PostgreSQL container, but its default database, username, and password do not currently match `application.properties`. If you use Docker Compose for local development, update either the compose file or the Spring datasource settings so they point to the same database credentials.

## Documentation

Additional project notes and SQL scripts are in `docs/`, including:

* `docs/api-documentation.md`
* `docs/script.sql`
* `docs/risk-register-schema.sql`
* `docs/document-upload-schema.sql`
* `docs/dashboard-statistics-schema.sql`

## Roadmap

Planned or likely future work:

* AI risk analysis
* AI progress report summarization
* Cloud object storage integration
* Notification workflows
* CI/CD and deployment hardening

## Author

Terrence Chan
