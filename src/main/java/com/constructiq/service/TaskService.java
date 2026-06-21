package com.constructiq.service;

import com.constructiq.dto.request.TaskRequest;
import com.constructiq.dto.response.TaskResponse;
import com.constructiq.entity.Project;
import com.constructiq.entity.Task;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.TaskRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:10 am
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRegistrationRepository registrationRepository;
    private final ProjectAccessService projectAccessService;

    private final Utils utils;

    public TaskResponse createTask(Long projectId, TaskRequest request, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProjectAndCheckManagement(projectId, currentUser);

        Task task = Task.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() == null ? TaskStatus.TODO : request.getStatus())
                .priority(request.getPriority() == null ? TaskPriority.MEDIUM : request.getPriority())
                .assignee(request.getAssignee())
                .dueDate(request.getDueDate())
                .build();

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    public List<TaskResponse> getTasksByProject(Long projectId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProjectAndCheckOwnership(projectId, currentUser);

        return taskRepository.findByProjectOrderByCreatedAtDesc(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TaskResponse> getMyTasks(
            TaskStatus status,
            TaskPriority priority,
            Authentication authentication
    ) {
        User currentUser = utils.getCurrentUser(authentication);
        List<Project> accessibleProjects = getAccessibleProjects(currentUser);

        if (accessibleProjects.isEmpty()) {
            return List.of();
        }

        return taskRepository.findAccessibleTasks(accessibleProjects, status, priority)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long taskId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);

        Task task = getTask(taskId);

        projectAccessService.checkProjectAccess(task.getProject(), currentUser);

        return toResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);

        Task task = getTask(taskId);

        projectAccessService.checkProjectManagementAccess(task.getProject(), currentUser);
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        task.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        task.setAssignee(request.getAssignee());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);

        return toResponse(updatedTask);
    }

    public void deleteTask(Long taskId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);

        Task task = getTask(taskId);

        projectAccessService.checkProjectAccess(task.getProject(), currentUser);

        taskRepository.delete(task);
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private Project getProjectAndCheckOwnership(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectAccessService.checkProjectAccess(project, currentUser);

        return project;
    }

    private Project getProjectAndCheckManagement(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        return project;
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

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(task.getAssignee())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
