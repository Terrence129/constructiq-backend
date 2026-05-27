# **ConstructIQ**

## AI-powered Construction Project Management Platform

A full-stack system for engineering/construction teams to track projects, tasks, progress reports, documents, risks, and use AI to analyze project risks.

This fits your background very well.

------

# 1. Tech Stack

## Backend

```text
Java 17
Spring Boot 3
Spring Security
JWT
Spring Data JPA
PostgreSQL
Maven
Swagger/OpenAPI
Docker
```

## Frontend

```text
React
TypeScript
Vite
TailwindCSS
React Router
Axios
TanStack Query
```

## AI

```text
OpenAI API or compatible LLM API
AI risk analysis
AI report summary
```

## Cloud / Deployment

```text
Frontend: Vercel
Backend: Render / Railway / AWS EC2
Database: PostgreSQL cloud
File storage: local first, then AWS S3
```

------

# 2. Main Features

## Version 1 — Core System

```text
User register/login
JWT authentication
Project CRUD
Task management
Progress report submission
Risk register
Document upload
Dashboard statistics
```

## Version 2 — AI Features

```text
AI risk analyzer
AI progress report summarizer
AI action suggestions
```

## Version 3 — Professional Polish

```text
Deployment
Swagger API docs
README
Architecture diagram
Screenshots
Demo data
```

------

# 3. Database Design

Use these tables:

```text
users
projects
tasks
progress_reports
risks
documents
ai_analysis_results
```

## User

```text
id
name
email
password_hash
role
created_at
```

## Project

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
```

## Task

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
```

## ProgressReport

```text
id
project_id
report_date
summary
completed_work
delayed_work
issues
created_by
created_at
```

## Risk

```text
id
project_id
title
description
category
probability
impact
severity
status
mitigation_plan
created_at
```

## Document

```text
id
project_id
file_name
file_url
file_type
uploaded_by
created_at
```

## AIAnalysisResult

```text
id
project_id
source_type
source_id
risk_level
summary
suggested_actions
created_at
```

------

# 4. Backend Project Structure

Create Spring Boot project:

```text
constructiq-backend
```

Structure:

```text
src/main/java/com/constructiq
├── ConstructIqApplication.java
├── config
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── OpenApiConfig.java
├── controller
│   ├── AuthController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   ├── ReportController.java
│   ├── RiskController.java
│   ├── DocumentController.java
│   └── AiController.java
├── dto
│   ├── request
│   └── response
├── entity
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   ├── ProgressReport.java
│   ├── Risk.java
│   ├── Document.java
│   └── AiAnalysisResult.java
├── repository
├── service
├── exception
└── util
```

------

# 5. Backend API Design

## Auth

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

## Projects

```http
GET    /api/projects
POST   /api/projects
GET    /api/projects/{id}
PUT    /api/projects/{id}
DELETE /api/projects/{id}
```

## Tasks

```http
GET  /api/projects/{projectId}/tasks
POST /api/projects/{projectId}/tasks
PUT  /api/tasks/{taskId}
DELETE /api/tasks/{taskId}
```

## Reports

```http
GET  /api/projects/{projectId}/reports
POST /api/projects/{projectId}/reports
GET  /api/reports/{reportId}
```

## Risks

```http
GET  /api/projects/{projectId}/risks
POST /api/projects/{projectId}/risks
PUT  /api/risks/{riskId}
DELETE /api/risks/{riskId}
```

## Documents

```http
POST /api/projects/{projectId}/documents/upload
GET  /api/projects/{projectId}/documents
```

## AI

```http
POST /api/projects/{projectId}/ai/analyze-risk
POST /api/reports/{reportId}/ai/summarize
```

------

# 6. Frontend Structure

Create:

```text
constructiq-frontend
```

Structure:

```text
src
├── api
│   ├── axios.ts
│   ├── authApi.ts
│   ├── projectApi.ts
│   ├── taskApi.ts
│   ├── reportApi.ts
│   ├── riskApi.ts
│   └── aiApi.ts
├── components
│   ├── layout
│   ├── ui
│   ├── project
│   ├── task
│   ├── risk
│   └── report
├── pages
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── DashboardPage.tsx
│   ├── ProjectsPage.tsx
│   ├── ProjectDetailPage.tsx
│   ├── TasksPage.tsx
│   ├── RisksPage.tsx
│   ├── ReportsPage.tsx
│   └── AiAnalysisPage.tsx
├── types
│   ├── auth.ts
│   ├── project.ts
│   ├── task.ts
│   ├── risk.ts
│   └── report.ts
├── hooks
├── router
└── main.tsx
```

------

# 7. Frontend Pages

## Login Page

Must include:

```text
email
password
login button
error message
JWT storage
redirect to dashboard
```

## Dashboard Page

Show:

```text
total projects
active projects
high risks
overdue tasks
recent reports
```

## Projects Page

Show:

```text
project list
create project modal
status filter
search
```

## Project Detail Page

Tabs:

```text
Overview
Tasks
Reports
Risks
Documents
AI Analysis
```

## Risk Page

Show risk matrix:

```text
probability × impact
severity = probability * impact
```

## AI Page

User clicks:

```text
Analyze Project Risk
```

Then show:

```text
Risk level
Summary
Main issues
Suggested actions
```

------

# 8. Implementation Order

Do not build randomly. Follow this order.

## Step 1: Backend skeleton

Create Spring Boot project with dependencies:

```text
Spring Web
Spring Security
Spring Data JPA
PostgreSQL Driver
Validation
Lombok
Springdoc OpenAPI
```

First goal:

```text
GET /api/health
```

returns:

```json
{
  "status": "ok"
}
```

------

## Step 2: PostgreSQL connection

Create database:

```sql
CREATE DATABASE constructiq;
```

In `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/constructiq
    username: postgres
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

------

## Step 3: User authentication

Implement:

```text
User entity
UserRepository
RegisterRequest
LoginRequest
AuthResponse
JwtService
AuthService
AuthController
SecurityConfig
```

After this, you must be able to:

```text
register user
login user
receive JWT
access protected API
```

This is the first serious checkpoint.

------

## Step 4: Project CRUD

Implement:

```text
Project entity
ProjectRepository
ProjectService
ProjectController
ProjectRequest
ProjectResponse
```

You must support:

```text
create project
list projects
get project detail
update project
delete project
```

------

## Step 5: Task module

Implement:

```text
Task entity
Task status enum
Task priority enum
TaskService
TaskController
```

Statuses:

```text
TODO
IN_PROGRESS
BLOCKED
DONE
```

Priorities:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

------

## Step 6: Risk module

Implement:

```text
Risk entity
RiskController
RiskService
```

Risk severity formula:

```text
severity = probability * impact
```

Example:

```text
probability: 4
impact: 5
severity: 20
risk level: HIGH
```

------

## Step 7: Progress report module

Fields:

```text
summary
completedWork
delayedWork
issues
reportDate
```

This module is important because AI will later analyze it.

------

## Step 8: File upload

First use local storage:

```text
/uploads
```

Later replace with AWS S3.

Endpoint:

```http
POST /api/projects/{projectId}/documents/upload
```

Accept:

```text
multipart/form-data
```

------

## Step 9: Frontend login

Create React app.

Install:

```bash
npm create vite@latest constructiq-frontend -- --template react-ts
cd constructiq-frontend
npm install
npm install axios react-router-dom @tanstack/react-query
npm install -D tailwindcss postcss autoprefixer
```

Build:

```text
LoginPage
RegisterPage
ProtectedRoute
axios interceptor with JWT
```

------

## Step 10: Frontend project dashboard

Build:

```text
DashboardPage
ProjectsPage
ProjectDetailPage
```

At this point:

```text
Frontend can call backend
JWT works
Projects display properly
```

------

## Step 11: Add tasks, risks, reports UI

Build:

```text
TaskTable
TaskForm
RiskTable
RiskForm
ReportForm
ReportList
```

Do not make it beautiful first.

Make it functional.

------

## Step 12: AI risk analyzer

Backend receives project data and recent reports.

Prompt example:

```text
You are an assistant for construction project management.

Analyze the following project information and identify possible risks.

Project:
{name}

Description:
{description}

Recent progress reports:
{reports}

Existing risks:
{risks}

Return result in JSON:
{
  "riskLevel": "LOW | MEDIUM | HIGH | CRITICAL",
  "summary": "...",
  "mainIssues": ["...", "..."],
  "suggestedActions": ["...", "..."]
}
```

Store result in:

```text
ai_analysis_results
```

Frontend displays result in a clean card.

------

# 9. What You Will Master From This Project

## Java / Spring Boot

You will master:

```text
REST API
Spring Security
JWT
JPA relationships
DTO design
service layer
exception handling
validation
file upload
API documentation
```

## React / TypeScript

You will master:

```text
component design
forms
API calls
routing
protected pages
state management
TypeScript interfaces
dashboard UI
```

## Database

You will master:

```text
entity relationships
foreign keys
query design
pagination
PostgreSQL basics
```

## Cloud

You will master:

```text
environment variables
deployment
database hosting
frontend/backend separation
S3-style file storage
```

## AI Engineering

You will master:

```text
prompt design
LLM API call
JSON response parsing
AI feature integration
business workflow design
```

------

# 10. Weekly Milestones

## Week 1

```text
Spring Boot setup
PostgreSQL setup
User auth
JWT
```

## Week 2

```text
Project CRUD
Task CRUD
Risk CRUD
```

## Week 3

```text
Progress reports
File upload
Swagger docs
Backend polish
```

## Week 4

```text
React setup
Login/register
Dashboard
Project pages
```

## Week 5

```text
Tasks UI
Risks UI
Reports UI
Frontend/backend integration
```

## Week 6

```text
AI risk analyzer
AI report summarizer
Store AI results
```

## Week 7

```text
Deployment
Docker
PostgreSQL cloud
Environment variables
```

## Week 8

```text
README
Screenshots
Architecture diagram
Resume bullets
LinkedIn post
Interview preparation
```

------

# 11. Minimum Viable Version

The minimum version must have:

```text
Login/register
JWT
Project CRUD
Task CRUD
Risk CRUD
Progress reports
AI risk analysis
React dashboard
Deployment
README
```

This alone is already better than most student projects.

------

# 12. Advanced Features Later

Only after MVP:

```text
Role-based access: Admin / Manager / Engineer
AWS S3 upload
Email notification
Calendar view
Kanban board
PDF report export
Audit logs
Docker Compose
CI/CD GitHub Actions
```

Do not start with these.

------

# 13. Your First Task Now

Start backend.

Create:

```text
constructiq-backend
```

First endpoint:

```http
GET /api/health
```

Expected response:

```json
{
  "status": "ok",
  "service": "ConstructIQ Backend"
}
```

Then move to authentication.

Your first serious checkpoint is:

> Register → Login → receive JWT → use JWT to call protected API.

Once you can do that, the project has truly started.