package com.constructiq.repository;

import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.entity.UserProjectRegistration;
import com.constructiq.enums.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProjectRegistrationRepository extends JpaRepository<UserProjectRegistration, Long> {

    boolean existsByUserAndProject(User user, Project project);

    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    boolean existsByUserIdAndProjectIdAndRole(Long userId, Long projectId, ProjectMemberRole role);

    List<UserProjectRegistration> findByProject(Project project);

    List<UserProjectRegistration> findByUser(User user);
}
