package com.example.evolon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_master", uniqueConstraints = @UniqueConstraint(columnNames = { "set_code", "card_number" }))
@Data
@NoArgsConstructor // JPA用デフォルトコンストラクタ
public class CardMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String setCode;

	@Column(nullable = false)
	private String cardNumber;

	@Column(nullable = false)
	private String cardName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rarity rarity;

	private String packName;

	@Enumerated(EnumType.STRING)
	private Regulation regulation;

	// 任意のコンストラクタ
	public CardMaster(Long id, String setCode, String cardNumber, String cardName,
			Rarity rarity, String packName, Regulation regulation) {
		this.id = id;
		this.setCode = setCode;
		this.cardNumber = cardNumber;
		this.cardName = cardName;
		this.rarity = rarity;
		this.packName = packName;
		this.regulation = regulation;
	}
}
