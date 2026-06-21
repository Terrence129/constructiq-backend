package com.constructiq.service;

import com.constructiq.dto.request.RiskRequest;
import com.constructiq.dto.response.RiskResponse;
import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.RiskCategory;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.RiskRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserProjectRegistrationRepository registrationRepository;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private Utils utils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RiskService riskService;

    @Test
    void createRiskCalculatesSeverityAndRiskLevel() {
        User creator = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(creator).build();
        RiskRequest request = riskRequest();
        request.setProbability(4);
        request.setImpact(5);

        when(utils.getCurrentUser(authentication)).thenReturn(creator);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(riskRepository.save(any(Risk.class))).thenAnswer(invocation -> {
            Risk risk = invocation.getArgument(0);
            risk.setId(100L);
            return risk;
        });

        RiskResponse response = riskService.createRisk(10L, request, authentication);

        ArgumentCaptor<Risk> captor = ArgumentCaptor.forClass(Risk.class);
        verify(riskRepository).save(captor.capture());

        assertThat(captor.getValue().getSeverity()).isEqualTo(20);
        assertThat(captor.getValue().getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(response.getSeverity()).isEqualTo(20);
        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
    }

    @Test
    void createRiskRejectsUserWithoutManagementAccess() {
        User member = User.builder().id(2L).build();
        User creator = User.builder().id(1L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();
        RiskRequest request = riskRequest();

        when(utils.getCurrentUser(authentication)).thenReturn(member);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You do not have permission to manage this project"))
                .when(projectAccessService)
                .checkProjectManagementAccess(project, member);

        assertThatThrownBy(() -> riskService.createRisk(10L, request, authentication))
                .isInstanceOf(AccessDeniedException.class);

        verify(riskRepository, never()).save(any(Risk.class));
    }

    @Test
    void getRisksByProjectChecksProjectAccess() {
        User member = User.builder().id(2L).build();
        User creator = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(creator).build();
        Risk risk = risk(project, creator);

        when(utils.getCurrentUser(authentication)).thenReturn(member);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(riskRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of(risk));

        List<RiskResponse> responses = riskService.getRisksByProject(10L, authentication);

        verify(projectAccessService).checkProjectAccess(project, member);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void getMyRisksDefaultsToAllAccessibleRisks() {
        User user = User.builder().id(1L).name("Current User").build();
        User creator = User.builder().id(2L).name("Admin User").build();
        Project createdProject = Project.builder().id(10L).name("Created Project").createdBy(user).build();
        Project registeredProject = Project.builder().id(20L).name("Registered Project").createdBy(creator).build();
        Risk risk = risk(createdProject, user);
        List<Project> accessibleProjects = List.of(createdProject, registeredProject);

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(createdProject));
        when(registrationRepository.findByUser(user))
                .thenReturn(List.of(UserProjectRegistration.builder().project(registeredProject).build()));
        when(riskRepository.findAccessibleRisks(accessibleProjects, null, null, null)).thenReturn(List.of(risk));

        List<RiskResponse> responses = riskService.getMyRisks(null, null, null, authentication);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(100L);
        assertThat(responses.get(0).getProjectId()).isEqualTo(10L);
        assertThat(responses.get(0).getCategory()).isEqualTo(RiskCategory.SCHEDULE);
        assertThat(responses.get(0).getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(responses.get(0).getStatus()).isEqualTo(RiskStatus.OPEN);
        verify(riskRepository).findAccessibleRisks(accessibleProjects, null, null, null);
    }

    @Test
    void getMyRisksFiltersByCategoryRiskLevelAndStatus() {
        User user = User.builder().id(1L).name("Current User").build();
        Project project = Project.builder().id(10L).name("Filtered Project").createdBy(user).build();
        Risk risk = risk(project, user);

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(project));
        when(registrationRepository.findByUser(user)).thenReturn(List.of());
        when(riskRepository.findAccessibleRisks(
                List.of(project),
                RiskCategory.SCHEDULE,
                RiskLevel.CRITICAL,
                RiskStatus.OPEN
        )).thenReturn(List.of(risk));

        List<RiskResponse> responses = riskService.getMyRisks(
                RiskCategory.SCHEDULE,
                RiskLevel.CRITICAL,
                RiskStatus.OPEN,
                authentication
        );

        assertThat(responses).extracting(RiskResponse::getId).containsExactly(100L);
        verify(riskRepository).findAccessibleRisks(
                List.of(project),
                RiskCategory.SCHEDULE,
                RiskLevel.CRITICAL,
                RiskStatus.OPEN
        );
    }

    @Test
    void getMyRisksReturnsEmptyWhenUserHasNoAccessibleProjects() {
        User user = User.builder().id(1L).name("Current User").build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(registrationRepository.findByUser(user)).thenReturn(List.of());

        List<RiskResponse> responses = riskService.getMyRisks(null, null, null, authentication);

        assertThat(responses).isEmpty();
        verifyNoInteractions(riskRepository);
    }

    @Test
    void updateRiskRecalculatesSeverityAndRiskLevel() {
        User creator = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(creator).build();
        Risk existingRisk = risk(project, creator);
        RiskRequest request = riskRequest();
        request.setProbability(3);
        request.setImpact(4);

        when(utils.getCurrentUser(authentication)).thenReturn(creator);
        when(riskRepository.findById(100L)).thenReturn(Optional.of(existingRisk));
        when(riskRepository.save(existingRisk)).thenReturn(existingRisk);

        RiskResponse response = riskService.updateRisk(100L, request, authentication);

        assertThat(response.getSeverity()).isEqualTo(12);
        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
    }

    private RiskRequest riskRequest() {
        RiskRequest request = new RiskRequest();
        request.setTitle("Steel delivery delay");
        request.setDescription("Structural steel delivery may delay critical path work.");
        request.setCategory(RiskCategory.SCHEDULE);
        request.setProbability(4);
        request.setImpact(4);
        request.setStatus(RiskStatus.OPEN);
        request.setMitigationPlan("Confirm alternate supplier and resequence non-critical tasks.");
        request.setOwner("Site Manager");
        return request;
    }

    private Risk risk(Project project, User createdBy) {
        return Risk.builder()
                .id(100L)
                .project(project)
                .title("Steel delivery delay")
                .description("Structural steel delivery may delay critical path work.")
                .category(RiskCategory.SCHEDULE)
                .probability(4)
                .impact(4)
                .severity(16)
                .riskLevel(RiskLevel.CRITICAL)
                .status(RiskStatus.OPEN)
                .mitigationPlan("Confirm alternate supplier and resequence non-critical tasks.")
                .owner("Site Manager")
                .createdBy(createdBy)
                .build();
    }
}
