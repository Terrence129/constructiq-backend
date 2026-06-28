package com.constructiq.repository;

import com.constructiq.entity.Document;
import com.constructiq.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByProjectOrderByCreatedAtDesc(Project project);

    List<Document> findByProjectInOrderByCreatedAtDesc(List<Project> projects);

    long countByProjectIn(List<Project> projects);
}
