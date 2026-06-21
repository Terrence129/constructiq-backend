package com.constructiq.dto.response;

import com.constructiq.enums.RiskCategory;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class RiskResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private RiskCategory category;
    private Integer probability;
    private Integer impact;
    private Integer severity;
    private RiskLevel riskLevel;
    private RiskStatus status;
    private String mitigationPlan;
    private String owner;
    private LocalDate targetDate;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
