package com.constructiq.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_statistics_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_projects", nullable = false)
    private Long totalProjects;

    @Column(name = "active_projects", nullable = false)
    private Long activeProjects;

    @Column(name = "completed_projects", nullable = false)
    private Long completedProjects;

    @Column(name = "total_tasks", nullable = false)
    private Long totalTasks;

    @Column(name = "open_tasks", nullable = false)
    private Long openTasks;

    @Column(name = "completed_tasks", nullable = false)
    private Long completedTasks;

    @Column(name = "overdue_tasks", nullable = false)
    private Long overdueTasks;

    @Column(name = "total_risks", nullable = false)
    private Long totalRisks;

    @Column(name = "open_risks", nullable = false)
    private Long openRisks;

    @Column(name = "high_risks", nullable = false)
    private Long highRisks;

    @Column(name = "critical_risks", nullable = false)
    private Long criticalRisks;

    @Column(name = "progress_reports", nullable = false)
    private Long progressReports;

    @Column(name = "documents", nullable = false)
    private Long documents;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    public void onCreate() {
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }
    }
}
