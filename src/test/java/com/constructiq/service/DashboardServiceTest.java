package com.constructiq.service;

import com.constructiq.dto.response.DashboardStatisticsResponse;
import com.constructiq.entity.DashboardStatisticsSnapshot;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.ProjectStatus;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import com.constructiq.enums.TaskStatus;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.*;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

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
    private DashboardStatisticsSnapshotRepository snapshotRepository;

    @Mock
    private Utils utils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getStatisticsAggregatesAccessibleProjects() {
        User user = User.builder().id(1L).name("Dashboard User").build();
        Project activeProject = Project.builder().id(10L).status(ProjectStatus.ACTIVE).build();
        Project completedProject = Project.builder().id(20L).status(ProjectStatus.COMPLETED).build();
        Project registeredProject = Project.builder().id(30L).status(ProjectStatus.PLANNING).build();
        UserProjectRegistration registration = UserProjectRegistration.builder()
                .user(user)
                .project(registeredProject)
                .build();
        List<Project> expectedProjects = List.of(activeProject, completedProject, registeredProject);

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user))
                .thenReturn(List.of(activeProject, completedProject));
        when(registrationRepository.findByUser(user)).thenReturn(List.of(registration));
        when(taskRepository.countByProjectIn(expectedProjects)).thenReturn(8L);
        when(taskRepository.countByProjectInAndStatusNot(expectedProjects, TaskStatus.DONE)).thenReturn(5L);
        when(taskRepository.countByProjectInAndStatus(expectedProjects, TaskStatus.DONE)).thenReturn(3L);
        when(taskRepository.countByProjectInAndDueDateBeforeAndStatusNot(
                eq(expectedProjects),
                any(LocalDate.class),
                eq(TaskStatus.DONE)
        )).thenReturn(2L);
        when(riskRepository.countByProjectIn(expectedProjects)).thenReturn(6L);
        when(riskRepository.countByProjectInAndStatusNot(expectedProjects, RiskStatus.CLOSED)).thenReturn(4L);
        when(riskRepository.countByProjectInAndRiskLevel(expectedProjects, RiskLevel.HIGH)).thenReturn(2L);
        when(riskRepository.countByProjectInAndRiskLevel(expectedProjects, RiskLevel.CRITICAL)).thenReturn(1L);
        when(progressReportRepository.countByProjectIn(expectedProjects)).thenReturn(7L);
        when(documentRepository.countByProjectIn(expectedProjects)).thenReturn(9L);

        DashboardStatisticsResponse response = dashboardService.getStatistics(authentication);

        assertThat(response.getTotalProjects()).isEqualTo(3);
        assertThat(response.getActiveProjects()).isEqualTo(1);
        assertThat(response.getCompletedProjects()).isEqualTo(1);
        assertThat(response.getTotalTasks()).isEqualTo(8);
        assertThat(response.getOpenTasks()).isEqualTo(5);
        assertThat(response.getCompletedTasks()).isEqualTo(3);
        assertThat(response.getOverdueTasks()).isEqualTo(2);
        assertThat(response.getTotalRisks()).isEqualTo(6);
        assertThat(response.getOpenRisks()).isEqualTo(4);
        assertThat(response.getHighRisks()).isEqualTo(2);
        assertThat(response.getCriticalRisks()).isEqualTo(1);
        assertThat(response.getProgressReports()).isEqualTo(7);
        assertThat(response.getDocuments()).isEqualTo(9);
        assertThat(response.getSnapshotId()).isNull();
    }

    @Test
    void getStatisticsReturnsZeroesWhenUserHasNoProjects() {
        User user = User.builder().id(1L).name("Dashboard User").build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(registrationRepository.findByUser(user)).thenReturn(List.of());

        DashboardStatisticsResponse response = dashboardService.getStatistics(authentication);

        assertThat(response.getTotalProjects()).isZero();
        assertThat(response.getTotalTasks()).isZero();
        assertThat(response.getTotalRisks()).isZero();
        assertThat(response.getProgressReports()).isZero();
        assertThat(response.getDocuments()).isZero();
        verifyNoInteractions(taskRepository, riskRepository, progressReportRepository, documentRepository);
    }

    @Test
    void createSnapshotPersistsCurrentStatistics() {
        User user = User.builder().id(1L).name("Dashboard User").build();
        Project activeProject = Project.builder().id(10L).status(ProjectStatus.ACTIVE).build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(activeProject));
        when(registrationRepository.findByUser(user)).thenReturn(List.of());
        when(taskRepository.countByProjectIn(List.of(activeProject))).thenReturn(1L);
        when(taskRepository.countByProjectInAndStatusNot(List.of(activeProject), TaskStatus.DONE)).thenReturn(1L);
        when(taskRepository.countByProjectInAndStatus(List.of(activeProject), TaskStatus.DONE)).thenReturn(0L);
        when(taskRepository.countByProjectInAndDueDateBeforeAndStatusNot(
                eq(List.of(activeProject)),
                any(LocalDate.class),
                eq(TaskStatus.DONE)
        )).thenReturn(0L);
        when(snapshotRepository.save(any(DashboardStatisticsSnapshot.class))).thenAnswer(invocation -> {
            DashboardStatisticsSnapshot snapshot = invocation.getArgument(0);
            snapshot.setId(100L);
            return snapshot;
        });

        DashboardStatisticsResponse response = dashboardService.createSnapshot(authentication);

        ArgumentCaptor<DashboardStatisticsSnapshot> captor =
                ArgumentCaptor.forClass(DashboardStatisticsSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        assertThat(captor.getValue().getTotalProjects()).isEqualTo(1);
        assertThat(captor.getValue().getActiveProjects()).isEqualTo(1);
        assertThat(response.getSnapshotId()).isEqualTo(100L);
    }

    @Test
    void getLatestSnapshotReturnsSavedSnapshot() {
        User user = User.builder().id(1L).name("Dashboard User").build();
        DashboardStatisticsSnapshot snapshot = DashboardStatisticsSnapshot.builder()
                .id(100L)
                .user(user)
                .totalProjects(3L)
                .activeProjects(1L)
                .completedProjects(1L)
                .totalTasks(8L)
                .openTasks(5L)
                .completedTasks(3L)
                .overdueTasks(2L)
                .totalRisks(6L)
                .openRisks(4L)
                .highRisks(2L)
                .criticalRisks(1L)
                .progressReports(7L)
                .documents(9L)
                .generatedAt(LocalDateTime.now())
                .build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(snapshotRepository.findFirstByUserOrderByGeneratedAtDesc(user)).thenReturn(Optional.of(snapshot));

        DashboardStatisticsResponse response = dashboardService.getLatestSnapshot(authentication);

        assertThat(response.getSnapshotId()).isEqualTo(100L);
        assertThat(response.getTotalProjects()).isEqualTo(3);
        assertThat(response.getHighRisks()).isEqualTo(2);
    }

    @Test
    void getLatestSnapshotThrowsWhenMissing() {
        User user = User.builder().id(1L).name("Dashboard User").build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(snapshotRepository.findFirstByUserOrderByGeneratedAtDesc(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getLatestSnapshot(authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dashboard statistics snapshot not found");
    }
}
