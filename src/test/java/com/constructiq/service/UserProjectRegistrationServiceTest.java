package com.constructiq.service;

import com.constructiq.dto.request.UserProjectRegistrationRequest;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.ProjectMemberRole;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import com.constructiq.repository.UserRepository;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProjectRegistrationServiceTest {

    @Mock
    private UserProjectRegistrationRepository registrationRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private Utils utils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserProjectRegistrationService registrationService;

    @Test
    void createRegistrationRejectsNonCreator() {
        User creator = User.builder().id(1L).build();
        User registeredUser = User.builder().id(2L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();
        UserProjectRegistrationRequest request = requestForUser(3L);

        when(utils.getCurrentUser(authentication)).thenReturn(registeredUser);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        org.mockito.Mockito.doThrow(new AccessDeniedException("You do not have permission to manage this project"))
                .when(projectAccessService)
                .checkProjectManagementAccess(project, registeredUser);

        assertThatThrownBy(() -> registrationService.createRegistration(10L, request, authentication))
                .isInstanceOf(AccessDeniedException.class);

        verify(registrationRepository, never()).save(any(UserProjectRegistration.class));
    }

    @Test
    void createRegistrationRejectsDuplicateRegistration() {
        User creator = User.builder().id(1L).build();
        User targetUser = User.builder().id(2L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();
        UserProjectRegistrationRequest request = requestForUser(2L);

        when(utils.getCurrentUser(authentication)).thenReturn(creator);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(registrationRepository.existsByUserAndProject(targetUser, project)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.createRegistration(10L, request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is already registered to this project");

        verify(registrationRepository, never()).save(any(UserProjectRegistration.class));
    }

    @Test
    void createRegistrationDefaultsRoleToMember() {
        User creator = User.builder().id(1L).build();
        User targetUser = User.builder().id(2L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();
        UserProjectRegistrationRequest request = requestForUser(2L);

        when(utils.getCurrentUser(authentication)).thenReturn(creator);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(registrationRepository.existsByUserAndProject(targetUser, project)).thenReturn(false);
        when(registrationRepository.save(any(UserProjectRegistration.class)))
                .thenAnswer(invocation -> {
                    UserProjectRegistration registration = invocation.getArgument(0);
                    registration.setId(100L);
                    return registration;
                });

        registrationService.createRegistration(10L, request, authentication);

        org.mockito.ArgumentCaptor<UserProjectRegistration> captor =
                org.mockito.ArgumentCaptor.forClass(UserProjectRegistration.class);
        verify(registrationRepository).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getRole())
                .isEqualTo(ProjectMemberRole.MEMBER);
    }

    private UserProjectRegistrationRequest requestForUser(Long userId) {
        UserProjectRegistrationRequest request = new UserProjectRegistrationRequest();
        request.setUserId(userId);
        request.setTitle("Site Engineer");
        request.setDescription("Responsible for weekly progress reporting and task updates.");
        return request;
    }
}
