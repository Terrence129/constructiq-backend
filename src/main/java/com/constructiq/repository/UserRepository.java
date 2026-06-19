package com.constructiq.repository;

import com.constructiq.dto.projection.UserSummaryProjection;
import com.constructiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/27 21:34
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""

            select u.id as id, u.name as name, u.email as email, u.role as role
            from User u
            where (:name is null or lower(u.name) like concat('%', cast(:name as string), '%'))
              and (:email is null or lower(u.email) like concat('%', cast(:email as string), '%'))
            order by lower(u.name), lower(u.email), u.id
            """)
    List<UserSummaryProjection> findUserSummaries(
            @Param("name") String name,
            @Param("email") String email
    );

    @Query("""
            select u.id as id, u.name as name, u.email as email, u.role as role
            from User u
            where u.id = :id
            """)
    Optional<UserSummaryProjection> findUserSummaryById(@Param("id") Long id);
}
