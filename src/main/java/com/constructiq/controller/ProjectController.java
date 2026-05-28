package com.constructiq.controller;

import com.constructiq.dto.request.ProjectRequest;
import com.constructiq.dto.response.ProjectResponse;
import com.constructiq.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 28/5/2026 3:33 pm
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectResponse createProject(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        return projectService.createProject(request, authentication);
    }

    @GetMapping
    public List<ProjectResponse> getMyProjects(Authentication authentication) {
        return projectService.getMyProjects(authentication);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return projectService.getProjectById(id, authentication);
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        return projectService.updateProject(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        projectService.deleteProject(id, authentication);
    }
}
