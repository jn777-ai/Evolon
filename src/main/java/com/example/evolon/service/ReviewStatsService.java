package com.example.evolon.service;

import org.springframework.stereotype.Service;

import com.example.evolon.entity.ReviewStats;
import com.example.evolon.entity.User;
import com.example.evolon.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewStatsService {

	private final ReviewRepository reviewRepository;

	//  公開対象（2者評価が揃ったレビュー）だけで集計
	public ReviewStats getStats(User user) {
		long count = reviewRepository.countVisibleByReviewee(user);
		Double avg = reviewRepository.avgVisibleRatingByReviewee(user);

		ReviewStats rs = new ReviewStats();
		rs.setUser(user);
		rs.setReviewCount((int) count);
		rs.setAvgRating(avg != null ? avg : 0.0);
		return rs;
	}
}
