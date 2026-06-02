# Commit Message

```text
feat(project): add project registration access control
```

# Pull Request

## Title

```text
feat(project): add project registration access control
```

## Summary

This PR adds project-level access control backed by the `user_project_registrations` table. A user can now access a project, its tasks, and its progress reports when they are either the project creator or registered to that project.

## Changes

- Added `UserProjectRegistration` entity and repository.
- Added project registration request and response DTOs.
- Added `ProjectAccessService` to centralize project access checks.
- Updated project detail/update/delete to allow project creators and registered users.
- Updated task create/list/read/update/delete to check access through the related project.
- Updated progress report create/list/read/update/delete to check access through the related project.
- Added registration management APIs under `/api/projects/{projectId}/registrations`.
- Restricted registration management to project creators only.
- Added duplicate registration protection.
- Added 403 Forbidden handling for access-denied requests.
- Updated current-user lookup to use the authenticated JWT principal.
- Added service tests for project access checks and registration management rules.

## API Endpoints

```http
POST   /api/projects/{projectId}/registrations
GET    /api/projects/{projectId}/registrations
DELETE /api/projects/{projectId}/registrations/{registrationId}
```

## Access Rules

- Project creator can access the project.
- Registered project users can access the project.
- Project creator can manage registrations.
- Registered users cannot manage registrations.
- Users without project access receive `403 Forbidden`.
- Missing resources return `404 Not Found`.
- Duplicate registrations return `400 Bad Request`.

## Validation

Ran:

```bash
.\mvnw.cmd test
```

Result:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Postman Coverage

Postman test cases are documented in:

```text
docs/project-access-control-postman-tests.md
```

Covered scenarios:

- User B cannot access User A's project before registration.
- User A registers User B to the project.
- User B can access the project after registration.
- User B can create/list/read/update/delete tasks under the project.
- User B can create/list/read/update/delete progress reports under the project.
- User B cannot add or remove registrations.
- User A can remove User B's registration.
- User B loses project access after registration removal.
- Duplicate registrations return 400.
- Missing user registration returns 404.

## Database Note

The expected table definition uses `BIGSERIAL` for `id` and a unique constraint on `(user_id, project_id)`.

```sql
CREATE TABLE user_project_registrations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    title VARCHAR(150),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_upr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_upr_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uk_upr_user_project UNIQUE (user_id, project_id)
);
```

If the table already exists without a unique constraint, add:

```sql
ALTER TABLE user_project_registrations
ADD CONSTRAINT uk_user_project_registration
UNIQUE (user_id, project_id);
```

