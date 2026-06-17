package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk, Long> {

    List<Risk> findByProjectOrderByCreatedAtDesc(Project project);
}
