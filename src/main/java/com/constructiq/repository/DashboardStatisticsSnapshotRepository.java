package com.constructiq.repository;

import com.constructiq.entity.DashboardStatisticsSnapshot;
import com.constructiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardStatisticsSnapshotRepository extends JpaRepository<DashboardStatisticsSnapshot, Long> {

    Optional<DashboardStatisticsSnapshot> findFirstByUserOrderByGeneratedAtDesc(User user);
}
