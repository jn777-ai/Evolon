package com.example.evolon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.evolon.entity.AppOrder;
import com.example.evolon.entity.OrderStatus;
import com.example.evolon.entity.Review;
import com.example.evolon.entity.ReviewResult;
import com.example.evolon.entity.User;
import com.example.evolon.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;

	/* =====================
	 * 集計系
	 * ===================== */

	public long countGoodForUser(User user) {
		return reviewRepository.countByRevieweeAndResult(user, ReviewResult.GOOD);
	}

	public long countBadForUser(User user) {
		return reviewRepository.countByRevieweeAndResult(user, ReviewResult.BAD);
	}

	/* =====================
	 * レビュー済み判定
	 * ===================== */

	public boolean hasReviewed(Long orderId, User reviewer) {
		return reviewRepository.existsByOrder_IdAndReviewer(orderId, reviewer);
	}

	/* =====================
	 * レビュー作成（共通）
	 * ===================== */
	@Transactional
	public Review createReview(
			AppOrder order,
			User reviewer,
			ReviewResult result,
			String comment) {

		validateReviewInput(order, reviewer, result, comment);

		// 二重レビュー防止
		if (hasReviewed(order.getId(), reviewer)) {
			throw new IllegalStateException("すでに評価済みです");
		}

		boolean isBuyer = reviewer.getId().equals(order.getBuyer().getId());
		boolean isSeller = reviewer.getId().equals(order.getItem().getSeller().getId());

		if (!isBuyer && !isSeller) {
			throw new IllegalStateException("この取引を評価する権限がありません");
		}

		// 到着確認後のみ評価可
		if (order.getOrderStatus() != OrderStatus.DELIVERED
				&& order.getOrderStatus() != OrderStatus.COMPLETED) {
			throw new IllegalStateException("到着確認後に評価できます");
		}

		// 出品者は購入者評価が先
		if (isSeller) {
			boolean buyerReviewed = hasReviewed(order.getId(), order.getBuyer());
			if (!buyerReviewed) {
				throw new IllegalStateException("購入者の評価完了後に出品者は評価できます");
			}
		}

		User reviewee = isBuyer
				? order.getItem().getSeller()
				: order.getBuyer();

		Review review = new Review();
		review.setOrder(order);
		review.setReviewer(reviewer);
		review.setReviewee(reviewee);

		// 旧互換（DB制約対応）
		review.setSeller(order.getItem().getSeller());
		review.setItem(order.getItem());
		review.setRating(result == ReviewResult.GOOD ? 5 : 1);

		review.setResult(result);
		review.setComment(comment.trim());

		return reviewRepository.save(review);
	}

	/* =====================
	 * private
	 * ===================== */

	private void validateReviewInput(
			AppOrder order,
			User reviewer,
			ReviewResult result,
			String comment) {

		if (order == null || order.getId() == null) {
			throw new IllegalArgumentException("order が不正です");
		}
		if (reviewer == null) {
			throw new IllegalArgumentException("reviewer が不正です");
		}
		if (result == null) {
			throw new IllegalArgumentException("result が不正です");
		}
		if (comment == null || comment.trim().isEmpty()) {
			throw new IllegalArgumentException("comment は必須です");
		}
	}

	// 旧Controller互換（seller用）
	public long countGoodForSeller(User seller) {
		return countGoodForUser(seller);
	}

	public long countBadForSeller(User seller) {
		return countBadForUser(seller);
	}

}
