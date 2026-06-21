package com.constructiq.controller;

import com.constructiq.dto.request.TaskRequest;
import com.constructiq.dto.response.TaskResponse;
import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import com.constructiq.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:11 am
 */
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/projects/{projectId}/tasks")
    public TaskResponse createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        return taskService.createTask(projectId, request, authentication);
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskResponse> getTasksByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return taskService.getTasksByProject(projectId, authentication);
    }

    @GetMapping("/api/tasks/{taskId}")
    public TaskResponse getTaskById(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return taskService.getTaskById(taskId, authentication);
    }

    @PutMapping("/api/tasks/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        return taskService.updateTask(taskId, request, authentication);
    }

    @DeleteMapping("/api/tasks/{taskId}")
    public void deleteTask(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        taskService.deleteTask(taskId, authentication);
    }

    @GetMapping("/api/tasks")
    public List<TaskResponse> getMyTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Authentication authentication
    ) {
        return taskService.getMyTasks(status, priority, authentication);
    }
}
