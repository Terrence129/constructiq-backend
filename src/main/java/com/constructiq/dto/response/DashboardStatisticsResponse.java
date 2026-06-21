package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class DashboardStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long snapshotId;
    private Long userId;
    private String userName;
    private Long totalProjects;
    private Long activeProjects;
    private Long completedProjects;
    private Long totalTasks;
    private Long openTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Long totalRisks;
    private Long openRisks;
    private Long highRisks;
    private Long criticalRisks;
    private Long progressReports;
    private Long documents;
    private LocalDateTime generatedAt;
}
