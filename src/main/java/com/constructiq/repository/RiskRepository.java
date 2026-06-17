package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk, Long> {

    List<Risk> findByProjectOrderByCreatedAtDesc(Project project);

    long countByProjectIn(List<Project> projects);

    long countByProjectInAndRiskLevel(List<Project> projects, RiskLevel riskLevel);

    long countByProjectInAndStatusNot(List<Project> projects, RiskStatus status);
}
