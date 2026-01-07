package com.example.evolon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.example.evolon.domain.enums.CardCondition;
import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * カード固有情報エンティティ
 * （カード名・レアリティ・レギュレーション・状態など）
 */
@Entity
@Table(name = "card_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo {

	// =========================
	// 主キー
	// =========================
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// =========================
	// 商品（1対1）
	// =========================
	@OneToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	// =========================
	// カード名
	// =========================
	@Column(name = "card_name", nullable = false)
	private String cardName;

	// =========================
	// レアリティ
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "rarity", nullable = false)
	private Rarity rarity;

	// =========================
	// レギュレーション
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "regulation", nullable = false)
	private Regulation regulation;

	// =========================
	// 封入パック名（任意）
	// =========================
	@Column(name = "pack_name")
	private String packName;

	// =========================
	// カード状態（例：MINT, NEAR_MINT, USED）
	// Thymeleaf で item.cardInfo.condition にアクセスするために追加
	// =========================
	@Enumerated(EnumType.STRING)
	@Column(name = "condition", nullable = false)
	private CardCondition condition;
}
