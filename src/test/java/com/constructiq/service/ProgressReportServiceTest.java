package com.constructiq.service;

import com.constructiq.dto.response.ProgressReportResponse;
import com.constructiq.entity.ProgressReport;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.repository.ProgressReportRepository;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProgressReportRepository progressReportRepository;

    @Mock
    private UserProjectRegistrationRepository registrationRepository;

    @Mock
    private Utils utils;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProgressReportService progressReportService;

    @Test
    void getMyReportsReturnsReportsForAccessibleProjects() {
        User user = User.builder().id(1L).name("Current User").build();
        User creator = User.builder().id(2L).name("Admin User").build();
        Project createdProject = project(10L, "Created Project", user);
        Project registeredProject = project(20L, "Registered Project", creator);
        ProgressReport report = report(100L, createdProject, user);
        List<Project> accessibleProjects = List.of(createdProject, registeredProject);

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(createdProject));
        when(registrationRepository.findByUser(user))
                .thenReturn(List.of(UserProjectRegistration.builder().project(registeredProject).build()));
        when(progressReportRepository.findAccessibleProgressReports(accessibleProjects)).thenReturn(List.of(report));

        List<ProgressReportResponse> responses = progressReportService.getMyReports(authentication);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(100L);
        assertThat(responses.get(0).getProjectId()).isEqualTo(10L);
        assertThat(responses.get(0).getCreatedById()).isEqualTo(1L);
        verify(progressReportRepository).findAccessibleProgressReports(accessibleProjects);
    }

    @Test
    void getMyReportsReturnsEmptyWhenUserHasNoAccessibleProjects() {
        User user = User.builder().id(1L).name("Current User").build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(registrationRepository.findByUser(user)).thenReturn(List.of());

        List<ProgressReportResponse> responses = progressReportService.getMyReports(authentication);

        assertThat(responses).isEmpty();
        verifyNoInteractions(progressReportRepository);
    }

    private Project project(Long id, String name, User createdBy) {
        return Project.builder()
                .id(id)
                .name(name)
                .createdBy(createdBy)
                .build();
    }

    private ProgressReport report(Long id, Project project, User createdBy) {
        return ProgressReport.builder()
                .id(id)
                .project(project)
                .reportDate(LocalDate.of(2026, 7, 8))
                .summary("Weekly progress update.")
                .completedWork("Foundation inspection completed.")
                .delayedWork("None.")
                .issues("None.")
                .nextActions("Prepare next inspection checklist.")
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
