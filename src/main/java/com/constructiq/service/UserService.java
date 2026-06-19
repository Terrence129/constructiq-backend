package com.constructiq.service;

import com.constructiq.dto.projection.UserSummaryProjection;
import com.constructiq.dto.response.UserResponse;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(String name, String email) {
        String normalizedName = normalize(name);
        String normalizedEmail = normalize(email);

        return userRepository.findUserSummaries(normalizedName, normalizedEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userRepository.findUserSummaryById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    private UserResponse toResponse(UserSummaryProjection user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
