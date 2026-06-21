package com.constructiq.service;

import com.constructiq.dto.request.UserProjectRegistrationRequest;
import com.constructiq.dto.response.UserProjectRegistrationResponse;
import com.constructiq.config.CacheConfig;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.ProjectMemberRole;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.repository.UserRepository;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProjectRegistrationService {

    private final UserProjectRegistrationRepository registrationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;
    private final Utils utils;

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROJECTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TASKS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RISKS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PROGRESS_REPORTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DOCUMENTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.REGISTRATIONS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public UserProjectRegistrationResponse createRegistration(
            Long projectId,
            UserProjectRegistrationRequest request,
            Authentication authentication
    ) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (project.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Project creator does not need a project registration");
        }

        if (registrationRepository.existsByUserAndProject(user, project)) {
            throw new IllegalArgumentException("User is already registered to this project");
        }

        UserProjectRegistration registration = UserProjectRegistration.builder()
                .user(user)
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .role(request.getRole() == null ? ProjectMemberRole.MEMBER : request.getRole())
                .build();

        return toResponse(registrationRepository.save(registration));
    }

    @Cacheable(cacheNames = CacheConfig.REGISTRATIONS, key = "#authentication.name + ':project:' + #projectId")
    public List<UserProjectRegistrationResponse> getRegistrations(Long projectId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        return registrationRepository.findByProject(project)
                .stream()
                .map(this::toResponse)
                .toList();
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
    public void deleteRegistration(Long projectId, Long registrationId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        UserProjectRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (!registration.getProject().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Registration not found");
        }

        registrationRepository.delete(registration);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private UserProjectRegistrationResponse toResponse(UserProjectRegistration registration) {
        return UserProjectRegistrationResponse.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .userName(registration.getUser().getName())
                .userEmail(registration.getUser().getEmail())
                .projectId(registration.getProject().getId())
                .projectName(registration.getProject().getName())
                .title(registration.getTitle())
                .description(registration.getDescription())
                .role(registration.getRole())
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }
}
