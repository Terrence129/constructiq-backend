# ConstructIQ Backend

## Overview

ConstructIQ is an AI-powered Construction Project Management Platform designed to help engineering and construction teams manage projects, tasks, risks, progress reports, and project documentation.

The platform aims to improve project visibility, collaboration, and decision-making by combining traditional project management workflows with AI-assisted risk analysis.

This repository contains the backend service built with Spring Boot.

---

## Features

### Authentication & Authorization

* User registration
* User login
* JWT authentication
* Protected REST APIs
* Role-based foundation for future expansion

### Project Management

* Create project
* Update project
* Delete project
* View project details
* Project ownership validation

### Task Management

* Create tasks under projects
* Update task status
* Task priority management
* Assignee tracking
* Task ownership protection

### Progress Report Management

* Submit project progress reports
* Track completed work
* Record delayed work
* Capture project issues
* Define next actions

### Future Features

* Risk Management
* Document Management
* AI Risk Analyzer
* AI Progress Report Summarizer
* Dashboard Analytics
* AWS S3 Integration
* Notification System

---

## Technology Stack

### Backend

* Java 17
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Maven

### Database

* PostgreSQL

### Authentication

* JWT (JSON Web Token)
* BCrypt Password Encryption

### API Documentation

* Swagger / OpenAPI

### Future Integrations

* OpenAI API
* AWS S3
* Docker
* CI/CD

---

## Project Structure

```text
src/main/java/com/constructiq

├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── security
├── config
├── exception
├── enums
└── util
```

---

## Database Design

### Users

```text
id
name
email
password_hash
role
created_at
updated_at
```

### Projects

```text
id
name
description
location
client_name
status
start_date
end_date
created_by
created_at
updated_at
```

### Tasks

```text
id
project_id
title
description
status
priority
assignee
due_date
created_at
updated_at
```

### Progress Reports

```text
id
project_id
report_date
summary
completed_work
delayed_work
issues
next_actions
created_by
created_at
updated_at
```

---

## API Endpoints

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

### Projects

```http
POST   /api/projects
GET    /api/projects
GET    /api/projects/{id}
PUT    /api/projects/{id}
DELETE /api/projects/{id}
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

---

## Local Setup

### Prerequisites

* Java 17
* Maven
* PostgreSQL

### Clone Repository

```bash
git clone https://github.com/your-username/constructiq-backend.git

cd constructiq-backend
```

### Create Database

```sql
CREATE DATABASE constructiq;
```

### Configure Application

Update:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/constructiq
spring.datasource.username=postgres
spring.datasource.password=your_password

jwt.secret=your_secret_key
jwt.expiration=86400000
```

### Run Application

```bash
mvn spring-boot:run
```

Application will start at:

```text
http://localhost:8080
```

---

## Development Workflow

### Create Feature Branch

```bash
git checkout -b feature/project-crud
```

### Commit Convention

```bash
feat(auth): implement register and login

feat(project): implement project CRUD

feat(task): implement task management

feat(report): implement progress report management
```

### Pull Request Convention

```text
feat(module): short description
```

Example:

```text
feat(task): implement task management
```

---

## Roadmap

### Phase 1

* Authentication
* Project CRUD
* Task Management
* Progress Reports

### Phase 2

* Risk Management
* File Upload
* Dashboard Analytics

### Phase 3

* AI Risk Analysis
* AI Progress Summarization
* AI Project Insights

### Phase 4

* AWS Deployment
* Docker
* CI/CD Pipeline

---

## Author

Terrence Chan

BSc Computer Science and Technology (CDUT & Oxford Brookes University)

Graduate Diploma in Systems Analysis (NUS-ISS)

Interested in:

* Full-Stack Development
* Cloud Computing
* AI Applications
* Construction Digital Transformation
