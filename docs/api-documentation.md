# ConstructIQ Backend API Documentation

## Overview

This document describes the REST API exposed by the ConstructIQ backend.

Base URL for local development:

```text
http://localhost:8080
```

All API request and response bodies use JSON unless stated otherwise.

## Authentication

The API uses JWT bearer authentication.

Include the token returned by login or registration in protected requests:

```http
Authorization: Bearer <token>
```

Public endpoints:

```http
POST /api/auth/register
POST /api/auth/login
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

All other endpoints require authentication.

## Authorization Model

### Global User Roles

| Role | Description |
| --- | --- |
| `USER` | Standard authenticated user. |
| `ADMIN` | User allowed to create projects. |

New users registered through `POST /api/auth/register` are created with role `USER`. Admin users must be created or promoted outside the public registration API.

### Project Member Roles

| Role | Description |
| --- | --- |
| `MEMBER` | Can access project data. |
| `MANAGER` | Can access project data and manage the project. |

### Project Access Rules

| Action | Allowed Users |
| --- | --- |
| Create project | `ADMIN` only. |
| View project | Project creator or registered project member. |
| List my projects | Authenticated user. Returns projects created by the user and projects where the user is registered. |
| Update project | Project creator or project member with role `MANAGER`. |
| Delete project | Project creator or project member with role `MANAGER`. |
| Add/list/remove project registrations | Project creator or project member with role `MANAGER`. |
| List tasks under a project | Project creator or registered project member. |
| Read a task | Project creator or registered project member. |
| Create/update/delete a task | Project creator or project member with role `MANAGER`. |
| List progress reports under a project | Project creator or registered project member. |
| Read a progress report | Project creator or registered project member. |
| Create/update/delete a progress report | Project creator or project member with role `MANAGER`. |
| List risks under a project | Project creator or registered project member. |
| Read a risk | Project creator or registered project member. |
| Create/update/delete a risk | Project creator or project member with role `MANAGER`. |
| List documents under a project | Project creator or registered project member. |
| Read/download a document | Project creator or registered project member. |
| Upload/delete a document | Project creator or project member with role `MANAGER`. |
| View dashboard statistics | Authenticated user. Includes only projects the user can access. |
| Create/read dashboard statistics snapshots | Authenticated user. Snapshots are scoped to the current user. |

## Common Response Types

### Error Response

```json
{
  "timestamp": "2026-06-17T15:55:37.123",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to manage this project"
}
```

Common status codes:

| Status | Meaning |
| --- | --- |
| `200 OK` | Request succeeded. |
| `400 Bad Request` | Invalid input, validation failure, or duplicate registration. |
| `401 Unauthorized` | Invalid login credentials. |
| `403 Forbidden` | Authenticated user does not have permission. |
| `404 Not Found` | Requested resource was not found. |

## Enums

### UserRole

```text
USER
ADMIN
```

### ProjectStatus

```text
PLANNING
ACTIVE
ON_HOLD
COMPLETED
CANCELLED
```

### ProjectMemberRole

```text
MEMBER
MANAGER
```

### TaskStatus

```text
TODO
IN_PROGRESS
BLOCKED
DONE
```

### TaskPriority

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### RiskCategory

```text
SAFETY
SCHEDULE
COST
QUALITY
DESIGN
PROCUREMENT
ENVIRONMENT
LEGAL
GENERAL
```

### RiskLevel

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### RiskStatus

```text
OPEN
MITIGATING
MONITORING
CLOSED
```

## Authentication API

### Register

Creates a new user account with role `USER` and returns a JWT.

```http
POST /api/auth/register
Content-Type: application/json
```

Request body:

| Field | Type | Required | Validation |
| --- | --- | --- | --- |
| `name` | string | Yes | Not blank. |
| `email` | string | Yes | Valid email, not blank, unique. |
| `password` | string | Yes | Not blank, minimum 8 characters. |

Example request:

```json
{
  "name": "Jane Builder",
  "email": "jane.builder@example.com",
  "password": "Password123!"
}
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "name": "Jane Builder",
    "email": "jane.builder@example.com",
    "role": "USER"
  }
}
```

### Login

Authenticates a user and returns a JWT.

```http
POST /api/auth/login
Content-Type: application/json
```

Request body:

| Field | Type | Required | Validation |
| --- | --- | --- | --- |
| `email` | string | Yes | Valid email, not blank. |
| `password` | string | Yes | Not blank. |

Example request:

```json
{
  "email": "jane.builder@example.com",
  "password": "Password123!"
}
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "name": "Jane Builder",
    "email": "jane.builder@example.com",
    "role": "USER"
  }
}
```

## Projects API

### Project Object

```json
{
  "id": 10,
  "name": "Harbour Tower",
  "description": "Mixed-use construction project.",
  "location": "Singapore",
  "clientName": "ConstructIQ Client",
  "status": "PLANNING",
  "startDate": "2026-07-01",
  "endDate": "2027-12-31",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Create Project

Creates a project. The authenticated user must have role `ADMIN`.

The creator can add people to the project during creation by passing `members`. Members are stored as project registrations. If a member role is omitted, it defaults to `MEMBER`.

```http
POST /api/projects
Authorization: Bearer <admin-token>
Content-Type: application/json
```

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `name` | string | Yes | Not blank. |
| `description` | string | No | Project description. |
| `location` | string | No | Project location. |
| `clientName` | string | No | Client name. |
| `status` | ProjectStatus | No | Defaults to `PLANNING`. |
| `startDate` | date | No | Format `YYYY-MM-DD`. |
| `endDate` | date | No | Format `YYYY-MM-DD`. |
| `members` | array | No | Initial project members. |

Member object:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `userId` | number | Yes | Existing user id. Must not be the project creator. |
| `title` | string | No | Member title in the project. |
| `description` | string | No | Member responsibility description. |
| `role` | ProjectMemberRole | No | Defaults to `MEMBER`. |

Example request:

```json
{
  "name": "Harbour Tower",
  "description": "Mixed-use construction project.",
  "location": "Singapore",
  "clientName": "ConstructIQ Client",
  "status": "PLANNING",
  "startDate": "2026-07-01",
  "endDate": "2027-12-31",
  "members": [
    {
      "userId": 2,
      "title": "Site Engineer",
      "description": "Responsible for weekly site updates.",
      "role": "MEMBER"
    },
    {
      "userId": 3,
      "title": "Project Manager",
      "description": "Can manage project data and members.",
      "role": "MANAGER"
    }
  ]
}
```

Example response:

```json
{
  "id": 10,
  "name": "Harbour Tower",
  "description": "Mixed-use construction project.",
  "location": "Singapore",
  "clientName": "ConstructIQ Client",
  "status": "PLANNING",
  "startDate": "2026-07-01",
  "endDate": "2027-12-31",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

Failure cases:

| Status | Reason |
| --- | --- |
| `400` | Duplicate users in `members`, or creator is included as a member. |
| `403` | Authenticated user is not an admin. |
| `404` | A member `userId` does not exist. |

### List My Projects

Returns projects created by the authenticated user plus projects where the user is registered.

```http
GET /api/projects
Authorization: Bearer <token>
```

Example response:

```json
[
  {
    "id": 10,
    "name": "Harbour Tower",
    "description": "Mixed-use construction project.",
    "location": "Singapore",
    "clientName": "ConstructIQ Client",
    "status": "PLANNING",
    "startDate": "2026-07-01",
    "endDate": "2027-12-31",
    "createdById": 1,
    "createdByName": "Admin User",
    "createdAt": "2026-06-17T15:55:37.123",
    "updatedAt": null
  }
]
```

### Get Project By ID

The authenticated user must be the project creator or a registered project member.

```http
GET /api/projects/{id}
Authorization: Bearer <token>
```

Path parameters:

| Parameter | Type | Required |
| --- | --- | --- |
| `id` | number | Yes |

Example response:

```json
{
  "id": 10,
  "name": "Harbour Tower",
  "description": "Mixed-use construction project.",
  "location": "Singapore",
  "clientName": "ConstructIQ Client",
  "status": "PLANNING",
  "startDate": "2026-07-01",
  "endDate": "2027-12-31",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Update Project

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
PUT /api/projects/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

Path parameters:

| Parameter | Type | Required |
| --- | --- | --- |
| `id` | number | Yes |

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `name` | string | Yes | Not blank. |
| `description` | string | No | Replaces current value. |
| `location` | string | No | Replaces current value. |
| `clientName` | string | No | Replaces current value. |
| `status` | ProjectStatus | No | Replaces current value if present. |
| `startDate` | date | No | Replaces current value. |
| `endDate` | date | No | Replaces current value. |

Example request:

```json
{
  "name": "Harbour Tower Phase 1",
  "description": "Updated project description.",
  "location": "Singapore",
  "clientName": "ConstructIQ Client",
  "status": "ACTIVE",
  "startDate": "2026-07-01",
  "endDate": "2027-12-31"
}
```

Example response:

```json
{
  "id": 10,
  "name": "Harbour Tower Phase 1",
  "description": "Updated project description.",
  "location": "Singapore",
  "clientName": "ConstructIQ Client",
  "status": "ACTIVE",
  "startDate": "2026-07-01",
  "endDate": "2027-12-31",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": "2026-06-17T16:10:00.000"
}
```

### Delete Project

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
DELETE /api/projects/{id}
Authorization: Bearer <token>
```

Successful response:

```text
200 OK
```

The response body is empty.

## Project Registrations API

Project registrations define which users can access a project and whether they can manage it.

### Registration Object

```json
{
  "id": 100,
  "userId": 2,
  "userName": "Jane Builder",
  "userEmail": "jane.builder@example.com",
  "projectId": 10,
  "projectName": "Harbour Tower",
  "title": "Site Engineer",
  "description": "Responsible for weekly site updates.",
  "role": "MEMBER",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Add Project Registration

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
POST /api/projects/{projectId}/registrations
Authorization: Bearer <token>
Content-Type: application/json
```

Path parameters:

| Parameter | Type | Required |
| --- | --- | --- |
| `projectId` | number | Yes |

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `userId` | number | Yes | Existing user id. Must not be the project creator. |
| `title` | string | No | Member title in the project. |
| `description` | string | No | Member responsibility description. |
| `role` | ProjectMemberRole | No | Defaults to `MEMBER`. |

Example request:

```json
{
  "userId": 2,
  "title": "Site Engineer",
  "description": "Responsible for weekly site updates.",
  "role": "MEMBER"
}
```

Example response:

```json
{
  "id": 100,
  "userId": 2,
  "userName": "Jane Builder",
  "userEmail": "jane.builder@example.com",
  "projectId": 10,
  "projectName": "Harbour Tower",
  "title": "Site Engineer",
  "description": "Responsible for weekly site updates.",
  "role": "MEMBER",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

Failure cases:

| Status | Reason |
| --- | --- |
| `400` | User is already registered, or the target user is the project creator. |
| `403` | Authenticated user cannot manage the project. |
| `404` | Project or user not found. |

### List Project Registrations

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
GET /api/projects/{projectId}/registrations
Authorization: Bearer <token>
```

Example response:

```json
[
  {
    "id": 100,
    "userId": 2,
    "userName": "Jane Builder",
    "userEmail": "jane.builder@example.com",
    "projectId": 10,
    "projectName": "Harbour Tower",
    "title": "Site Engineer",
    "description": "Responsible for weekly site updates.",
    "role": "MEMBER",
    "createdAt": "2026-06-17T15:55:37.123",
    "updatedAt": null
  }
]
```

### Delete Project Registration

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
DELETE /api/projects/{projectId}/registrations/{registrationId}
Authorization: Bearer <token>
```

Path parameters:

| Parameter | Type | Required |
| --- | --- | --- |
| `projectId` | number | Yes |
| `registrationId` | number | Yes |

Successful response:

```text
200 OK
```

The response body is empty.

## Tasks API

### Task Object

```json
{
  "id": 500,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "title": "Inspect foundation works",
  "description": "Confirm foundation preparation is complete.",
  "status": "TODO",
  "priority": "HIGH",
  "assignee": "Jane Builder",
  "dueDate": "2026-07-15",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Create Task

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
POST /api/projects/{projectId}/tasks
Authorization: Bearer <token>
Content-Type: application/json
```

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `title` | string | Yes | Not blank. |
| `description` | string | No | Task description. |
| `status` | TaskStatus | No | Defaults to `TODO`. |
| `priority` | TaskPriority | No | Defaults to `MEDIUM`. |
| `assignee` | string | No | Free-text assignee name. |
| `dueDate` | date | No | Format `YYYY-MM-DD`. |

Example request:

```json
{
  "title": "Inspect foundation works",
  "description": "Confirm foundation preparation is complete.",
  "status": "TODO",
  "priority": "HIGH",
  "assignee": "Jane Builder",
  "dueDate": "2026-07-15"
}
```

Example response:

```json
{
  "id": 500,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "title": "Inspect foundation works",
  "description": "Confirm foundation preparation is complete.",
  "status": "TODO",
  "priority": "HIGH",
  "assignee": "Jane Builder",
  "dueDate": "2026-07-15",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### List Tasks By Project

The authenticated user must be the project creator or a registered project member.

```http
GET /api/projects/{projectId}/tasks
Authorization: Bearer <token>
```

Example response:

```json
[
  {
    "id": 500,
    "projectId": 10,
    "projectName": "Harbour Tower",
    "title": "Inspect foundation works",
    "description": "Confirm foundation preparation is complete.",
    "status": "TODO",
    "priority": "HIGH",
    "assignee": "Jane Builder",
    "dueDate": "2026-07-15",
    "createdAt": "2026-06-17T15:55:37.123",
    "updatedAt": null
  }
]
```

### Get Task By ID

The authenticated user must be the project creator or a registered project member.

```http
GET /api/tasks/{taskId}
Authorization: Bearer <token>
```

Example response:

```json
{
  "id": 500,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "title": "Inspect foundation works",
  "description": "Confirm foundation preparation is complete.",
  "status": "TODO",
  "priority": "HIGH",
  "assignee": "Jane Builder",
  "dueDate": "2026-07-15",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Update Task

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
PUT /api/tasks/{taskId}
Authorization: Bearer <token>
Content-Type: application/json
```

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `title` | string | Yes | Not blank. |
| `description` | string | No | Replaces current value. |
| `status` | TaskStatus | No | Replaces current value if present. |
| `priority` | TaskPriority | No | Replaces current value if present. |
| `assignee` | string | No | Replaces current value. |
| `dueDate` | date | No | Replaces current value. |

Example request:

```json
{
  "title": "Inspect foundation works",
  "description": "Inspection completed.",
  "status": "DONE",
  "priority": "HIGH",
  "assignee": "Jane Builder",
  "dueDate": "2026-07-15"
}
```

### Delete Task

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
DELETE /api/tasks/{taskId}
Authorization: Bearer <token>
```

Successful response:

```text
200 OK
```

The response body is empty.

## Progress Reports API

### Progress Report Object

```json
{
  "id": 700,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "reportDate": "2026-07-08",
  "summary": "Weekly progress update.",
  "completedWork": "Foundation inspection completed.",
  "delayedWork": "None.",
  "issues": "None.",
  "nextActions": "Prepare next inspection checklist.",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Create Progress Report

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
POST /api/projects/{projectId}/progressReports
Authorization: Bearer <token>
Content-Type: application/json
```

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `reportDate` | date | Yes | Format `YYYY-MM-DD`. |
| `summary` | string | Yes | Report summary. |
| `completedWork` | string | No | Completed work details. |
| `delayedWork` | string | No | Delayed work details. |
| `issues` | string | No | Current issues. |
| `nextActions` | string | No | Planned next actions. |

Example request:

```json
{
  "reportDate": "2026-07-08",
  "summary": "Weekly progress update.",
  "completedWork": "Foundation inspection completed.",
  "delayedWork": "None.",
  "issues": "None.",
  "nextActions": "Prepare next inspection checklist."
}
```

Example response:

```json
{
  "id": 700,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "reportDate": "2026-07-08",
  "summary": "Weekly progress update.",
  "completedWork": "Foundation inspection completed.",
  "delayedWork": "None.",
  "issues": "None.",
  "nextActions": "Prepare next inspection checklist.",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### List Progress Reports By Project

The authenticated user must be the project creator or a registered project member.

```http
GET /api/projects/{projectId}/progressReports
Authorization: Bearer <token>
```

Example response:

```json
[
  {
    "id": 700,
    "projectId": 10,
    "projectName": "Harbour Tower",
    "reportDate": "2026-07-08",
    "summary": "Weekly progress update.",
    "completedWork": "Foundation inspection completed.",
    "delayedWork": "None.",
    "issues": "None.",
    "nextActions": "Prepare next inspection checklist.",
    "createdById": 1,
    "createdByName": "Admin User",
    "createdAt": "2026-06-17T15:55:37.123",
    "updatedAt": null
  }
]
```

### Get Progress Report By ID

The authenticated user must be the project creator or a registered project member.

```http
GET /api/progressReports/{progressReportId}
Authorization: Bearer <token>
```

Example response:

```json
{
  "id": 700,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "reportDate": "2026-07-08",
  "summary": "Weekly progress update.",
  "completedWork": "Foundation inspection completed.",
  "delayedWork": "None.",
  "issues": "None.",
  "nextActions": "Prepare next inspection checklist.",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T15:55:37.123",
  "updatedAt": null
}
```

### Update Progress Report

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
PUT /api/progressReports/{progressReportId}
Authorization: Bearer <token>
Content-Type: application/json
```

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `reportDate` | date | No | Replaces current value if present. |
| `summary` | string | No | Replaces current value if present. |
| `completedWork` | string | No | Replaces current value. |
| `delayedWork` | string | No | Replaces current value. |
| `issues` | string | No | Replaces current value. |
| `nextActions` | string | No | Replaces current value. |

Example request:

```json
{
  "reportDate": "2026-07-09",
  "summary": "Updated weekly progress report.",
  "completedWork": "Foundation inspection and report update completed.",
  "delayedWork": "None.",
  "issues": "None.",
  "nextActions": "Schedule next site walkthrough."
}
```

### Delete Progress Report

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
DELETE /api/progressReports/{progressReportId}
Authorization: Bearer <token>
```

Successful response:

```text
200 OK
```

The response body is empty.

## Risks API

Risk severity is calculated by the backend:

```text
severity = probability * impact
```

Risk level is derived from severity:

| Severity | Risk level |
| --- | --- |
| `1-5` | `LOW` |
| `6-10` | `MEDIUM` |
| `11-15` | `HIGH` |
| `16-25` | `CRITICAL` |

### Risk Object

```json
{
  "id": 900,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "title": "Steel delivery delay",
  "description": "Structural steel delivery may delay critical path work.",
  "category": "SCHEDULE",
  "probability": 4,
  "impact": 5,
  "severity": 20,
  "riskLevel": "CRITICAL",
  "status": "OPEN",
  "mitigationPlan": "Confirm alternate supplier and resequence non-critical tasks.",
  "owner": "Site Manager",
  "targetDate": "2026-07-20",
  "createdById": 1,
  "createdByName": "Admin User",
  "createdAt": "2026-06-17T18:42:38.000",
  "updatedAt": null
}
```

### Create Risk

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
POST /api/projects/{projectId}/risks
Authorization: Bearer <token>
Content-Type: application/json
```

Request body:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `title` | string | Yes | Not blank. |
| `description` | string | No | Risk description. |
| `category` | RiskCategory | No | Defaults to `GENERAL`. |
| `probability` | number | Yes | Integer from `1` to `5`. |
| `impact` | number | Yes | Integer from `1` to `5`. |
| `status` | RiskStatus | No | Defaults to `OPEN`. |
| `mitigationPlan` | string | No | Planned mitigation. |
| `owner` | string | No | Free-text owner name. |
| `targetDate` | date | No | Format `YYYY-MM-DD`. |

Example request:

```json
{
  "title": "Steel delivery delay",
  "description": "Structural steel delivery may delay critical path work.",
  "category": "SCHEDULE",
  "probability": 4,
  "impact": 5,
  "status": "OPEN",
  "mitigationPlan": "Confirm alternate supplier and resequence non-critical tasks.",
  "owner": "Site Manager",
  "targetDate": "2026-07-20"
}
```

### List Risks By Project

The authenticated user must be the project creator or a registered project member.

```http
GET /api/projects/{projectId}/risks
Authorization: Bearer <token>
```

### Get Risk By ID

The authenticated user must be the project creator or a registered project member.

```http
GET /api/risks/{riskId}
Authorization: Bearer <token>
```

### Update Risk

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
PUT /api/risks/{riskId}
Authorization: Bearer <token>
Content-Type: application/json
```

The request body uses the same fields as create. The backend recalculates `severity` and `riskLevel` when `probability` or `impact` changes.

### Delete Risk

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
DELETE /api/risks/{riskId}
Authorization: Bearer <token>
```

Successful response:

```text
200 OK
```

The response body is empty.

## Documents API

Documents are stored on the local filesystem under the configured upload directory.

Default local upload directory:

```properties
app.upload-dir=uploads
```

Uploaded file metadata is stored in the `documents` table. The file itself is stored under:

```text
{app.upload-dir}/projects/{projectId}
```

### Document Object

```json
{
  "id": 1100,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "fileName": "site-plan.pdf",
  "fileUrl": "/api/documents/1100/download",
  "fileType": "application/pdf",
  "fileSize": 204800,
  "uploadedById": 1,
  "uploadedByName": "Admin User",
  "createdAt": "2026-06-17T19:18:52.000",
  "updatedAt": null
}
```

### Upload Document

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
POST /api/projects/{projectId}/documents/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Form fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `file` | file | Yes | Uploaded document file. |

Example response:

```json
{
  "id": 1100,
  "projectId": 10,
  "projectName": "Harbour Tower",
  "fileName": "site-plan.pdf",
  "fileUrl": "/api/documents/1100/download",
  "fileType": "application/pdf",
  "fileSize": 204800,
  "uploadedById": 1,
  "uploadedByName": "Admin User",
  "createdAt": "2026-06-17T19:18:52.000",
  "updatedAt": null
}
```

### List Documents By Project

The authenticated user must be the project creator or a registered project member.

```http
GET /api/projects/{projectId}/documents
Authorization: Bearer <token>
```

Example response:

```json
[
  {
    "id": 1100,
    "projectId": 10,
    "projectName": "Harbour Tower",
    "fileName": "site-plan.pdf",
    "fileUrl": "/api/documents/1100/download",
    "fileType": "application/pdf",
    "fileSize": 204800,
    "uploadedById": 1,
    "uploadedByName": "Admin User",
    "createdAt": "2026-06-17T19:18:52.000",
    "updatedAt": null
  }
]
```

### Get Document Metadata

The authenticated user must be the project creator or a registered project member.

```http
GET /api/documents/{documentId}
Authorization: Bearer <token>
```

### Download Document

The authenticated user must be the project creator or a registered project member.

```http
GET /api/documents/{documentId}/download
Authorization: Bearer <token>
```

Successful response:

```text
200 OK
Content-Disposition: attachment; filename="site-plan.pdf"
```

The response body is the binary file content.

### Delete Document

The authenticated user must be the project creator or a project member with role `MANAGER`.

```http
DELETE /api/documents/{documentId}
Authorization: Bearer <token>
```

Successful response:

```text
200 OK
```

The response body is empty.

## Dashboard API

Dashboard statistics are scoped to the authenticated user. The API counts projects the user created and projects where the user is registered.

The live statistics endpoint does not write to the database. Snapshot endpoints use the `dashboard_statistics_snapshots` table.

### Dashboard Statistics Object

```json
{
  "snapshotId": null,
  "userId": 2,
  "userName": "Dashboard User",
  "totalProjects": 3,
  "activeProjects": 1,
  "completedProjects": 1,
  "totalTasks": 8,
  "openTasks": 5,
  "completedTasks": 3,
  "overdueTasks": 2,
  "totalRisks": 6,
  "openRisks": 4,
  "highRisks": 2,
  "criticalRisks": 1,
  "progressReports": 7,
  "documents": 9,
  "generatedAt": "2026-06-17T20:04:31.000"
}
```

### Get Live Dashboard Statistics

Returns current aggregate statistics for the authenticated user.

```http
GET /api/dashboard/statistics
Authorization: Bearer <token>
```

Included counts:

| Field | Description |
| --- | --- |
| `totalProjects` | Accessible project count. |
| `activeProjects` | Accessible projects with status `ACTIVE`. |
| `completedProjects` | Accessible projects with status `COMPLETED`. |
| `totalTasks` | Tasks under accessible projects. |
| `openTasks` | Tasks under accessible projects where status is not `DONE`. |
| `completedTasks` | Tasks under accessible projects with status `DONE`. |
| `overdueTasks` | Tasks under accessible projects with due date before today and status not `DONE`. |
| `totalRisks` | Risks under accessible projects. |
| `openRisks` | Risks under accessible projects where status is not `CLOSED`. |
| `highRisks` | Risks under accessible projects with risk level `HIGH`. |
| `criticalRisks` | Risks under accessible projects with risk level `CRITICAL`. |
| `progressReports` | Progress reports under accessible projects. |
| `documents` | Documents under accessible projects. |

### Create Dashboard Statistics Snapshot

Calculates current statistics and stores them in `dashboard_statistics_snapshots`.

```http
POST /api/dashboard/statistics/snapshots
Authorization: Bearer <token>
```

Example response:

```json
{
  "snapshotId": 100,
  "userId": 2,
  "userName": "Dashboard User",
  "totalProjects": 3,
  "activeProjects": 1,
  "completedProjects": 1,
  "totalTasks": 8,
  "openTasks": 5,
  "completedTasks": 3,
  "overdueTasks": 2,
  "totalRisks": 6,
  "openRisks": 4,
  "highRisks": 2,
  "criticalRisks": 1,
  "progressReports": 7,
  "documents": 9,
  "generatedAt": "2026-06-17T20:04:31.000"
}
```

### Get Latest Dashboard Statistics Snapshot

Returns the latest saved snapshot for the authenticated user.

```http
GET /api/dashboard/statistics/snapshots/latest
Authorization: Bearer <token>
```

Failure cases:

| Status | Reason |
| --- | --- |
| `404` | The user has no saved dashboard statistics snapshot. |

## Quick Workflow Example

1. Register or log in as a user.
2. Ensure one user has role `ADMIN`.
3. Log in as the admin user.
4. Create a project with `POST /api/projects`.
5. Add users to the project through `members` during creation or through `POST /api/projects/{projectId}/registrations`.
6. Use `role: "MANAGER"` for users who should manage the project.
7. Use `role: "MEMBER"` for users who should only access project data.

Example manager registration:

```json
{
  "userId": 3,
  "title": "Project Manager",
  "description": "Can manage project data and members.",
  "role": "MANAGER"
}
```

Example read-only project member registration:

```json
{
  "userId": 2,
  "title": "Site Engineer",
  "description": "Can access project data.",
  "role": "MEMBER"
}
```
