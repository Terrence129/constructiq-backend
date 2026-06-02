package com.constructiq.service;

import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAccessServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserProjectRegistrationRepository registrationRepository;

    @InjectMocks
    private ProjectAccessService projectAccessService;

    @Test
    void hasProjectAccessAllowsProjectCreator() {
        User creator = User.builder().id(1L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();

        assertThat(projectAccessService.hasProjectAccess(project, creator)).isTrue();
    }

    @Test
    void hasProjectAccessAllowsRegisteredUser() {
        User creator = User.builder().id(1L).build();
        User registeredUser = User.builder().id(2L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();

        when(registrationRepository.existsByUserIdAndProjectId(2L, 10L)).thenReturn(true);

        assertThat(projectAccessService.hasProjectAccess(project, registeredUser)).isTrue();
    }

    @Test
    void checkProjectAccessRejectsUnregisteredUser() {
        User creator = User.builder().id(1L).build();
        User unregisteredUser = User.builder().id(2L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();

        when(registrationRepository.existsByUserIdAndProjectId(2L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> projectAccessService.checkProjectAccess(project, unregisteredUser))
                .isInstanceOf(AccessDeniedException.class);
    }
}
