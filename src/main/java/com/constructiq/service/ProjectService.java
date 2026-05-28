package com.constructiq.service;

import com.constructiq.dto.request.ProjectRequest;
import com.constructiq.dto.response.ProjectResponse;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.enums.ProjectStatus;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 28/5/2026 3:33 pm
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(ProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .clientName(request.getClientName())
                .status(request.getStatus() == null ? ProjectStatus.PLANNING : request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(currentUser)
                .build();

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    public List<ProjectResponse> getMyProjects(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return projectRepository.findByCreatedByOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        checkOwnership(project, currentUser);

        return toResponse(project);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        checkOwnership(project, currentUser);
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        project.setDescription(request.getDescription());
        project.setLocation(request.getLocation());
        project.setClientName(request.getClientName());

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project updatedProject = projectRepository.save(project);

        return toResponse(updatedProject);
    }

    public void deleteProject(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        checkOwnership(project, currentUser);

        projectRepository.delete(project);
    }

    private User getCurrentUser(Authentication authentication) {
//        String email = authentication.getName();
        String email = "peacefulterrence@gmail.com";
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private void checkOwnership(Project project, User currentUser) {
        if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You do not have permission to access this project");
        }
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .location(project.getLocation())
                .clientName(project.getClientName())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdById(project.getCreatedBy().getId())
                .createdByName(project.getCreatedBy().getName())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}