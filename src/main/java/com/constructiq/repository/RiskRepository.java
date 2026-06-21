package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import com.constructiq.enums.RiskCategory;
import com.constructiq.enums.RiskLevel;
import com.constructiq.enums.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk, Long> {

    List<Risk> findByProjectOrderByCreatedAtDesc(Project project);

    @Query("""
            select risk
            from Risk risk
            join fetch risk.project project
            join fetch risk.createdBy createdBy
            where project in :projects
              and (:category is null or risk.category = :category)
              and (:riskLevel is null or risk.riskLevel = :riskLevel)
              and (:status is null or risk.status = :status)
            order by risk.createdAt desc, risk.id desc
            """)
    List<Risk> findAccessibleRisks(
            @Param("projects") List<Project> projects,
            @Param("category") RiskCategory category,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("status") RiskStatus status
    );

    long countByProjectIn(List<Project> projects);

    long countByProjectInAndRiskLevel(List<Project> projects, RiskLevel riskLevel);

    long countByProjectInAndStatusNot(List<Project> projects, RiskStatus status);
}
