package com.example.evolon.service;

// I/O 例外処理
import java.io.IOException;
// 金額
import java.math.BigDecimal;
// 一覧返却
import java.util.List;
// Optional
import java.util.Optional;

// ページング
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// Service
import org.springframework.stereotype.Service;
// ファイルアップロード
import org.springframework.web.multipart.MultipartFile;

// Enum
import com.example.evolon.domain.enums.CardCondition;
import com.example.evolon.domain.enums.ListingType;
import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;
// Entity
import com.example.evolon.entity.Item;
import com.example.evolon.entity.User;
// Repository
import com.example.evolon.repository.ItemRepository;

@Service
public class ItemService {

	// =========================
	// フィールド
	// =========================
	private final ItemRepository itemRepository;
	private final CategoryService categoryService;
	private final CloudinaryService cloudinaryService;

	// =========================
	// コンストラクタ
	// =========================
	public ItemService(
			ItemRepository itemRepository,
			CategoryService categoryService,
			CloudinaryService cloudinaryService) {

		this.itemRepository = itemRepository;
		this.categoryService = categoryService;
		this.cloudinaryService = cloudinaryService;
	}

	// =========================
	// 通常の商品検索（一覧用）
	// =========================
	public Page<Item> searchItems(String keyword, Long categoryId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		if (keyword != null && !keyword.isEmpty() && categoryId != null) {
			return itemRepository
					.findByNameContainingIgnoreCaseAndCategoryIdAndStatus(
							keyword, categoryId, "出品中", pageable);

		} else if (keyword != null && !keyword.isEmpty()) {
			return itemRepository
					.findByNameContainingIgnoreCaseAndStatus(
							keyword, "出品中", pageable);

		} else if (categoryId != null) {
			return itemRepository
					.findByCategoryIdAndStatus(
							categoryId, "出品中", pageable);

		} else {
			return itemRepository
					.findByStatus("出品中", pageable);
		}
	}

	// =========================
	// ★ ポケモンカード詳細検索
	// =========================
	public Page<Item> searchPokemonCards(
			String cardName,
			Rarity rarity,
			Regulation regulation,
			String packName,
			CardCondition condition,
			ListingType listingType,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			Pageable pageable) {

		return itemRepository.searchPokemonCards(
				cardName,
				rarity,
				regulation,
				packName,
				condition,
				listingType,
				minPrice,
				maxPrice,
				pageable);
	}

	// =========================
	// 全商品取得（管理用）
	// =========================
	public List<Item> getAllItems() {
		return itemRepository.findAll();
	}

	// =========================
	// 商品ID取得
	// =========================
	public Optional<Item> getItemById(Long id) {
		return itemRepository.findById(id);
	}

	// =========================
	// 商品保存（画像対応）
	// =========================
	public Item saveItem(Item item, MultipartFile imageFile) throws IOException {

		if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadFile(imageFile);
			item.setImageUrl(imageUrl);
		}

		return itemRepository.save(item);
	}

	// =========================
	// 商品削除
	// =========================
	public void deleteItem(Long id) {

		itemRepository.findById(id).ifPresent(item -> {

			if (item.getImageUrl() != null) {
				cloudinaryService.deleteFile(item.getImageUrl());
			}

			itemRepository.deleteById(id);
		});
	}

	// =========================
	// 出品者別一覧
	// =========================
	public List<Item> getItemsBySeller(User seller) {
		return itemRepository.findBySeller(seller);
	}

	// =========================
	// 売却確定
	// =========================
	public void markItemAsSold(Long itemId) {

		itemRepository.findById(itemId).ifPresent(item -> {
			item.setStatus("売却済");
			itemRepository.save(item);
		});
	}

	// =========================
	// 最近の出品（管理者用）
	// =========================
	public List<Item> getRecentItems() {
		return itemRepository.findTop5ByOrderByCreatedAtDesc();
	}
}
