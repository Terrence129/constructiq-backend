package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 28/5/2026 3:33 pm
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCreatedByOrderByCreatedAtDesc(User user);
}
