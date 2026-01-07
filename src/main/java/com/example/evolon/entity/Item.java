package com.example.evolon.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.example.evolon.domain.enums.CardCondition;
import com.example.evolon.domain.enums.ListingType;
import com.example.evolon.domain.enums.ShippingDuration;
import com.example.evolon.domain.enums.ShippingFeeBurden;
import com.example.evolon.domain.enums.ShippingMethod;
import com.example.evolon.domain.enums.ShippingRegion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品エンティティ（出品単位）
 */
@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

	// =========================
	// 主キー
	// =========================
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// =========================
	// 出品者
	// =========================
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User seller;

	// =========================
	// 商品名（表示用）
	// =========================
	@Column(nullable = false)
	private String name;

	// =========================
	// 商品説明
	// =========================
	@Column(columnDefinition = "TEXT")
	private String description;

	// =========================
	// 価格
	// =========================
	@Column(nullable = false)
	private BigDecimal price;

	// =========================
	// 発送目安
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_duration", nullable = false)
	private ShippingDuration shippingDuration;

	// =========================
	// 送料負担
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_fee_burden", nullable = false)
	private ShippingFeeBurden shippingFeeBurden;

	// =========================
	// 発送地域
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_region", nullable = false)
	private ShippingRegion shippingRegion;

	// =========================
	// 発送方法
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_method", nullable = false)
	private ShippingMethod shippingMethod;

	// =========================
	// 出品タイプ（SINGLE / BOX / DECK）
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "listing_type", nullable = false)
	private ListingType listingType;

	// =========================
	// カード状態
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "condition", nullable = false)
	private CardCondition condition;

	// =========================
	// カテゴリ
	// =========================
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	// =========================
	// 出品ステータス
	// =========================
	@Column(nullable = false)
	private String status;

	// =========================
	// 画像URL
	// =========================
	private String imageUrl;

	// =========================
	// カード情報（1対1）
	// ★ フィールド初期化しない（JPA事故防止）
	// =========================
	@OneToOne(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private CardInfo cardInfo;

	// =========================
	// 作成日時
	// =========================
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	// =========================
	// 保存前処理
	// =========================
	@PrePersist
	public void prePersist() {

		// 作成日時を自動設定
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}

		// 出品ステータス初期値
		if (this.status == null) {
			this.status = "出品中";
		}

		// CardInfo が未設定なら生成
		if (this.cardInfo == null) {
			this.cardInfo = new CardInfo();
		}

		// 双方向関連を必ずセット
		this.cardInfo.setItem(this);
	}
}
