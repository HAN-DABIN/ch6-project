package com.example.ch6project.domain.pointHistory.repository;

import com.example.ch6project.domain.pointHistory.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
