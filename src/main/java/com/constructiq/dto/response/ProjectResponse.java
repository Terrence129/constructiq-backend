package com.constructiq.dto.response;

import com.constructiq.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 28/5/2026 3:33 pm
 */
@Getter
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private String location;
    private String clientName;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
