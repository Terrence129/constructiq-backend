package com.constructiq.dto.response;

import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:10 am
 */
@Getter
@Builder
public class TaskResponse {

    private Long id;

    private Long projectId;
    private String projectName;

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignee;
    private LocalDate dueDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
