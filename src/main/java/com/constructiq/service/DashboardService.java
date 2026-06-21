package com.constructiq.service;

import com.constructiq.dto.response.DashboardStatisticsResponse;
import com.constructiq.config.CacheConfig;
import com.constructiq.entity.DashboardStatisticsSnapshot;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.enums.ProjectStatus;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import com.constructiq.enums.TaskStatus;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.*;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final UserProjectRegistrationRepository registrationRepository;
    private final TaskRepository taskRepository;
    private final RiskRepository riskRepository;
    private final ProgressReportRepository progressReportRepository;
    private final DocumentRepository documentRepository;
    private final DashboardStatisticsSnapshotRepository snapshotRepository;
    private final Utils utils;

    @Cacheable(cacheNames = CacheConfig.DASHBOARD_STATISTICS, key = "#authentication.name + ':current'")
    public DashboardStatisticsResponse getStatistics(Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        return buildStatistics(currentUser, null, LocalDateTime.now());
    }

    @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    public DashboardStatisticsResponse createSnapshot(Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        DashboardStatisticsResponse statistics = buildStatistics(currentUser, null, LocalDateTime.now());

        DashboardStatisticsSnapshot snapshot = DashboardStatisticsSnapshot.builder()
                .user(currentUser)
                .totalProjects(statistics.getTotalProjects())
                .activeProjects(statistics.getActiveProjects())
                .completedProjects(statistics.getCompletedProjects())
                .totalTasks(statistics.getTotalTasks())
                .openTasks(statistics.getOpenTasks())
                .completedTasks(statistics.getCompletedTasks())
                .overdueTasks(statistics.getOverdueTasks())
                .totalRisks(statistics.getTotalRisks())
                .openRisks(statistics.getOpenRisks())
                .highRisks(statistics.getHighRisks())
                .criticalRisks(statistics.getCriticalRisks())
                .progressReports(statistics.getProgressReports())
                .documents(statistics.getDocuments())
                .generatedAt(statistics.getGeneratedAt())
                .build();

        return toResponse(snapshotRepository.save(snapshot));
    }

    @Cacheable(cacheNames = CacheConfig.DASHBOARD_STATISTICS, key = "#authentication.name + ':latestSnapshot'")
    public DashboardStatisticsResponse getLatestSnapshot(Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        DashboardStatisticsSnapshot snapshot = snapshotRepository.findFirstByUserOrderByGeneratedAtDesc(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard statistics snapshot not found"));

        return toResponse(snapshot);
    }

    private DashboardStatisticsResponse buildStatistics(
            User user,
            Long snapshotId,
            LocalDateTime generatedAt
    ) {
        List<Project> projects = getAccessibleProjects(user);

        long totalProjects = projects.size();
        long activeProjects = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.ACTIVE)
                .count();
        long completedProjects = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.COMPLETED)
                .count();

        long totalTasks = 0;
        long openTasks = 0;
        long completedTasks = 0;
        long overdueTasks = 0;
        long totalRisks = 0;
        long openRisks = 0;
        long highRisks = 0;
        long criticalRisks = 0;
        long progressReports = 0;
        long documents = 0;

        if (!projects.isEmpty()) {
            totalTasks = taskRepository.countByProjectIn(projects);
            openTasks = taskRepository.countByProjectInAndStatusNot(projects, TaskStatus.DONE);
            completedTasks = taskRepository.countByProjectInAndStatus(projects, TaskStatus.DONE);
            overdueTasks = taskRepository.countByProjectInAndDueDateBeforeAndStatusNot(
                    projects,
                    LocalDate.now(),
                    TaskStatus.DONE
            );
            totalRisks = riskRepository.countByProjectIn(projects);
            openRisks = riskRepository.countByProjectInAndStatusNot(projects, RiskStatus.CLOSED);
            highRisks = riskRepository.countByProjectInAndRiskLevel(projects, RiskLevel.HIGH);
            criticalRisks = riskRepository.countByProjectInAndRiskLevel(projects, RiskLevel.CRITICAL);
            progressReports = progressReportRepository.countByProjectIn(projects);
            documents = documentRepository.countByProjectIn(projects);
        }

        return DashboardStatisticsResponse.builder()
                .snapshotId(snapshotId)
                .userId(user.getId())
                .userName(user.getName())
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalTasks(totalTasks)
                .openTasks(openTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .totalRisks(totalRisks)
                .openRisks(openRisks)
                .highRisks(highRisks)
                .criticalRisks(criticalRisks)
                .progressReports(progressReports)
                .documents(documents)
                .generatedAt(generatedAt)
                .build();
    }

    private List<Project> getAccessibleProjects(User user) {
        Map<Long, Project> accessibleProjects = new LinkedHashMap<>();

        projectRepository.findByCreatedByOrderByCreatedAtDesc(user)
                .forEach(project -> accessibleProjects.put(project.getId(), project));

        registrationRepository.findByUser(user)
                .forEach(registration -> accessibleProjects.putIfAbsent(
                        registration.getProject().getId(),
                        registration.getProject()
                ));

        return accessibleProjects.values().stream().toList();
    }

    private DashboardStatisticsResponse toResponse(DashboardStatisticsSnapshot snapshot) {
        return DashboardStatisticsResponse.builder()
                .snapshotId(snapshot.getId())
                .userId(snapshot.getUser().getId())
                .userName(snapshot.getUser().getName())
                .totalProjects(snapshot.getTotalProjects())
                .activeProjects(snapshot.getActiveProjects())
                .completedProjects(snapshot.getCompletedProjects())
                .totalTasks(snapshot.getTotalTasks())
                .openTasks(snapshot.getOpenTasks())
                .completedTasks(snapshot.getCompletedTasks())
                .overdueTasks(snapshot.getOverdueTasks())
                .totalRisks(snapshot.getTotalRisks())
                .openRisks(snapshot.getOpenRisks())
                .highRisks(snapshot.getHighRisks())
                .criticalRisks(snapshot.getCriticalRisks())
                .progressReports(snapshot.getProgressReports())
                .documents(snapshot.getDocuments())
                .generatedAt(snapshot.getGeneratedAt())
                .build();
    }
}
