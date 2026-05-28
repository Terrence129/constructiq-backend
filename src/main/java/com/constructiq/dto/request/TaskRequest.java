package com.constructiq.dto.request;

import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:09 am
 */
@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private String assignee;

    private LocalDate dueDate;
}
