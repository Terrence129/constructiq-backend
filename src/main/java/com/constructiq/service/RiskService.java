package com.constructiq.service;

import com.constructiq.dto.request.RiskRequest;
import com.constructiq.dto.response.RiskResponse;
import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import com.constructiq.entity.User;
import com.constructiq.enums.RiskCategory;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.repository.RiskRepository;
import com.constructiq.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskRepository riskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessService projectAccessService;
    private final Utils utils;

    public RiskResponse createRisk(Long projectId, RiskRequest request, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        Risk risk = Risk.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory() == null ? RiskCategory.GENERAL : request.getCategory())
                .probability(request.getProbability())
                .impact(request.getImpact())
                .severity(calculateSeverity(request.getProbability(), request.getImpact()))
                .riskLevel(calculateRiskLevel(request.getProbability(), request.getImpact()))
                .status(request.getStatus() == null ? RiskStatus.OPEN : request.getStatus())
                .mitigationPlan(request.getMitigationPlan())
                .owner(request.getOwner())
                .targetDate(request.getTargetDate())
                .createdBy(currentUser)
                .build();

        return toResponse(riskRepository.save(risk));
    }

    public List<RiskResponse> getRisksByProject(Long projectId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectAccess(project, currentUser);

        return riskRepository.findByProjectOrderByCreatedAtDesc(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RiskResponse getRiskById(Long riskId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Risk risk = getRisk(riskId);

        projectAccessService.checkProjectAccess(risk.getProject(), currentUser);

        return toResponse(risk);
    }

    public RiskResponse updateRisk(Long riskId, RiskRequest request, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Risk risk = getRisk(riskId);

        projectAccessService.checkProjectManagementAccess(risk.getProject(), currentUser);

        if (request.getTitle() != null) {
            risk.setTitle(request.getTitle());
        }

        risk.setDescription(request.getDescription());

        if (request.getCategory() != null) {
            risk.setCategory(request.getCategory());
        }

        if (request.getProbability() != null) {
            risk.setProbability(request.getProbability());
        }

        if (request.getImpact() != null) {
            risk.setImpact(request.getImpact());
        }

        risk.setSeverity(calculateSeverity(risk.getProbability(), risk.getImpact()));
        risk.setRiskLevel(calculateRiskLevel(risk.getProbability(), risk.getImpact()));

        if (request.getStatus() != null) {
            risk.setStatus(request.getStatus());
        }

        risk.setMitigationPlan(request.getMitigationPlan());
        risk.setOwner(request.getOwner());
        risk.setTargetDate(request.getTargetDate());

        return toResponse(riskRepository.save(risk));
    }

    public void deleteRisk(Long riskId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Risk risk = getRisk(riskId);

        projectAccessService.checkProjectManagementAccess(risk.getProject(), currentUser);

        riskRepository.delete(risk);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Risk getRisk(Long riskId) {
        return riskRepository.findById(riskId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk not found"));
    }

    private int calculateSeverity(Integer probability, Integer impact) {
        return probability * impact;
    }

    private RiskLevel calculateRiskLevel(Integer probability, Integer impact) {
        int severity = calculateSeverity(probability, impact);

        if (severity >= 16) {
            return RiskLevel.CRITICAL;
        }

        if (severity >= 11) {
            return RiskLevel.HIGH;
        }

        if (severity >= 6) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    private RiskResponse toResponse(Risk risk) {
        return RiskResponse.builder()
                .id(risk.getId())
                .projectId(risk.getProject().getId())
                .projectName(risk.getProject().getName())
                .title(risk.getTitle())
                .description(risk.getDescription())
                .category(risk.getCategory())
                .probability(risk.getProbability())
                .impact(risk.getImpact())
                .severity(risk.getSeverity())
                .riskLevel(risk.getRiskLevel())
                .status(risk.getStatus())
                .mitigationPlan(risk.getMitigationPlan())
                .owner(risk.getOwner())
                .targetDate(risk.getTargetDate())
                .createdById(risk.getCreatedBy().getId())
                .createdByName(risk.getCreatedBy().getName())
                .createdAt(risk.getCreatedAt())
                .updatedAt(risk.getUpdatedAt())
                .build();
    }
}
