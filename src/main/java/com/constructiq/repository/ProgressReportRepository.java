package com.constructiq.repository;

import com.constructiq.entity.ProgressReport;
import com.constructiq.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:07 pm
 */
public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {

    List<ProgressReport> findByProjectOrderByReportDateDesc(Project project);

    @Query("""
            select progressReport
            from ProgressReport progressReport
            join fetch progressReport.project project
            join fetch progressReport.createdBy createdBy
            where project in :projects
            order by progressReport.reportDate desc, progressReport.createdAt desc, progressReport.id desc
            """)
    List<ProgressReport> findAccessibleProgressReports(@Param("projects") List<Project> projects);

    long countByProjectIn(List<Project> projects);
}
