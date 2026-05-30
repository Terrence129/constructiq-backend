package com.constructiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:08 pm
 */
@Getter
@Setter
public class ProgressReportRequest {

    @NotNull
    private LocalDate reportDate;

    @NotBlank
    private String summary;

    private String completedWork;

    private String delayedWork;

    private String issues;

    private String nextActions;
}
