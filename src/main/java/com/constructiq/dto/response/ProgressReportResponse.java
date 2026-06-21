package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:08 pm
 */
@Getter
@Builder
public class ProgressReportResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long projectId;

    private String projectName;

    private LocalDate reportDate;

    private String summary;

    private String completedWork;

    private String delayedWork;

    private String issues;

    private String nextActions;

    private Long createdById;

    private String createdByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
