package com.example.evolon.entity;

import java.math.BigDecimal;
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

import com.example.evolon.domain.enums.ShippingDuration;
import com.example.evolon.domain.enums.ShippingFeeBurden;
import com.example.evolon.domain.enums.ShippingRegion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品エンティティ
 */
@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

	// 主キー
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 出品者（User）とのリレーション。NULL禁止
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User seller;

	// 商品名
	@Column(nullable = false)
	private String name;

	// 商品説明（TEXT型）
	@Column(columnDefinition = "TEXT")
	private String description;

	// 価格
	@Column(nullable = false)
	private BigDecimal price;

	// 発送目安
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_duration", nullable = false)
	private ShippingDuration shippingDuration;

	// 送料負担
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_fee_burden", nullable = false)
	private ShippingFeeBurden shippingFeeBurden;

	// 発送地域（Enum型に修正）
	@Enumerated(EnumType.STRING)
	@Column(name = "shipping_region", nullable = false)
	private ShippingRegion shippingRegion;

	// カテゴリ（未分類を許容するためnullable）
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	// 出品ステータス（例：出品中 / 売却済）
	private String status;

	// 画像URL
	private String imageUrl;

	// 作成日時
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}
