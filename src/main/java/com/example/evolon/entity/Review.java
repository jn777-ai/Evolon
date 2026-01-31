package com.example.evolon.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(columnNames = { "order_id", "reviewer_id" }))
@Data
@NoArgsConstructor
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 注文
	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private AppOrder order;

	// レビューを書く人
	@ManyToOne
	@JoinColumn(name = "reviewer_id", nullable = false)
	private User reviewer;

	// レビューされる人
	@ManyToOne
	@JoinColumn(name = "reviewee_id", nullable = false)
	private User reviewee;

	// 旧互換（DBのため残す）
	@ManyToOne
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;

	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@Enumerated(EnumType.STRING)
	@Column(name = "result", nullable = false, length = 10)
	private ReviewResult result;

	@Column(name = "rating", nullable = false)
	private Integer rating;

	@Column(name = "comment")
	private String comment;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;
}
