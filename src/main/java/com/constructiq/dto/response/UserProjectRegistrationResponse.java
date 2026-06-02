package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProjectRegistrationResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
