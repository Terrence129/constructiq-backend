package com.constructiq.dto.response;

import com.constructiq.enums.ProjectMemberRole;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserProjectRegistrationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private ProjectMemberRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
