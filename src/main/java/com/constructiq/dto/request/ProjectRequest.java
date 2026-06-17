package com.constructiq.dto.request;

import com.constructiq.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 28/5/2026 3:33 pm
 */
@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private String location;

    private String clientName;

    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    @Valid
    private List<UserProjectRegistrationRequest> members;
}
