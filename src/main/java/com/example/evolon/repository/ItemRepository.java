package com.example.evolon.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evolon.entity.Item;
import com.example.evolon.entity.ItemStatus;
import com.example.evolon.entity.User;

public interface ItemRepository extends JpaRepository<Item, Long> {

	/* =========================
	 * 公開中（出品中）検索
	 * ========================= */

	// ステータスのみ（公開中一覧など）
	Page<Item> findByStatus(
			ItemStatus status,
			Pageable pageable);

	// 名前の部分一致 + ステータス
	Page<Item> findByNameContainingIgnoreCaseAndStatus(
			String name,
			ItemStatus status,
			Pageable pageable);

	// カテゴリ + ステータス
	Page<Item> findByCategory_IdAndStatus(
			Long categoryId,
			ItemStatus status,
			Pageable pageable);

	// 名前 + カテゴリ + ステータス
	Page<Item> findByNameContainingIgnoreCaseAndCategory_IdAndStatus(
			String name,
			Long categoryId,
			ItemStatus status,
			Pageable pageable);

	/* =========================
	 * 出品者
	 * ========================= */

	// 出品者の全商品
	List<Item> findBySeller(User seller);

	// 出品者 + ステータス（SELLING / SOLD / SUSPENDED など）
	List<Item> findBySellerAndStatus(
			User seller,
			ItemStatus status);

	/* =========================
	 * 管理・ダッシュボード用
	 * ========================= */

	// 最近の出品商品
	List<Item> findTop5ByOrderByCreatedAtDesc();
}
