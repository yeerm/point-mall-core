package com.pointmall.core.repository;

import com.pointmall.core.entity.point_history.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository <PointHistory, Long> {
}
