package com.example.evolon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evolon.entity.ReviewStats;
import com.example.evolon.entity.User;

public interface ReviewStatsRepository extends JpaRepository<ReviewStats, Long> {
	Optional<ReviewStats> findByUser(User user);
}
