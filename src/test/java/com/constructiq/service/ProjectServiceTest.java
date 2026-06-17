package com.constructiq.service;

import com.constructiq.dto.request.ProjectRequest;
import com.constructiq.dto.request.UserProjectRegistrationRequest;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.ProjectMemberRole;
import com.constructiq.enums.UserRole;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.repository.UserRepository;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserProjectRegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private Utils utils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProjectRejectsNonAdmin() {
        User user = User.builder().id(1L).role(UserRole.USER).build();

        when(utils.getCurrentUser(authentication)).thenReturn(user);

        assertThatThrownBy(() -> projectService.createProject(projectRequest(), authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only admins can create projects");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void createProjectCreatesInitialMembers() {
        User admin = User.builder().id(1L).role(UserRole.ADMIN).build();
        User member = User.builder().id(2L).build();
        User manager = User.builder().id(3L).build();
        ProjectRequest request = projectRequest();
        request.setMembers(List.of(
                memberRequest(2L, null),
                memberRequest(3L, ProjectMemberRole.MANAGER)
        ));

        when(utils.getCurrentUser(authentication)).thenReturn(admin);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(10L);
            return project;
        });
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));

        projectService.createProject(request, authentication);

        ArgumentCaptor<UserProjectRegistration> captor =
                ArgumentCaptor.forClass(UserProjectRegistration.class);
        verify(registrationRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(UserProjectRegistration::getRole)
                .containsExactly(ProjectMemberRole.MEMBER, ProjectMemberRole.MANAGER);
    }

    private ProjectRequest projectRequest() {
        ProjectRequest request = new ProjectRequest();
        request.setName("New Project");
        return request;
    }

    private UserProjectRegistrationRequest memberRequest(Long userId, ProjectMemberRole role) {
        UserProjectRegistrationRequest request = new UserProjectRegistrationRequest();
        request.setUserId(userId);
        request.setRole(role);
        return request;
    }
}
