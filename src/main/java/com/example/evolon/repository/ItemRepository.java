package com.example.evolon.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.evolon.domain.enums.CardCondition;
import com.example.evolon.domain.enums.ListingType;
import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;
import com.example.evolon.entity.Item;
import com.example.evolon.entity.User;

/**
 * 商品（Item）エンティティ用 Repository
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

	// =========================
	// 通常検索（商品名 + ステータス）
	// =========================
	Page<Item> findByNameContainingIgnoreCaseAndStatus(
			String name,
			String status,
			Pageable pageable);

	// =========================
	// カテゴリ検索 + ステータス
	// =========================
	Page<Item> findByCategoryIdAndStatus(
			Long categoryId,
			String status,
			Pageable pageable);

	// =========================
	// 商品名 + カテゴリ + ステータス
	// =========================
	Page<Item> findByNameContainingIgnoreCaseAndCategoryIdAndStatus(
			String name,
			Long categoryId,
			String status,
			Pageable pageable);

	// =========================
	// ステータスのみ
	// =========================
	Page<Item> findByStatus(
			String status,
			Pageable pageable);

	// =========================
	// 出品者別一覧
	// =========================
	List<Item> findBySeller(User seller);

	// =========================
	// 最近の出品（管理用）
	// =========================
	List<Item> findTop5ByOrderByCreatedAtDesc();

	// =====================================================
	// ★ ポケモンカード用 詳細検索 ★
	// CardInfo と JOIN して絞り込みを行う
	// =====================================================
	@Query("""
			    SELECT i
			    FROM Item i
			    JOIN i.cardInfo c
			    WHERE i.status = '出品中'
			      AND (
			           :cardName IS NULL
			           OR :cardName = ''
			           OR LOWER(c.cardName) LIKE LOWER(CONCAT('%', :cardName, '%'))
			      )
			      AND (
			           :rarity IS NULL
			           OR c.rarity = :rarity
			      )
			      AND (
			           :regulation IS NULL
			           OR c.regulation = :regulation
			      )
			      AND (
			           :packName IS NULL
			           OR :packName = ''
			           OR LOWER(c.packName) LIKE LOWER(CONCAT('%', :packName, '%'))
			      )
			      AND (
			           :condition IS NULL
			           OR c.condition = :condition
			      )
			      AND (
			           :listingType IS NULL
			           OR i.listingType = :listingType
			      )
			      AND (
			           :minPrice IS NULL
			           OR i.price >= :minPrice
			      )
			      AND (
			           :maxPrice IS NULL
			           OR i.price <= :maxPrice
			      )
			""")

	Page<Item> searchPokemonCards(
			@Param("cardName") String cardName,
			@Param("rarity") Rarity rarity,
			@Param("regulation") Regulation regulation,
			@Param("packName") String packName,
			@Param("condition") CardCondition condition,
			@Param("listingType") ListingType listingType,
			@Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice,
			Pageable pageable);
}