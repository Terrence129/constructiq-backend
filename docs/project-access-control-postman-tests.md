# Project Access Control Postman Tests

## Environment Variables

Create a Postman environment with these variables:

```text
baseUrl=http://localhost:8080
userAEmail=user-a@example.com
userBEmail=user-b@example.com
password=Password123!
userAToken=
userBToken=
userAId=
userBId=
projectId=
taskId=
progressReportId=
registrationId=
```

Use `Bearer {{userAToken}}` or `Bearer {{userBToken}}` in the Authorization header for protected requests.

## 1. Register User A

```http
POST {{baseUrl}}/api/auth/register
Content-Type: application/json
```

```json
{
  "name": "User A",
  "email": "{{userAEmail}}",
  "password": "{{password}}"
}
```

Tests:

```javascript
pm.test("register user A returns token", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.token).to.be.a("string").and.not.empty;
  pm.expect(body.user.id).to.exist;
  pm.environment.set("userAToken", body.token);
  pm.environment.set("userAId", body.user.id);
});
```

## 2. Register User B

```http
POST {{baseUrl}}/api/auth/register
Content-Type: application/json
```

```json
{
  "name": "User B",
  "email": "{{userBEmail}}",
  "password": "{{password}}"
}
```

Tests:

```javascript
pm.test("register user B returns token", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.token).to.be.a("string").and.not.empty;
  pm.expect(body.user.id).to.exist;
  pm.environment.set("userBToken", body.token);
  pm.environment.set("userBId", body.user.id);
});
```

## 3. User A Creates Project

```http
POST {{baseUrl}}/api/projects
Authorization: Bearer {{userAToken}}
Content-Type: application/json
```

```json
{
  "name": "Access Control Test Project",
  "description": "Project used for user_project_registrations access tests.",
  "location": "Singapore",
  "clientName": "ConstructIQ Test Client",
  "status": "PLANNING",
  "startDate": "2026-06-02",
  "endDate": "2026-12-31"
}
```

Tests:

```javascript
pm.test("user A can create project", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.id).to.exist;
  pm.expect(body.createdById).to.eql(Number(pm.environment.get("userAId")));
  pm.environment.set("projectId", body.id);
});
```

## 4. Scenario A: User B Cannot Access Unregistered Project

```http
GET {{baseUrl}}/api/projects/{{projectId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("unregistered user B gets 403 for project detail", function () {
  pm.response.to.have.status(403);
  const body = pm.response.json();
  pm.expect(body.error).to.eql("Forbidden");
});
```

## 5. User A Registers User B To Project

```http
POST {{baseUrl}}/api/projects/{{projectId}}/registrations
Authorization: Bearer {{userAToken}}
Content-Type: application/json
```

```json
{
  "userId": {{userBId}},
  "title": "Site Engineer",
  "description": "Responsible for weekly progress reporting and task updates."
}
```

Tests:

```javascript
pm.test("project creator can register user B", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.id).to.exist;
  pm.expect(body.userId).to.eql(Number(pm.environment.get("userBId")));
  pm.expect(body.projectId).to.eql(Number(pm.environment.get("projectId")));
  pm.environment.set("registrationId", body.id);
});
```

## 6. Duplicate Registration Returns 400

Repeat the same request from step 5.

Tests:

```javascript
pm.test("duplicate registration returns 400", function () {
  pm.response.to.have.status(400);
  const body = pm.response.json();
  pm.expect(body.error).to.eql("Bad Request");
});
```

## 7. Scenario B: User B Can View Project

```http
GET {{baseUrl}}/api/projects/{{projectId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can view project", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.id).to.eql(Number(pm.environment.get("projectId")));
});
```

## 8. User B Can Create Task

```http
POST {{baseUrl}}/api/projects/{{projectId}}/tasks
Authorization: Bearer {{userBToken}}
Content-Type: application/json
```

```json
{
  "title": "Inspect foundation works",
  "description": "Confirm foundation preparation is complete.",
  "status": "TODO",
  "priority": "HIGH",
  "assignee": "User B",
  "dueDate": "2026-06-10"
}
```

Tests:

```javascript
pm.test("registered user B can create task", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.id).to.exist;
  pm.expect(body.projectId).to.eql(Number(pm.environment.get("projectId")));
  pm.environment.set("taskId", body.id);
});
```

## 9. User B Can List, Read, Update, And Delete Task

List:

```http
GET {{baseUrl}}/api/projects/{{projectId}}/tasks
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can list tasks", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json()).to.be.an("array");
});
```

Read:

```http
GET {{baseUrl}}/api/tasks/{{taskId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can read task", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().id).to.eql(Number(pm.environment.get("taskId")));
});
```

Update:

```http
PUT {{baseUrl}}/api/tasks/{{taskId}}
Authorization: Bearer {{userBToken}}
Content-Type: application/json
```

```json
{
  "title": "Inspect foundation works",
  "description": "Inspection completed.",
  "status": "DONE",
  "priority": "HIGH",
  "assignee": "User B",
  "dueDate": "2026-06-10"
}
```

Tests:

```javascript
pm.test("registered user B can update task", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().status).to.eql("DONE");
});
```

Delete:

```http
DELETE {{baseUrl}}/api/tasks/{{taskId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can delete task", function () {
  pm.response.to.have.status(200);
});
```

## 10. User B Can Create Progress Report

```http
POST {{baseUrl}}/api/projects/{{projectId}}/progressReports
Authorization: Bearer {{userBToken}}
Content-Type: application/json
```

```json
{
  "reportDate": "2026-06-02",
  "summary": "Weekly progress update",
  "completedWork": "Foundation inspection completed.",
  "delayedWork": "None",
  "issues": "None",
  "nextActions": "Prepare next inspection checklist."
}
```

Tests:

```javascript
pm.test("registered user B can create progress report", function () {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  pm.expect(body.id).to.exist;
  pm.expect(body.projectId).to.eql(Number(pm.environment.get("projectId")));
  pm.environment.set("progressReportId", body.id);
});
```

## 11. User B Can List, Read, Update, And Delete Progress Report

List:

```http
GET {{baseUrl}}/api/projects/{{projectId}}/progressReports
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can list progress reports", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json()).to.be.an("array");
});
```

Read:

```http
GET {{baseUrl}}/api/progressReports/{{progressReportId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can read progress report", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().id).to.eql(Number(pm.environment.get("progressReportId")));
});
```

Update:

```http
PUT {{baseUrl}}/api/progressReports/{{progressReportId}}
Authorization: Bearer {{userBToken}}
Content-Type: application/json
```

```json
{
  "reportDate": "2026-06-03",
  "summary": "Updated weekly progress report",
  "completedWork": "Foundation inspection and report update completed.",
  "delayedWork": "None",
  "issues": "None",
  "nextActions": "Schedule next site walkthrough."
}
```

Tests:

```javascript
pm.test("registered user B can update progress report", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().summary).to.eql("Updated weekly progress report");
});
```

Delete:

```http
DELETE {{baseUrl}}/api/progressReports/{{progressReportId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("registered user B can delete progress report", function () {
  pm.response.to.have.status(200);
});
```

## 12. Scenario C: User B Cannot Manage Registrations

```http
POST {{baseUrl}}/api/projects/{{projectId}}/registrations
Authorization: Bearer {{userBToken}}
Content-Type: application/json
```

```json
{
  "userId": {{userAId}},
  "title": "Project Manager",
  "description": "Attempted registration by a non-creator."
}
```

Tests:

```javascript
pm.test("registered user B cannot manage registrations", function () {
  pm.response.to.have.status(403);
  const body = pm.response.json();
  pm.expect(body.error).to.eql("Forbidden");
});
```

## 13. Scenario D: User A Removes User B Registration

```http
DELETE {{baseUrl}}/api/projects/{{projectId}}/registrations/{{registrationId}}
Authorization: Bearer {{userAToken}}
```

Tests:

```javascript
pm.test("project creator can remove user B registration", function () {
  pm.response.to.have.status(200);
});
```

## 14. User B Can No Longer Access Project

```http
GET {{baseUrl}}/api/projects/{{projectId}}
Authorization: Bearer {{userBToken}}
```

Tests:

```javascript
pm.test("removed user B gets 403 for project detail", function () {
  pm.response.to.have.status(403);
  const body = pm.response.json();
  pm.expect(body.error).to.eql("Forbidden");
});
```

## 15. Missing User Registration Returns 404

```http
POST {{baseUrl}}/api/projects/{{projectId}}/registrations
Authorization: Bearer {{userAToken}}
Content-Type: application/json
```

```json
{
  "userId": 999999,
  "title": "Missing User",
  "description": "This should return 404."
}
```

Tests:

```javascript
pm.test("missing user returns 404", function () {
  pm.response.to.have.status(404);
  const body = pm.response.json();
  pm.expect(body.error).to.eql("Not Found");
});
```

