package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.Task;
import com.constructiq.enums.TaskPriority;
import com.constructiq.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 29/5/2026 12:08 am
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectOrderByCreatedAtDesc(Project project);

    @Query("""
            select task
            from Task task
            join fetch task.project project
            where project in :projects
              and (:status is null or task.status = :status)
              and (:priority is null or task.priority = :priority)
            order by task.createdAt desc, task.id desc
            """)
    List<Task> findAccessibleTasks(
            @Param("projects") List<Project> projects,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority
    );

    long countByProjectIn(List<Project> projects);

    long countByProjectInAndStatus(List<Project> projects, TaskStatus status);

    long countByProjectInAndStatusNot(List<Project> projects, TaskStatus status);

    long countByProjectInAndDueDateBeforeAndStatusNot(
            List<Project> projects,
            LocalDate dueDate,
            TaskStatus status
    );
}
