package com.constructiq.service;

import com.constructiq.dto.request.ProgressReportRequest;
import com.constructiq.dto.response.ProgressReportResponse;
import com.constructiq.entity.ProgressReport;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProgressReportRepository;
import com.constructiq.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final Utils utils;

//    createReport()
    public ProgressReportResponse createProgressReport(Long projectId, ProgressReportRequest progressReportRequest, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        checkProjectReportWriteAuthority(projectId, currentUser);
        Project project = getProject(projectId);
        ProgressReport  progressReport = ProgressReport.builder()
                .project(project)
                .reportDate(progressReportRequest.getReportDate())
                .summary(progressReportRequest.getSummary())
                .delayedWork(progressReportRequest.getDelayedWork())
                .issues(progressReportRequest.getIssues())
                .nextActions(progressReportRequest.getNextActions())
                .build();

        ProgressReport savedProgressReport = progressReportRepository.save(progressReport);
        return toResponse(savedProgressReport);
    }


    //    getReportsByProject()
    public List<ProgressReportResponse> getReportsByProject(Long projectId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        return progressReportRepository.findByProjectOrderByReportDateDesc(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

//    getReportById()
    public ProgressReportResponse getProgressReportById(Long progressReportId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        ProgressReport progressReport = getProgressReport(progressReportId);
        checkProjectReportReadAuthority(progressReport.getProject().getId(), currentUser);
        return toResponse(progressReport);
    }

//    updateReport()
    public  ProgressReportResponse updateProgressReport(Long progressReportId, ProgressReportRequest progressReportRequest, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        checkProjectReportWriteAuthority(progressReportId, currentUser);
        ProgressReport progressReport = getProgressReport(progressReportId);
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
    public void deleteProgressReportById(Long progressReportId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        checkProjectReportWriteAuthority(progressReportId, currentUser);
        ProgressReport progressReport = getProgressReport(progressReportId);
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

    private void checkProjectReportReadAuthority(Long projectId, User currentUser) {
        boolean noPermission = false;
        //  TODO: add authentication logic

        if (noPermission) {
            throw new IllegalArgumentException("You do not have permission to read this progress report");
        }
    }
    private void checkProjectReportWriteAuthority(Long projectId, User currentUser) {
        boolean noPermission = false;
        //  TODO: add authentication logic
        if (noPermission) {
            throw new IllegalArgumentException("You do not have permission to write this progress report");
        }
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
                .build();
    }
}
