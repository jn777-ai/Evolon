package com.example.evolon.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.evolon.domain.enums.CardCondition;
import com.example.evolon.domain.enums.ListingType;
import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;
import com.example.evolon.domain.enums.ShippingDuration;
import com.example.evolon.domain.enums.ShippingFeeBurden;
import com.example.evolon.domain.enums.ShippingMethod;
import com.example.evolon.domain.enums.ShippingRegion;
import com.example.evolon.entity.CardInfo;
import com.example.evolon.entity.Category;
import com.example.evolon.entity.Item;
import com.example.evolon.entity.User;
import com.example.evolon.service.CategoryService;
import com.example.evolon.service.ChatService;
import com.example.evolon.service.FavoriteService;
import com.example.evolon.service.ItemService;
import com.example.evolon.service.ReviewService;
import com.example.evolon.service.UserService;

/**
 * 商品（Item）に関する MVC コントローラ
 * ・商品一覧
 * ・商品詳細
 * ・商品出品
 * ・ポケモンカード検索
 */
@Controller
@RequestMapping("/items")
public class ItemController {

	// =========================
	// Service 注入
	// =========================
	private final ItemService itemService;
	private final CategoryService categoryService;
	private final UserService userService;
	private final ChatService chatService;
	private final FavoriteService favoriteService;
	private final ReviewService reviewService;

	public ItemController(
			ItemService itemService,
			CategoryService categoryService,
			UserService userService,
			ChatService chatService,
			FavoriteService favoriteService,
			ReviewService reviewService) {

		this.itemService = itemService;
		this.categoryService = categoryService;
		this.userService = userService;
		this.chatService = chatService;
		this.favoriteService = favoriteService;
		this.reviewService = reviewService;
	}

	// =====================================================
	// 共通 ModelAttribute
	// enum を全画面で使えるようにする
	// =====================================================

	@ModelAttribute("listingTypes")
	public ListingType[] listingTypes() {
		return ListingType.values();
	}

	@ModelAttribute("rarities")
	public Rarity[] rarities() {
		return Rarity.values();
	}

	@ModelAttribute("regulations")
	public Regulation[] regulations() {
		return Regulation.values();
	}

	@ModelAttribute("conditions")
	public CardCondition[] conditions() {
		return CardCondition.values();
	}

	// =====================================================
	// 商品一覧（通常）
	// =====================================================
	@GetMapping
	public String listItems(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			Model model) {

		// 商品検索
		Page<Item> items = itemService.searchItems(keyword, categoryId, page, size);

		// カテゴリ一覧
		List<Category> categories = categoryService.getAllCategories();

		model.addAttribute("items", items);
		model.addAttribute("categories", categories);

		return "item_list";
	}

	// =====================================================
	// 商品詳細
	// =====================================================
	@GetMapping("/{id}")
	public String showItemDetail(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		Item item = itemService.getItemById(id)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		model.addAttribute("item", item);
		model.addAttribute("chats", chatService.getChatMessagesByItem(id));

		// 出品者の平均評価
		reviewService.getAverageRatingForSeller(item.getSeller())
				.ifPresent(avg -> model.addAttribute("sellerAverageRating",
						String.format("%.1f", avg)));

		boolean isOwner = false;
		boolean isFavorited = false;

		if (userDetails != null) {
			User user = userService.getUserByEmail(userDetails.getUsername())
					.orElseThrow();

			isOwner = item.getSeller().getId().equals(user.getId());
			isFavorited = favoriteService.isFavorited(user, id);
		}

		model.addAttribute("isOwner", isOwner);
		model.addAttribute("isFavorited", isFavorited);

		return "item_detail";
	}

	// =====================================================
	// 商品出品フォーム
	// =====================================================
	@GetMapping("/new")
	public String showAddItemForm(Model model) {

		Item item = new Item();

		// ★ CardInfo を必ず初期化（null防止）
		CardInfo cardInfo = new CardInfo();
		cardInfo.setItem(item);
		item.setCardInfo(cardInfo);

		model.addAttribute("item", item);
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("shippingDurations", ShippingDuration.values());
		model.addAttribute("shippingFeeBurdens", ShippingFeeBurden.values());
		model.addAttribute("shippingRegions", ShippingRegion.values());
		model.addAttribute("shippingMethods", ShippingMethod.values());

		return "item_form";
	}

	// =====================================================
	// 商品登録
	// =====================================================
	@PostMapping
	public String addItem(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute Item item,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			RedirectAttributes redirectAttributes) {

		try {
			// 出品者設定
			User seller = userService.getUserByEmail(userDetails.getUsername())
					.orElseThrow();
			item.setSeller(seller);

			// カテゴリ設定
			Category category = categoryService.getCategoryById(categoryId)
					.orElseThrow();
			item.setCategory(category);

			item.setStatus("出品中");

			// ★ CardInfo と Item の双方向関連を保証
			if (item.getCardInfo() != null) {
				item.getCardInfo().setItem(item);
			}

			itemService.saveItem(item, imageFile);

			redirectAttributes.addFlashAttribute(
					"successMessage", "商品を出品しました！");

			return "redirect:/items";

		} catch (IOException e) {

			redirectAttributes.addFlashAttribute(
					"errorMessage", "画像アップロードに失敗しました");

			return "redirect:/items/new";
		}
	}

	// =====================================================
	// ポケモンカード検索（詳細検索）
	// =====================================================
	@GetMapping("/search")
	public String searchPokemonCards(
			@RequestParam(required = false) String cardName,
			@RequestParam(required = false) Rarity rarity,
			@RequestParam(required = false) Regulation regulation,
			@RequestParam(required = false) String packName,
			@RequestParam(required = false) CardCondition condition,
			@RequestParam(required = false) ListingType listingType,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(defaultValue = "new") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size,
			Model model) {

		// 並び替え設定
		Sort sortOrder = switch (sort) {
		case "priceAsc" -> Sort.by("price").ascending();
		case "priceDesc" -> Sort.by("price").descending();
		default -> Sort.by("createdAt").descending();
		};

		Pageable pageable = PageRequest.of(page, size, sortOrder);

		// 検索実行
		Page<Item> items = itemService.searchPokemonCards(
				cardName,
				rarity,
				regulation,
				packName,
				condition,
				listingType,
				minPrice,
				maxPrice,
				pageable);

		// View に渡す
		model.addAttribute("items", items);
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("sort", sort);

		// ★ 検索条件保持（画面再表示用）
		model.addAttribute("cardName", cardName);
		model.addAttribute("rarity", rarity);
		model.addAttribute("regulation", regulation);
		model.addAttribute("packName", packName);
		model.addAttribute("condition", condition);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);

		return "item_list";
	}
}
