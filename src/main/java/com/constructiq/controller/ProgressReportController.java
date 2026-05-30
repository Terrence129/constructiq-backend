package com.constructiq.controller;

import com.constructiq.dto.request.ProgressReportRequest;
import com.constructiq.dto.response.ProgressReportResponse;
import com.constructiq.service.ProgressReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:09 pm
 */

@RestController
@RequiredArgsConstructor
public class ProgressReportController {
    private final ProgressReportService progressReportService;

    @PostMapping("/api/projects/{projectId}/progressReports")
    public ProgressReportResponse createProgressReport(
            @PathVariable Long projectId,
            @RequestBody ProgressReportRequest progressReportRequest,
            Authentication authentication
    ){
        return progressReportService.createProgressReport(projectId, progressReportRequest, authentication);
    }

    @GetMapping("/api/projects/{projectId}/progressReports")
    public List<ProgressReportResponse> getProgressReportsByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ){
        return progressReportService.getProgressReportsByProject(projectId, authentication);
    }

    @GetMapping("/api/progressReports/{progressReportId}")
    public ProgressReportResponse getProgressReportById(
            @PathVariable Long progressReportId, Authentication authentication
    ){
        return progressReportService.getProgressReportById(progressReportId, authentication);
    }

    @PutMapping("/api/progressReports/{progressReportId}")
    public ProgressReportResponse updateProgressReport(
            @PathVariable Long progressReportId,
            @RequestBody ProgressReportRequest progressReportRequest,
            Authentication authentication
    ){
        return progressReportService.updateProgressReport(progressReportId, progressReportRequest, authentication);
    }

    @DeleteMapping("/api/progressReports/{progressReportId}")
    public void deleteProgressReport(
            @PathVariable Long progressReportId, Authentication authentication){
        progressReportService.deleteProgressReportById(progressReportId, authentication);
    }
}
