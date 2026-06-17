package com.constructiq.dto.request;

import com.constructiq.enums.RiskCategory;
import com.constructiq.enums.RiskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RiskRequest {

    @NotBlank(message = "Risk title is required")
    private String title;

    private String description;

    private RiskCategory category;

    @NotNull(message = "Risk probability is required")
    @Min(value = 1, message = "Risk probability must be between 1 and 5")
    @Max(value = 5, message = "Risk probability must be between 1 and 5")
    private Integer probability;

    @NotNull(message = "Risk impact is required")
    @Min(value = 1, message = "Risk impact must be between 1 and 5")
    @Max(value = 5, message = "Risk impact must be between 1 and 5")
    private Integer impact;

    private RiskStatus status;

    private String mitigationPlan;

    private String owner;

    private LocalDate targetDate;
}
