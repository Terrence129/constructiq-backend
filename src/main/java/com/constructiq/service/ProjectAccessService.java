package com.constructiq.service;

import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final UserProjectRegistrationRepository registrationRepository;

    public void checkProjectAccess(Project project, User user) {
        if (!hasProjectAccess(project, user)) {
            throw new AccessDeniedException("You do not have permission to access this project");
        }
    }

    public boolean hasProjectAccess(Project project, User user) {
        if (project == null || user == null || project.getId() == null || user.getId() == null) {
            return false;
        }

        if (project.getCreatedBy() != null && user.getId().equals(project.getCreatedBy().getId())) {
            return true;
        }

        return registrationRepository.existsByUserIdAndProjectId(user.getId(), project.getId());
    }

    public Project getProjectWithAccessCheck(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        checkProjectAccess(project, user);

        return project;
    }

    public void checkProjectCreator(Project project, User user) {
        if (project == null || user == null || project.getCreatedBy() == null
                || !project.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the project creator can manage registrations");
        }
    }
}
