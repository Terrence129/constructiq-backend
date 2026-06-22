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

### Redis Cache

* Redis-backed Spring Cache integration
* Cached service-layer reads for projects, tasks, risks, progress reports, documents, registrations, dashboard statistics, and users
* User-scoped cache keys for permission-sensitive reads
* Cache eviction on write operations to prevent stale responses

## Technology Stack

* Java 17
* Spring Boot 3.5.14
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Actuator
* PostgreSQL
* Redis
* Spring Cache
* Maven
* Lombok
* JJWT 0.12.6
* JUnit and Spring Security Test

## Project Structure

```text
.
|-- docker-compose.yml
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
* `config`: Spring Security, CORS, and Redis cache configuration.
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
* Redis
* Docker Desktop, if using Docker Compose

### Local Services

For local development, run PostgreSQL and Redis. You can run them manually or with Docker Compose.

If you use Docker Compose only for dependencies:

```bash
docker compose up postgres redis
```

Create a PostgreSQL database that matches `src/main/resources/application-local.properties`:

```sql
CREATE DATABASE constructiq;
```

Default local datasource settings:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/constructiq
spring.datasource.username=postgres
spring.datasource.password=123456
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Update these values for your local PostgreSQL and Redis setup before running the service.

### Application Settings

Shared settings live in `src/main/resources/application.properties`. Local datasource and Redis settings live in `src/main/resources/application-local.properties`.

```properties
jwt.secret=CHANGE_THIS_TO_A_LONG_SECRET_KEY_AT_LEAST_32_CHARS
jwt.expiration=86400000

app.upload-dir=uploads
spring.cache.type=redis
spring.data.redis.repositories.enabled=false
```

Use a strong JWT secret for non-local environments.

### Run

Use the `local` profile when running from IntelliJ IDEA or from the command line:

On Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

On macOS/Linux:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API starts at:

```text
http://localhost:8080
```

### Redis Cache Behavior

Redis is used as a cache, not as the primary database. PostgreSQL remains the source of truth.

The cache configuration is in `src/main/java/com/constructiq/config/CacheConfig.java`.

Default cache behavior:

```text
Default TTL: 10 minutes
Dashboard statistics TTL: 2 minutes
Users TTL: 30 minutes
Redis key prefix: constructiq:
```

Service methods use `@Cacheable` for repeated reads and `@CacheEvict` for writes. Permission-sensitive cache keys include the authenticated user's name so cached responses are scoped per user.

To inspect Redis locally:

```bash
docker exec -it constructiq-redis redis-cli
```

Useful Redis commands:

```redis
KEYS constructiq:*
TTL <key>
DEL <key>
FLUSHDB
```

Use `KEYS` only in local development.

### Test

On Windows:

```bash
mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

## Docker Compose Notes

`docker-compose.yml` defines PostgreSQL, Redis, and the backend application container.

When running the backend from IntelliJ IDEA, do not also start the `backend` Compose service on host port `8080`, or the embedded Spring Boot web server will fail with:

```text
Web server failed to start. Port 8080 was already in use.
```

This can happen automatically because the project includes `spring-boot-docker-compose`. During local Spring Boot startup, Boot can detect `docker-compose.yml` and start services from it. Since the current Compose file includes:

```yaml
backend:
  ports:
    - "8080:8080"
```

the backend container can occupy the same port that the IntelliJ-run application needs.

Recommended local approach:

```bash
docker compose up postgres redis
```

Then run the backend from IntelliJ or Maven with the `local` profile.

Alternative fixes:

* Disable Spring Boot Docker Compose integration for local runs with `spring.docker.compose.enabled=false`.
* Move the `backend` service into a Docker Compose profile and only start it intentionally.
* Create a dependency-only Compose file for PostgreSQL and Redis, then point `spring.docker.compose.file` to that file.
* Run the IntelliJ application on another port with `server.port=8081`.

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

## Author

Terrence Chan
