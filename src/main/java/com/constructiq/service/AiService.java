package com.constructiq.service;

import com.constructiq.dto.request.AiAdviceRequest;
import com.constructiq.dto.request.AiChatMessageRequest;
import com.constructiq.dto.request.AiChatRequest;
import com.constructiq.dto.response.AiAdviceResponse;
import com.constructiq.dto.response.AiChatResponse;
import com.constructiq.dto.response.AiContextSnippetResponse;
import com.constructiq.entity.Document;
import com.constructiq.entity.ProgressReport;
import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import com.constructiq.entity.Task;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import com.constructiq.exception.AiProviderException;
import com.constructiq.repository.DocumentRepository;
import com.constructiq.repository.ProgressReportRepository;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.RiskRepository;
import com.constructiq.repository.TaskRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final int MAX_CONTEXT_SNIPPETS = 8;
    private static final int MAX_PROJECT_SNIPPETS = 12;
    private static final int MAX_TASK_SNIPPETS = 20;
    private static final int MAX_RISK_SNIPPETS = 20;
    private static final int MAX_REPORT_SNIPPETS = 15;
    private static final int MAX_DOCUMENT_SNIPPETS = 10;
    private static final int MAX_SNIPPET_CHARS = 1200;
    private static final int MAX_EMBEDDING_TEXT_CHARS = 1500;
    private static final int MAX_RESPONSE_EXCERPT_CHARS = 500;
    private static final int MAX_HISTORY_MESSAGES = 10;

    private static final Set<String> CHAT_HISTORY_ROLES = Set.of("user", "assistant");

    private static final String SYSTEM_PROMPT = """
            You are ConstructIQ's construction project management assistant.
            Use only the provided project context when answering project-specific questions.
            If the context is insufficient, say what is missing instead of inventing details.
            Prioritize practical construction guidance for safety, schedule, cost, quality, risk, and next actions.
            For safety, legal, or contractual issues, flag that qualified professional review is needed.
            """;

    private final AiProviderClient aiProviderClient;
    private final ProjectRepository projectRepository;
    private final UserProjectRegistrationRepository registrationRepository;
    private final TaskRepository taskRepository;
    private final RiskRepository riskRepository;
    private final ProgressReportRepository progressReportRepository;
    private final DocumentRepository documentRepository;
    private final ProjectAccessService projectAccessService;
    private final Utils utils;

    public AiChatResponse chat(AiChatRequest request, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        List<Project> projects = resolveProjects(request.getProjectId(), currentUser);
        List<ContextSnippet> context = selectRelevantContext(
                request.getMessage(),
                buildContextSnippets(projects)
        );

        List<AiProviderClient.ChatMessage> messages = buildChatMessages(request, context);
        String answer = aiProviderClient.chat(messages);

        return AiChatResponse.builder()
                .answer(answer)
                .projectId(request.getProjectId())
                .chatModel(aiProviderClient.getChatModel())
                .embedModel(aiProviderClient.getEmbedModel())
                .generatedAt(LocalDateTime.now())
                .context(toContextResponses(context))
                .build();
    }

    public AiAdviceResponse advice(AiAdviceRequest request, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        List<Project> projects = resolveProjects(request.getProjectId(), currentUser);

        String focus = isBlank(request.getFocus())
                ? "overall project delivery, safety, schedule, cost, quality, risks, and next actions"
                : request.getFocus().trim();

        List<ContextSnippet> context = selectRelevantContext(
                focus + " construction project advice risk schedule cost quality task progress report",
                buildContextSnippets(projects)
        );

        String userPrompt = """
                CONTEXT:
                %s

                ADVICE REQUEST:
                Generate concise project management advice for the accessible construction project data.
                Focus: %s

                Return the answer with clear priorities, risks to watch, and practical next actions.
                """.formatted(formatContext(context), focus);

        String advice = aiProviderClient.chat(List.of(
                new AiProviderClient.ChatMessage("system", SYSTEM_PROMPT),
                new AiProviderClient.ChatMessage("user", userPrompt)
        ));

        return AiAdviceResponse.builder()
                .advice(advice)
                .projectId(request.getProjectId())
                .chatModel(aiProviderClient.getChatModel())
                .embedModel(aiProviderClient.getEmbedModel())
                .generatedAt(LocalDateTime.now())
                .context(toContextResponses(context))
                .build();
    }

    private List<Project> resolveProjects(Long projectId, User currentUser) {
        if (projectId != null) {
            return List.of(projectAccessService.getProjectWithAccessCheck(projectId, currentUser));
        }

        return getAccessibleProjects(currentUser);
    }

    private List<Project> getAccessibleProjects(User currentUser) {
        Map<Long, Project> accessibleProjects = new LinkedHashMap<>();

        projectRepository.findByCreatedByOrderByCreatedAtDesc(currentUser)
                .forEach(project -> accessibleProjects.put(project.getId(), project));

        registrationRepository.findByUser(currentUser)
                .stream()
                .map(UserProjectRegistration::getProject)
                .forEach(project -> accessibleProjects.putIfAbsent(project.getId(), project));

        return List.copyOf(accessibleProjects.values());
    }

    private List<ContextSnippet> buildContextSnippets(List<Project> projects) {
        if (projects.isEmpty()) {
            return List.of();
        }

        List<ContextSnippet> snippets = new ArrayList<>();

        projects.stream()
                .limit(MAX_PROJECT_SNIPPETS)
                .map(this::projectSnippet)
                .forEach(snippets::add);

        getTasks(projects).stream()
                .sorted(Comparator.comparingInt(this::taskImportance).reversed())
                .limit(MAX_TASK_SNIPPETS)
                .map(this::taskSnippet)
                .forEach(snippets::add);

        getRisks(projects).stream()
                .sorted(Comparator.comparingInt(this::riskImportance).reversed())
                .limit(MAX_RISK_SNIPPETS)
                .map(this::riskSnippet)
                .forEach(snippets::add);

        getProgressReports(projects).stream()
                .limit(MAX_REPORT_SNIPPETS)
                .map(this::progressReportSnippet)
                .forEach(snippets::add);

        getDocuments(projects).stream()
                .limit(MAX_DOCUMENT_SNIPPETS)
                .map(this::documentSnippet)
                .forEach(snippets::add);

        return snippets;
    }

    private List<Task> getTasks(List<Project> projects) {
        if (projects.size() == 1) {
            return taskRepository.findByProjectOrderByCreatedAtDesc(projects.get(0));
        }

        return taskRepository.findAccessibleTasks(projects, null, null);
    }

    private List<Risk> getRisks(List<Project> projects) {
        if (projects.size() == 1) {
            return riskRepository.findByProjectOrderByCreatedAtDesc(projects.get(0));
        }

        return riskRepository.findAccessibleRisks(projects, null, null, null);
    }

    private List<ProgressReport> getProgressReports(List<Project> projects) {
        if (projects.size() == 1) {
            return progressReportRepository.findByProjectOrderByReportDateDesc(projects.get(0));
        }

        return progressReportRepository.findAccessibleProgressReports(projects);
    }

    private List<Document> getDocuments(List<Project> projects) {
        if (projects.size() == 1) {
            return documentRepository.findByProjectOrderByCreatedAtDesc(projects.get(0));
        }

        return documentRepository.findByProjectInOrderByCreatedAtDesc(projects);
    }

    private List<ContextSnippet> selectRelevantContext(String query, List<ContextSnippet> candidates) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        try {
            List<Double> queryEmbedding = aiProviderClient.embed(abbreviate(query, MAX_EMBEDDING_TEXT_CHARS));

            return candidates.stream()
                    .map(snippet -> snippet.withScore(cosineSimilarity(
                            queryEmbedding,
                            aiProviderClient.embed(abbreviate(snippet.content(), MAX_EMBEDDING_TEXT_CHARS))
                    )))
                    .sorted(Comparator.comparingDouble(ContextSnippet::score).reversed())
                    .limit(MAX_CONTEXT_SNIPPETS)
                    .toList();
        } catch (AiProviderException ex) {
            return selectContextByKeywords(query, candidates);
        }
    }

    private List<ContextSnippet> selectContextByKeywords(String query, List<ContextSnippet> candidates) {
        Set<String> queryTerms = tokenize(query);

        return candidates.stream()
                .map(snippet -> snippet.withScore(keywordScore(queryTerms, snippet)))
                .sorted(Comparator.comparingDouble(ContextSnippet::score).reversed())
                .limit(MAX_CONTEXT_SNIPPETS)
                .toList();
    }

    private List<AiProviderClient.ChatMessage> buildChatMessages(
            AiChatRequest request,
            List<ContextSnippet> context
    ) {
        List<AiProviderClient.ChatMessage> messages = new ArrayList<>();
        messages.add(new AiProviderClient.ChatMessage("system", SYSTEM_PROMPT));

        List<AiChatMessageRequest> history = request.getHistory() == null
                ? List.of()
                : request.getHistory();

        int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        for (AiChatMessageRequest historyMessage : history.subList(start, history.size())) {
            String role = historyMessage.getRole();
            if (!CHAT_HISTORY_ROLES.contains(role)) {
                throw new IllegalArgumentException("Message role must be user or assistant");
            }

            messages.add(new AiProviderClient.ChatMessage(
                    role,
                    abbreviate(historyMessage.getContent(), 4000)
            ));
        }

        String userPrompt = """
                CONTEXT:
                %s

                USER QUESTION:
                %s
                """.formatted(formatContext(context), request.getMessage().trim());

        messages.add(new AiProviderClient.ChatMessage("user", userPrompt));

        return messages;
    }

    private String formatContext(List<ContextSnippet> context) {
        if (context.isEmpty()) {
            return "No accessible project data is available.";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < context.size(); i++) {
            ContextSnippet snippet = context.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(snippet.sourceType())
                    .append(" #")
                    .append(snippet.sourceId())
                    .append(" | Project #")
                    .append(snippet.projectId())
                    .append(" ")
                    .append(snippet.projectName())
                    .append(" | ")
                    .append(snippet.title())
                    .append("\n")
                    .append(snippet.content())
                    .append("\n\n");
        }

        return builder.toString().trim();
    }

    private List<AiContextSnippetResponse> toContextResponses(List<ContextSnippet> context) {
        return context.stream()
                .map(snippet -> AiContextSnippetResponse.builder()
                        .sourceType(snippet.sourceType())
                        .sourceId(snippet.sourceId())
                        .projectId(snippet.projectId())
                        .projectName(snippet.projectName())
                        .title(snippet.title())
                        .score(roundScore(snippet.score()))
                        .excerpt(abbreviate(snippet.content(), MAX_RESPONSE_EXCERPT_CHARS))
                        .build())
                .toList();
    }

    private ContextSnippet projectSnippet(Project project) {
        StringBuilder content = new StringBuilder("Project data.");
        appendField(content, "Name", project.getName());
        appendField(content, "Description", project.getDescription());
        appendField(content, "Location", project.getLocation());
        appendField(content, "Client", project.getClientName());
        appendField(content, "Status", project.getStatus());
        appendField(content, "Start date", project.getStartDate());
        appendField(content, "End date", project.getEndDate());

        return new ContextSnippet(
                "PROJECT",
                project.getId(),
                project.getId(),
                project.getName(),
                "Project: " + project.getName(),
                abbreviate(content.toString(), MAX_SNIPPET_CHARS),
                0.0
        );
    }

    private ContextSnippet taskSnippet(Task task) {
        Project project = task.getProject();
        StringBuilder content = new StringBuilder("Task data.");
        appendField(content, "Title", task.getTitle());
        appendField(content, "Description", task.getDescription());
        appendField(content, "Status", task.getStatus());
        appendField(content, "Priority", task.getPriority());
        appendField(content, "Assignee", task.getAssignee());
        appendField(content, "Due date", task.getDueDate());

        return new ContextSnippet(
                "TASK",
                task.getId(),
                project.getId(),
                project.getName(),
                "Task: " + task.getTitle(),
                abbreviate(content.toString(), MAX_SNIPPET_CHARS),
                0.0
        );
    }

    private ContextSnippet riskSnippet(Risk risk) {
        Project project = risk.getProject();
        StringBuilder content = new StringBuilder("Risk data.");
        appendField(content, "Title", risk.getTitle());
        appendField(content, "Description", risk.getDescription());
        appendField(content, "Category", risk.getCategory());
        appendField(content, "Probability", risk.getProbability());
        appendField(content, "Impact", risk.getImpact());
        appendField(content, "Severity", risk.getSeverity());
        appendField(content, "Risk level", risk.getRiskLevel());
        appendField(content, "Status", risk.getStatus());
        appendField(content, "Mitigation plan", risk.getMitigationPlan());
        appendField(content, "Owner", risk.getOwner());
        appendField(content, "Target date", risk.getTargetDate());

        return new ContextSnippet(
                "RISK",
                risk.getId(),
                project.getId(),
                project.getName(),
                "Risk: " + risk.getTitle(),
                abbreviate(content.toString(), MAX_SNIPPET_CHARS),
                0.0
        );
    }

    private ContextSnippet progressReportSnippet(ProgressReport progressReport) {
        Project project = progressReport.getProject();
        StringBuilder content = new StringBuilder("Progress report data.");
        appendField(content, "Report date", progressReport.getReportDate());
        appendField(content, "Summary", progressReport.getSummary());
        appendField(content, "Completed work", progressReport.getCompletedWork());
        appendField(content, "Delayed work", progressReport.getDelayedWork());
        appendField(content, "Issues", progressReport.getIssues());
        appendField(content, "Next actions", progressReport.getNextActions());

        return new ContextSnippet(
                "PROGRESS_REPORT",
                progressReport.getId(),
                project.getId(),
                project.getName(),
                "Progress report: " + progressReport.getReportDate(),
                abbreviate(content.toString(), MAX_SNIPPET_CHARS),
                0.0
        );
    }

    private ContextSnippet documentSnippet(Document document) {
        Project project = document.getProject();
        StringBuilder content = new StringBuilder("Document metadata.");
        appendField(content, "File name", document.getFileName());
        appendField(content, "File type", document.getFileType());
        appendField(content, "File size bytes", document.getFileSize());
        appendField(content, "Uploaded at", document.getCreatedAt());

        return new ContextSnippet(
                "DOCUMENT",
                document.getId(),
                project.getId(),
                project.getName(),
                "Document: " + document.getFileName(),
                abbreviate(content.toString(), MAX_SNIPPET_CHARS),
                0.0
        );
    }

    private int taskImportance(Task task) {
        int score = priorityRank(task.getPriority()) * 10;

        if (task.getStatus() == TaskStatus.BLOCKED) {
            score += 50;
        }

        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            score += 40;
        }

        if (task.getStatus() != TaskStatus.DONE) {
            score += 10;
        }

        return score;
    }

    private int riskImportance(Risk risk) {
        int score = riskLevelRank(risk.getRiskLevel()) * 20;

        if (risk.getSeverity() != null) {
            score += risk.getSeverity();
        }

        if (risk.getTargetDate() != null && !risk.getTargetDate().isAfter(LocalDate.now().plusDays(14))) {
            score += 10;
        }

        return score;
    }

    private int priorityRank(TaskPriority priority) {
        if (priority == null) {
            return 0;
        }

        return switch (priority) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private int riskLevelRank(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return 0;
        }

        return switch (riskLevel) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private double cosineSimilarity(List<Double> left, List<Double> right) {
        int dimensions = Math.min(left.size(), right.size());
        if (dimensions == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;

        for (int i = 0; i < dimensions; i++) {
            double leftValue = left.get(i);
            double rightValue = right.get(i);

            dotProduct += leftValue * rightValue;
            leftNorm += leftValue * leftValue;
            rightNorm += rightValue * rightValue;
        }

        if (leftNorm == 0.0 || rightNorm == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private double keywordScore(Set<String> queryTerms, ContextSnippet snippet) {
        if (queryTerms.isEmpty()) {
            return 0.0;
        }

        Set<String> contentTerms = tokenize(snippet.title() + " " + snippet.content());
        long matches = queryTerms.stream()
                .filter(contentTerms::contains)
                .count();

        return (double) matches / queryTerms.size();
    }

    private Set<String> tokenize(String text) {
        Set<String> terms = new LinkedHashSet<>();

        if (isBlank(text)) {
            return terms;
        }

        for (String token : text.toLowerCase().split("[^a-z0-9]+")) {
            if (token.length() > 2) {
                terms.add(token);
            }
        }

        return terms;
    }

    private void appendField(StringBuilder builder, String label, Object value) {
        if (value == null) {
            return;
        }

        String text = value.toString().trim();
        if (text.isBlank()) {
            return;
        }

        builder.append(' ')
                .append(label)
                .append(": ")
                .append(text.replaceAll("\\s+", " "))
                .append('.');
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        String normalized = text.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }

        if (maxLength <= 3) {
            return normalized.substring(0, maxLength);
        }

        return normalized.substring(0, maxLength - 3) + "...";
    }

    private Double roundScore(double score) {
        return Math.round(score * 1000.0) / 1000.0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ContextSnippet(
            String sourceType,
            Long sourceId,
            Long projectId,
            String projectName,
            String title,
            String content,
            double score
    ) {

        private ContextSnippet withScore(double score) {
            return new ContextSnippet(
                    sourceType,
                    sourceId,
                    projectId,
                    projectName,
                    title,
                    content,
                    score
            );
        }
    }
}
