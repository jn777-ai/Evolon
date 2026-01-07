package com.example.evolon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evolon.entity.Review;
import com.example.evolon.entity.ReviewResult;
import com.example.evolon.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByOrder_IdAndReviewer(Long orderId, User reviewer);

	long countByRevieweeAndResult(User reviewee, ReviewResult result);
}
