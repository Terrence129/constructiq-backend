package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:08 am
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectOrderByCreatedAtDesc(Project project);

}
