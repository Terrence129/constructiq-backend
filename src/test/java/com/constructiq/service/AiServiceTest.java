package com.constructiq.service;

import com.constructiq.dto.request.AiAdviceRequest;
import com.constructiq.dto.request.AiChatRequest;
import com.constructiq.dto.response.AiAdviceResponse;
import com.constructiq.dto.response.AiChatResponse;
import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import com.constructiq.entity.Task;
import com.constructiq.entity.User;
import com.constructiq.enums.ProjectStatus;
import com.constructiq.enums.RiskCategory;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiProviderClient aiProviderClient;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserProjectRegistrationRepository registrationRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private ProgressReportRepository progressReportRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private Utils utils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AiService aiService;

    @Test
    void chatRanksAuthorizedContextWithEmbeddings() {
        User user = User.builder().id(1L).name("Current User").build();
        Project project = project();
        Risk risk = risk(project, user);
        Task task = task(project);
        AiChatRequest request = new AiChatRequest();
        request.setMessage("What should we do about steel delivery?");

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(project));
        when(registrationRepository.findByUser(user)).thenReturn(List.of());
        when(taskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of(task));
        when(riskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of(risk));
        when(progressReportRepository.findByProjectOrderByReportDateDesc(project)).thenReturn(List.of());
        when(documentRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        when(aiProviderClient.embed(anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(0);
            if (text.contains("Steel delivery delay")) {
                return List.of(1.0, 0.0);
            }

            if (text.contains("Project data")) {
                return List.of(0.0, 1.0);
            }

            if (text.contains("Task data")) {
                return List.of(0.2, 0.8);
            }

            return List.of(1.0, 0.0);
        });
        when(aiProviderClient.chat(anyList())).thenReturn("Prioritize alternate supplier confirmation.");
        when(aiProviderClient.getChatModel()).thenReturn("llama3.1");
        when(aiProviderClient.getEmbedModel()).thenReturn("all-MiniLM-L6-v2");

        AiChatResponse response = aiService.chat(request, authentication);

        assertThat(response.getAnswer()).isEqualTo("Prioritize alternate supplier confirmation.");
        assertThat(response.getChatModel()).isEqualTo("llama3.1");
        assertThat(response.getEmbedModel()).isEqualTo("all-MiniLM-L6-v2");
        assertThat(response.getContext()).isNotEmpty();
        assertThat(response.getContext().get(0).getSourceType()).isEqualTo("RISK");
        assertThat(response.getContext().get(0).getTitle()).contains("Steel delivery delay");
    }

    @Test
    void adviceUsesRequestedProjectAccessAndFallsBackWhenEmbeddingsAreUnavailable() {
        User user = User.builder().id(1L).name("Current User").build();
        Project project = project();
        AiAdviceRequest request = new AiAdviceRequest();
        request.setProjectId(10L);
        request.setFocus("schedule risk");

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectAccessService.getProjectWithAccessCheck(10L, user)).thenReturn(project);
        when(taskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        when(riskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        when(progressReportRepository.findByProjectOrderByReportDateDesc(project)).thenReturn(List.of());
        when(documentRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        doThrow(new AiProviderException("AI embedding model is unavailable"))
                .when(aiProviderClient)
                .embed(anyString());
        when(aiProviderClient.chat(anyList())).thenReturn("Review the current schedule and confirm constraints.");
        when(aiProviderClient.getChatModel()).thenReturn("llama3.1");
        when(aiProviderClient.getEmbedModel()).thenReturn("all-MiniLM-L6-v2");

        AiAdviceResponse response = aiService.advice(request, authentication);

        assertThat(response.getAdvice()).isEqualTo("Review the current schedule and confirm constraints.");
        assertThat(response.getProjectId()).isEqualTo(10L);
        assertThat(response.getContext()).hasSize(1);
        assertThat(response.getContext().get(0).getSourceType()).isEqualTo("PROJECT");
        verify(projectAccessService).getProjectWithAccessCheck(10L, user);
    }

    @SuppressWarnings("unchecked")
    @Test
    void chatIncludesContextInProviderPrompt() {
        User user = User.builder().id(1L).name("Current User").build();
        Project project = project();
        AiChatRequest request = new AiChatRequest();
        request.setMessage("Summarize the harbour tower status");

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(project));
        when(registrationRepository.findByUser(user)).thenReturn(List.of());
        when(taskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        when(riskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        when(progressReportRepository.findByProjectOrderByReportDateDesc(project)).thenReturn(List.of());
        when(documentRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of());
        when(aiProviderClient.embed(anyString())).thenReturn(List.of(1.0));
        when(aiProviderClient.chat(anyList())).thenReturn("Harbour Tower is active.");

        aiService.chat(request, authentication);

        ArgumentCaptor<List<AiProviderClient.ChatMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiProviderClient).chat(captor.capture());

        List<AiProviderClient.ChatMessage> messages = captor.getValue();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(1).content()).contains("CONTEXT:");
        assertThat(messages.get(1).content()).contains("Project: Harbour Tower");
        assertThat(messages.get(1).content()).contains("USER QUESTION:");
    }

    private Project project() {
        User creator = User.builder().id(1L).name("Admin User").build();

        return Project.builder()
                .id(10L)
                .name("Harbour Tower")
                .description("Mixed-use construction project.")
                .location("Singapore")
                .clientName("ConstructIQ Client")
                .status(ProjectStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusMonths(6))
                .createdBy(creator)
                .build();
    }

    private Risk risk(Project project, User createdBy) {
        return Risk.builder()
                .id(100L)
                .project(project)
                .title("Steel delivery delay")
                .description("Structural steel delivery may delay critical path work.")
                .category(RiskCategory.SCHEDULE)
                .probability(4)
                .impact(5)
                .severity(20)
                .riskLevel(RiskLevel.CRITICAL)
                .status(RiskStatus.OPEN)
                .mitigationPlan("Confirm alternate supplier and resequence non-critical tasks.")
                .owner("Site Manager")
                .targetDate(LocalDate.now().plusDays(7))
                .createdBy(createdBy)
                .build();
    }

    private Task task(Project project) {
        return Task.builder()
                .id(200L)
                .project(project)
                .title("Inspect foundation works")
                .description("Confirm foundation preparation is complete.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assignee("Site Engineer")
                .dueDate(LocalDate.now().plusDays(5))
                .build();
    }
}
