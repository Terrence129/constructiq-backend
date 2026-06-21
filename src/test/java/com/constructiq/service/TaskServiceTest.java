package com.constructiq.service;

import com.constructiq.dto.response.TaskResponse;
import com.constructiq.entity.Project;
import com.constructiq.entity.Task;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.TaskRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

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
    private TaskService taskService;

    @Test
    void getMyTasksDefaultsToAllAccessibleTasks() {
        User user = User.builder().id(1L).build();
        Project createdProject = project(10L, "Created Project");
        Project registeredProject = project(20L, "Registered Project");
        Task task = task(100L, createdProject, TaskStatus.TODO, TaskPriority.HIGH);
        List<Project> accessibleProjects = List.of(createdProject, registeredProject);

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(createdProject));
        when(registrationRepository.findByUser(user))
                .thenReturn(List.of(UserProjectRegistration.builder().project(registeredProject).build()));
        when(taskRepository.findAccessibleTasks(accessibleProjects, null, null)).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.getMyTasks(null, null, authentication);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(100L);
        assertThat(response.get(0).getProjectId()).isEqualTo(10L);
        assertThat(response.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(response.get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
        verify(taskRepository).findAccessibleTasks(accessibleProjects, null, null);
    }

    @Test
    void getMyTasksFiltersByStatusAndPriority() {
        User user = User.builder().id(1L).build();
        Project project = project(10L, "Filtered Project");
        Task task = task(101L, project, TaskStatus.BLOCKED, TaskPriority.CRITICAL);

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of(project));
        when(registrationRepository.findByUser(user)).thenReturn(List.of());
        when(taskRepository.findAccessibleTasks(
                List.of(project),
                TaskStatus.BLOCKED,
                TaskPriority.CRITICAL
        )).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.getMyTasks(
                TaskStatus.BLOCKED,
                TaskPriority.CRITICAL,
                authentication
        );

        assertThat(response).extracting(TaskResponse::getId).containsExactly(101L);
        verify(taskRepository).findAccessibleTasks(
                List.of(project),
                TaskStatus.BLOCKED,
                TaskPriority.CRITICAL
        );
    }

    @Test
    void getMyTasksReturnsEmptyWhenUserHasNoAccessibleProjects() {
        User user = User.builder().id(1L).build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);
        when(projectRepository.findByCreatedByOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(registrationRepository.findByUser(user)).thenReturn(List.of());

        List<TaskResponse> response = taskService.getMyTasks(null, null, authentication);

        assertThat(response).isEmpty();
        verifyNoInteractions(taskRepository);
    }

    private Project project(Long id, String name) {
        return Project.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Task task(Long id, Project project, TaskStatus status, TaskPriority priority) {
        return Task.builder()
                .id(id)
                .project(project)
                .title("Task " + id)
                .status(status)
                .priority(priority)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
