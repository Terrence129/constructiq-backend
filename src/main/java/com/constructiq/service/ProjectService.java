package com.constructiq.service;

import com.constructiq.dto.request.ProjectRequest;
import com.constructiq.dto.request.UserProjectRegistrationRequest;
import com.constructiq.dto.response.ProjectResponse;
import com.constructiq.config.CacheConfig;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.ProjectMemberRole;
import com.constructiq.enums.ProjectStatus;
import com.constructiq.enums.UserRole;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.repository.UserRepository;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final UserProjectRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;
    private final Utils utils;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROJECTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.REGISTRATIONS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public ProjectResponse createProject(ProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only admins can create projects");
        }

        List<UserProjectRegistrationRequest> members = request.getMembers() == null
                ? List.of()
                : request.getMembers();
        validateInitialMembers(members, currentUser);

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

        members.stream()
                .map(member -> buildRegistration(savedProject, member))
                .forEach(registrationRepository::save);

        return toResponse(savedProject);
    }

    @Cacheable(cacheNames = CacheConfig.PROJECTS, key = "#authentication.name + ':mine'")
    public List<ProjectResponse> getMyProjects(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Map<Long, Project> accessibleProjects = new LinkedHashMap<>();

        projectRepository.findByCreatedByOrderByCreatedAtDesc(currentUser)
                .forEach(project -> accessibleProjects.put(project.getId(), project));

        registrationRepository.findByUser(currentUser)
                .forEach(registration -> accessibleProjects.putIfAbsent(
                        registration.getProject().getId(),
                        registration.getProject()
                ));

        return accessibleProjects.values().stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = CacheConfig.PROJECTS, key = "#authentication.name + ':' + #id")
    public ProjectResponse getProjectById(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectAccessService.checkProjectAccess(project, currentUser);

        return toResponse(project);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROJECTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TASKS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RISKS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PROGRESS_REPORTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DOCUMENTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.REGISTRATIONS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public ProjectResponse updateProject(Long id, ProjectRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectAccessService.checkProjectManagementAccess(project, currentUser);
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

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROJECTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TASKS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RISKS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PROGRESS_REPORTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DOCUMENTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.REGISTRATIONS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public void deleteProject(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        projectRepository.delete(project);
    }

    private User getCurrentUser(Authentication authentication) {
        return utils.getCurrentUser(authentication);
    }

    private void validateInitialMembers(List<UserProjectRegistrationRequest> members, User currentUser) {
        Set<Long> userIds = new HashSet<>();

        for (UserProjectRegistrationRequest member : members) {
            if (currentUser.getId().equals(member.getUserId())) {
                throw new IllegalArgumentException("Project creator does not need a project registration");
            }

            if (!userIds.add(member.getUserId())) {
                throw new IllegalArgumentException("Project member list contains duplicate users");
            }
        }
    }

    private UserProjectRegistration buildRegistration(Project project, UserProjectRegistrationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserProjectRegistration.builder()
                .user(user)
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .role(request.getRole() == null ? ProjectMemberRole.MEMBER : request.getRole())
                .build();
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
