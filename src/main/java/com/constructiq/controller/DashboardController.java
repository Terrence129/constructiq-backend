package com.constructiq.controller;

import com.constructiq.dto.response.DashboardStatisticsResponse;
import com.constructiq.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public DashboardStatisticsResponse getStatistics(Authentication authentication) {
        return dashboardService.getStatistics(authentication);
    }

    @PostMapping("/statistics/snapshots")
    public DashboardStatisticsResponse createSnapshot(Authentication authentication) {
        return dashboardService.createSnapshot(authentication);
    }

    @GetMapping("/statistics/snapshots/latest")
    public DashboardStatisticsResponse getLatestSnapshot(Authentication authentication) {
        return dashboardService.getLatestSnapshot(authentication);
    }
}
