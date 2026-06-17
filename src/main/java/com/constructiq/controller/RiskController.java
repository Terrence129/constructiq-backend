package com.constructiq.controller;

import com.constructiq.dto.request.RiskRequest;
import com.constructiq.dto.response.RiskResponse;
import com.constructiq.service.RiskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @PostMapping("/api/projects/{projectId}/risks")
    public RiskResponse createRisk(
            @PathVariable Long projectId,
            @Valid @RequestBody RiskRequest request,
            Authentication authentication
    ) {
        return riskService.createRisk(projectId, request, authentication);
    }

    @GetMapping("/api/projects/{projectId}/risks")
    public List<RiskResponse> getRisksByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return riskService.getRisksByProject(projectId, authentication);
    }

    @GetMapping("/api/risks/{riskId}")
    public RiskResponse getRiskById(
            @PathVariable Long riskId,
            Authentication authentication
    ) {
        return riskService.getRiskById(riskId, authentication);
    }

    @PutMapping("/api/risks/{riskId}")
    public RiskResponse updateRisk(
            @PathVariable Long riskId,
            @Valid @RequestBody RiskRequest request,
            Authentication authentication
    ) {
        return riskService.updateRisk(riskId, request, authentication);
    }

    @DeleteMapping("/api/risks/{riskId}")
    public void deleteRisk(
            @PathVariable Long riskId,
            Authentication authentication
    ) {
        riskService.deleteRisk(riskId, authentication);
    }
}
