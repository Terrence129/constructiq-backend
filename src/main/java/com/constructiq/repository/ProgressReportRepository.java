package com.constructiq.repository;

import com.constructiq.entity.ProgressReport;
import com.constructiq.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 7:07 pm
 */
public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {

    List<ProgressReport> findByProjectOrderByReportDateDesc(Project project);
}
