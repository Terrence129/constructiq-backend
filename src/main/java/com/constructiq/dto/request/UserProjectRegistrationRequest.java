package com.constructiq.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProjectRegistrationRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    private String title;

    private String description;
}
