package com.constructiq.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:07 pm
 */
@Entity
@Table(name = "progress_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private Project project;

    @Column(
            name = "report_date",
            nullable = false
    )
    private LocalDate reportDate;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String summary;

    @Column(name = "completed_work",columnDefinition = "TEXT")
    private String completedWork;

    @Column(name = "delayed_work",columnDefinition = "TEXT")
    private String delayedWork;

    @Column(columnDefinition = "TEXT")
    private String issues;

    @Column(name = "next_actions",columnDefinition = "TEXT")
    private String nextActions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            nullable = false
    )
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
