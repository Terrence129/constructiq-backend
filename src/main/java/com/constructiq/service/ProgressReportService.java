package com.constructiq.service;

import com.constructiq.dto.request.ProgressReportRequest;
import com.constructiq.dto.response.ProgressReportResponse;
import com.constructiq.config.CacheConfig;
import com.constructiq.entity.ProgressReport;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProgressReportRepository;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.UserProjectRegistrationRepository;
import org.springframework.security.core.Authentication;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:09 pm
 */

@Service
@RequiredArgsConstructor
public class ProgressReportService {

    private final ProjectRepository projectRepository;
    private final ProgressReportRepository progressReportRepository;
    private final UserProjectRegistrationRepository registrationRepository;
    private final Utils utils;
    private final ProjectAccessService projectAccessService;

//    createReport()
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROGRESS_REPORTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public ProgressReportResponse createProgressReport(Long projectId, ProgressReportRequest progressReportRequest, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);
        projectAccessService.checkProjectManagementAccess(project, currentUser);
        ProgressReport  progressReport = ProgressReport.builder()
                .project(project)
                .reportDate(progressReportRequest.getReportDate())
                .summary(progressReportRequest.getSummary())
                .completedWork(progressReportRequest.getCompletedWork())
                .delayedWork(progressReportRequest.getDelayedWork())
                .issues(progressReportRequest.getIssues())
                .nextActions(progressReportRequest.getNextActions())
                .createdBy(currentUser)
                .build();

        ProgressReport savedProgressReport = progressReportRepository.save(progressReport);
        return toResponse(savedProgressReport);
    }


    //    getReportsByProject()
    @Cacheable(cacheNames = CacheConfig.PROGRESS_REPORTS, key = "#authentication.name + ':project:' + #projectId")
    public List<ProgressReportResponse> getProgressReportsByProject(Long projectId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);
        projectAccessService.checkProjectAccess(project, currentUser);

        return progressReportRepository.findByProjectOrderByReportDateDesc(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = CacheConfig.PROGRESS_REPORTS, key = "#authentication.name + ':mine'")
    public List<ProgressReportResponse> getMyReports(Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        List<Project> accessibleProjects = getAccessibleProjects(currentUser);

        if (accessibleProjects.isEmpty()) {
            return List.of();
        }

        return progressReportRepository.findAccessibleProgressReports(accessibleProjects)
                .stream()
                .map(this::toResponse)
                .toList();
    }

//    getReportById()
    @Cacheable(cacheNames = CacheConfig.PROGRESS_REPORTS, key = "#authentication.name + ':id:' + #progressReportId")
    public ProgressReportResponse getProgressReportById(Long progressReportId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        ProgressReport progressReport = getProgressReport(progressReportId);
        projectAccessService.checkProjectAccess(progressReport.getProject(), currentUser);
        return toResponse(progressReport);
    }

//    updateReport()
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROGRESS_REPORTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public ProgressReportResponse updateProgressReport(Long progressReportId, ProgressReportRequest progressReportRequest, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        ProgressReport progressReport = getProgressReport(progressReportId);
        projectAccessService.checkProjectManagementAccess(progressReport.getProject(), currentUser);
        if (progressReportRequest.getReportDate() != null) {
            progressReport.setReportDate(progressReportRequest.getReportDate());
        }
        if (progressReportRequest.getSummary() != null) {
            progressReport.setSummary(progressReportRequest.getSummary());
        }
        progressReport.setCompletedWork(progressReportRequest.getCompletedWork());
        progressReport.setDelayedWork(progressReportRequest.getDelayedWork());
        progressReport.setIssues(progressReportRequest.getIssues());
        progressReport.setNextActions(progressReportRequest.getNextActions());
        ProgressReport savedProgressreport = progressReportRepository.save(progressReport);
        return toResponse(savedProgressreport);

    }

//    deleteReport()
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.PROGRESS_REPORTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public void deleteProgressReportById(Long progressReportId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        ProgressReport progressReport = getProgressReport(progressReportId);
        projectAccessService.checkProjectAccess(progressReport.getProject(), currentUser);
        progressReportRepository.delete(progressReport);

    }

    private Project getProject(Long projectId) {

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
    private ProgressReport getProgressReport(Long id) {
        return progressReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progress Report not found"));
    }

    private List<Project> getAccessibleProjects(User currentUser) {
        Map<Long, Project> accessibleProjects = new LinkedHashMap<>();

        projectRepository.findByCreatedByOrderByCreatedAtDesc(currentUser)
                .forEach(project -> accessibleProjects.put(project.getId(), project));

        registrationRepository.findByUser(currentUser)
                .stream()
                .map(UserProjectRegistration::getProject)
                .forEach(project -> accessibleProjects.putIfAbsent(project.getId(), project));

        return List.copyOf(accessibleProjects.values());
    }

    private ProgressReportResponse toResponse(ProgressReport progressReport) {
        return ProgressReportResponse.builder()
                .id(progressReport.getId())
                .projectId(progressReport.getProject().getId())
                .projectName(progressReport.getProject().getName())
                .reportDate(progressReport.getReportDate())
                .summary(progressReport.getSummary())
                .completedWork(progressReport.getCompletedWork())
                .delayedWork(progressReport.getDelayedWork())
                .issues(progressReport.getIssues())
                .nextActions(progressReport.getNextActions())
                .createdById(progressReport.getCreatedBy().getId())
                .createdByName(progressReport.getCreatedBy().getName())
                .createdAt(progressReport.getCreatedAt())
                .updatedAt(progressReport.getUpdatedAt())
                .build();
    }
}
