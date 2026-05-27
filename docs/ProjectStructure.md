src/main/java/com/constructiq
│
├── ConstructiqBackendApplication.java
│
├── controller
│   ├── HealthController.java
│   ├── AuthController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   ├── RiskController.java
│   ├── ReportController.java
│   ├── DocumentController.java
│   └── AiController.java
│
├── service
│   ├── AuthService.java
│   ├── ProjectService.java
│   ├── TaskService.java
│   ├── RiskService.java
│   ├── ReportService.java
│   ├── DocumentService.java
│   └── AiService.java
│
├── repository
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   ├── RiskRepository.java
│   ├── ProgressReportRepository.java
│   ├── DocumentRepository.java
│   └── AiAnalysisResultRepository.java
│
├── entity
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   ├── Risk.java
│   ├── ProgressReport.java
│   ├── Document.java
│   └── AiAnalysisResult.java
│
├── dto
│   ├── request
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── ProjectRequest.java
│   │   ├── TaskRequest.java
│   │   ├── RiskRequest.java
│   │   └── ProgressReportRequest.java
│   │
│   └── response
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── ProjectResponse.java
│       ├── TaskResponse.java
│       ├── RiskResponse.java
│       ├── ProgressReportResponse.java
│       └── AiAnalysisResponse.java
│
├── config
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── CorsConfig.java
│
├── security
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── ErrorResponse.java
│
├── enums
│   ├── UserRole.java
│   ├── ProjectStatus.java
│   ├── TaskStatus.java
│   ├── TaskPriority.java
│   ├── RiskCategory.java
│   └── RiskStatus.java
│
└── util
├── DateTimeUtil.java
└── FileUtil.java