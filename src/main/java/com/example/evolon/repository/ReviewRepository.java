package com.example.evolon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.evolon.entity.Review;
import com.example.evolon.entity.ReviewResult;
import com.example.evolon.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	/* =========================
	 * 既存（内部用：全件）
	 * ========================= */

	// 受け取った評価（全部）
	List<Review> findByRevieweeOrderByCreatedAtDesc(User reviewee);

	// レビュー済み判定
	boolean existsByOrder_IdAndReviewer(Long orderId, User reviewer);

	// GOOD / BAD 件数（全部）
	long countByRevieweeAndResult(User reviewee, ReviewResult result);

	// 件数（全部）
	@Query("select count(r) from Review r where r.reviewee = :user")
	long countByReviewee(@Param("user") User user);

	// 平均（全部）
	@Query("select avg(r.rating) from Review r where r.reviewee = :user")
	Double avgRatingByReviewee(@Param("user") User user);

	/* =========================
	 * 公開用（2者の評価が揃った注文だけ）
	 * - order ごとに reviewer が2人いる（購入者 & 出品者）ことを条件にする
	 * ========================= */

	//  一覧：公開対象だけ（2者レビュー揃った注文のみ）
	@Query("""
				SELECT r
				FROM Review r
				WHERE r.reviewee = :user
				  AND r.order.id IN (
				    SELECT r2.order.id
				    FROM Review r2
				    GROUP BY r2.order.id
				    HAVING COUNT(DISTINCT r2.reviewer.id) >= 2
				  )
				ORDER BY r.createdAt DESC
			""")
	List<Review> findVisibleReviewsForUser(@Param("user") User user);

	//  件数：公開対象だけ
	@Query("""
				SELECT count(r)
				FROM Review r
				WHERE r.reviewee = :user
				  AND r.order.id IN (
				    SELECT r2.order.id
				    FROM Review r2
				    GROUP BY r2.order.id
				    HAVING COUNT(DISTINCT r2.reviewer.id) >= 2
				  )
			""")
	long countVisibleByReviewee(@Param("user") User user);

	//  平均：公開対象だけ
	@Query("""
				SELECT avg(r.rating)
				FROM Review r
				WHERE r.reviewee = :user
				  AND r.order.id IN (
				    SELECT r2.order.id
				    FROM Review r2
				    GROUP BY r2.order.id
				    HAVING COUNT(DISTINCT r2.reviewer.id) >= 2
				  )
			""")
	Double avgVisibleRatingByReviewee(@Param("user") User user);

	//  GOOD/BAD 件数：公開対象だけ
	@Query("""
				SELECT count(r)
				FROM Review r
				WHERE r.reviewee = :user
				  AND r.result = :result
				  AND r.order.id IN (
				    SELECT r2.order.id
				    FROM Review r2
				    GROUP BY r2.order.id
				    HAVING COUNT(DISTINCT r2.reviewer.id) >= 2
				  )
			""")
	long countVisibleByRevieweeAndResult(
			@Param("user") User user,
			@Param("result") ReviewResult result);
}
