package com.constructiq.service;

import com.constructiq.dto.projection.UserSummaryProjection;
import com.constructiq.dto.response.UserResponse;
import com.constructiq.enums.UserRole;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUsersListsAllPublicUserFields() {
        UserSummaryProjection user = user(1L, "Jane Builder", "jane.builder@example.com", UserRole.USER);

        when(userRepository.findUserSummaries(null, null)).thenReturn(List.of(user));

        List<UserResponse> users = userService.getUsers(null, null);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getName()).isEqualTo("Jane Builder");
        assertThat(users.get(0).getEmail()).isEqualTo("jane.builder@example.com");
        assertThat(users.get(0).getRole()).isEqualTo(UserRole.USER);
        verify(userRepository).findUserSummaries(null, null);
    }

    @Test
    void getUsersSearchesWhenNameOrEmailIsProvided() {
        UserSummaryProjection user = user(2L, "Admin User", "admin@example.com", UserRole.ADMIN);

        when(userRepository.findUserSummaries("admin", "example")).thenReturn(List.of(user));

        List<UserResponse> users = userService.getUsers(" admin ", " EXAMPLE ");

        assertThat(users).extracting(UserResponse::getId).containsExactly(2L);
        verify(userRepository).findUserSummaries("admin", "example");
    }

    @Test
    void getUserByIdReturnsUser() {
        UserSummaryProjection user = user(3L, "Site Engineer", "site.engineer@example.com", UserRole.USER);

        when(userRepository.findUserSummaryById(3L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(3L);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getName()).isEqualTo("Site Engineer");
        assertThat(response.getEmail()).isEqualTo("site.engineer@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void getUserByIdRejectsMissingUser() {
        when(userRepository.findUserSummaryById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    private UserSummaryProjection user(Long id, String name, String email, UserRole role) {
        return new UserSummaryProjection() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public UserRole getRole() {
                return role;
            }
        };
    }
}
