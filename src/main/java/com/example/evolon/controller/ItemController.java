package com.example.evolon.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.evolon.domain.enums.CardCondition;
import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;
import com.example.evolon.domain.enums.ShippingDuration;
import com.example.evolon.domain.enums.ShippingFeeBurden;
import com.example.evolon.domain.enums.ShippingMethod;
import com.example.evolon.domain.enums.ShippingRegion;
import com.example.evolon.dto.CardAutoFillResponse;
import com.example.evolon.dto.ParsedCardNumber;
import com.example.evolon.entity.Category;
import com.example.evolon.entity.Item;
import com.example.evolon.entity.ItemStatus;
import com.example.evolon.entity.User;
import com.example.evolon.service.CardMasterService;
import com.example.evolon.service.CardNumberParserService;
import com.example.evolon.service.CategoryService;
import com.example.evolon.service.ChatService;
import com.example.evolon.service.FavoriteService;
import com.example.evolon.service.ItemService;
import com.example.evolon.service.RegulationService;
import com.example.evolon.service.ReviewService;
import com.example.evolon.service.UserService;

@Controller
@RequestMapping("/items")
public class ItemController {

	private final ItemService itemService;
	private final CategoryService categoryService;
	private final UserService userService;
	private final ChatService chatService;
	private final FavoriteService favoriteService;
	private final ReviewService reviewService;

	private final CardNumberParserService cardNumberParserService;
	private final CardMasterService cardMasterService;
	private final RegulationService regulationService;

	public ItemController(
			ItemService itemService,
			CategoryService categoryService,
			UserService userService,
			ChatService chatService,
			FavoriteService favoriteService,
			ReviewService reviewService,
			CardNumberParserService cardNumberParserService,
			CardMasterService cardMasterService,
			RegulationService regulationService) {

		this.itemService = itemService;
		this.categoryService = categoryService;
		this.userService = userService;
		this.chatService = chatService;
		this.favoriteService = favoriteService;
		this.reviewService = reviewService;
		this.cardNumberParserService = cardNumberParserService;
		this.cardMasterService = cardMasterService;
		this.regulationService = regulationService;
	}

	@GetMapping
	public String listItems(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			Model model) {

		ItemStatus statusEnum = parseEnumSafely(status, ItemStatus.class);

		Page<Item> items = itemService.searchItems(
				keyword,
				categoryId,
				statusEnum,
				page,
				size);

		model.addAttribute("items", items);
		model.addAttribute("categories", categoryService.getAllCategories());

		model.addAttribute("rarities", Rarity.values());
		model.addAttribute("regulations", Regulation.values());
		model.addAttribute("conditions", CardCondition.values());

		return "pages/items/item_list";
	}

	@GetMapping("/search")
	public String search(
			@RequestParam(required = false) String cardName,
			@RequestParam(required = false) String rarity,
			@RequestParam(required = false) String regulation,
			@RequestParam(required = false) String condition,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String packName,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(defaultValue = "new") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			Model model) {

		Rarity rarityEnum = parseEnumSafely(rarity, Rarity.class);
		Regulation regEnum = parseEnumSafely(regulation, Regulation.class);
		CardCondition condEnum = parseEnumSafely(condition, CardCondition.class);
		ItemStatus statusEnum = parseEnumSafely(status, ItemStatus.class);

		Page<Item> items = itemService.searchByCardFilters(
				cardName, rarityEnum, regEnum, condEnum,
				packName, minPrice, maxPrice, sort,
				statusEnum,
				page, size);

		model.addAttribute("items", items);
		model.addAttribute("rarities", Rarity.values());
		model.addAttribute("regulations", Regulation.values());
		model.addAttribute("conditions", CardCondition.values());
		model.addAttribute("categories", categoryService.getAllCategories());

		return "pages/items/item_list";
	}

	@GetMapping("/{id}")
	public String showItemDetail(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		Optional<Item> itemOpt = itemService.getItemById(id);
		if (itemOpt.isEmpty())
			return "redirect:/items";

		Item item = itemOpt.get();
		model.addAttribute("item", item);

		model.addAttribute("chats", chatService.getChatMessagesByItem(id));

		if (item.getSeller() != null) {
			model.addAttribute("sellerGoodCount", reviewService.countGoodForSeller(item.getSeller()));
			model.addAttribute("sellerBadCount", reviewService.countBadForSeller(item.getSeller()));
		}

		boolean isOwner = false;
		boolean isFavorited = false;

		if (userDetails != null) {
			User currentUser = userService.getUserByEmail(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			isOwner = item.getSeller() != null
					&& item.getSeller().getId().equals(currentUser.getId());

			isFavorited = favoriteService.isFavorited(currentUser, id);
		}

		model.addAttribute("isOwner", isOwner);
		model.addAttribute("isFavorited", isFavorited);

		return "pages/items/item_detail";
	}

	@GetMapping("/new")
	public String showAddItemForm(Model model) {

		model.addAttribute("item", new Item());
		model.addAttribute("categories", categoryService.getAllCategories());

		model.addAttribute("shippingDurations", ShippingDuration.values());
		model.addAttribute("shippingFeeBurdens", ShippingFeeBurden.values());
		model.addAttribute("shippingRegions", ShippingRegion.values());
		model.addAttribute("shippingMethods", ShippingMethod.values());

		model.addAttribute("rarities", Rarity.values());
		model.addAttribute("conditions", CardCondition.values());
		model.addAttribute("regulations", Regulation.values());

		// ✅ cardCategoryId を渡す（CategoryServiceに getCategoryByName が必要）
		Long cardCategoryId = categoryService.getCategoryByName("カード")
				.map(Category::getId)
				.orElse(null);
		model.addAttribute("cardCategoryId", cardCategoryId);

		return "pages/items/item_form";
	}

	@GetMapping("/auto-fill")
	@ResponseBody
	public CardAutoFillResponse autoFill(@RequestParam String text) {

		ParsedCardNumber parsed = cardNumberParserService.parse(text);

		return cardMasterService.findByParsedNumber(parsed)
				.map(cm -> {
					Regulation reg = regulationService.resolve(cm.getPrintedRegulation());
					return new CardAutoFillResponse(
							cm.getCardName(),
							cm.getRarity(),
							cm.getPackName(),
							reg);
				})
				.orElse(null);
	}

	@PostMapping
	public String addItem(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute Item item,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam("shippingDuration") ShippingDuration shippingDuration,
			@RequestParam("shippingFeeBurden") ShippingFeeBurden shippingFeeBurden,
			@RequestParam("shippingRegion") ShippingRegion shippingRegion,
			@RequestParam("shippingMethod") ShippingMethod shippingMethod,
			@RequestParam(value = "images", required = false) MultipartFile[] imageFiles,
			RedirectAttributes redirectAttributes) {

		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインしてください。");
			return "redirect:/login";
		}

		User seller = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Seller not found"));

		item.setSeller(seller);
		item.setShippingDuration(shippingDuration);
		item.setShippingFeeBurden(shippingFeeBurden);
		item.setShippingRegion(shippingRegion);
		item.setShippingMethod(shippingMethod);

		Category category = categoryService.getCategoryById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));
		item.setCategory(category);

		if (category.getName() == null || !"カード".equals(category.getName())) {
			item.setCardInfo(null);
		} else {
			if (imageFiles == null || imageFiles.length == 0 || imageFiles[0] == null || imageFiles[0].isEmpty()) {
				redirectAttributes.addFlashAttribute("errorMessage",
						"カードカテゴリでは商品画像（1枚目）が必須です（OCRに使用します）。");
				return "redirect:/items/new";
			}

			if (item.getCardInfo() == null
					|| item.getCardInfo().getCardName() == null || item.getCardInfo().getCardName().isBlank()
					|| item.getCardInfo().getPackName() == null || item.getCardInfo().getPackName().isBlank()
					|| item.getCardInfo().getRarity() == null
					|| item.getCardInfo().getCondition() == null
					|| item.getCardInfo().getRegulation() == null) {

				redirectAttributes.addFlashAttribute("errorMessage",
						"カード名・レアリティ・封入パック・状態・レギュレーションはすべて必須です。");
				return "redirect:/items/new";
			}

			item.getCardInfo().setItem(item);
		}

		try {
			itemService.saveItem(item, imageFiles);
			redirectAttributes.addFlashAttribute("successMessage", "商品を出品しました！");
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "画像のアップロードに失敗しました: " + e.getMessage());
			return "redirect:/items/new";
		}

		return "redirect:/items";
	}

	@GetMapping("/{id}/edit")
	public String showEditItemForm(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model,
			RedirectAttributes redirectAttributes) {

		Item existingItem = itemService.getItemById(id).orElse(null);
		if (existingItem == null)
			return "redirect:/items";

		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインしてください。");
			return "redirect:/login";
		}

		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (existingItem.getSeller() == null || !existingItem.getSeller().getId().equals(currentUser.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "この商品は編集できません。");
			return "redirect:/items/" + id;
		}

		if (existingItem.getStatus() == ItemStatus.SOLD) {
			redirectAttributes.addFlashAttribute("errorMessage", "取引完了のため編集できません。");
			return "redirect:/items/" + id;
		}

		model.addAttribute("item", existingItem);
		model.addAttribute("categories", categoryService.getAllCategories());

		model.addAttribute("shippingDurations", ShippingDuration.values());
		model.addAttribute("shippingFeeBurdens", ShippingFeeBurden.values());
		model.addAttribute("shippingRegions", ShippingRegion.values());
		model.addAttribute("shippingMethods", ShippingMethod.values());

		model.addAttribute("rarities", Rarity.values());
		model.addAttribute("conditions", CardCondition.values());
		model.addAttribute("regulations", Regulation.values());

		Long cardCategoryId = categoryService.getCategoryByName("カード")
				.map(Category::getId)
				.orElse(null);
		model.addAttribute("cardCategoryId", cardCategoryId);

		return "pages/items/item_form";
	}

	@PostMapping("/{id}")
	public String updateItem(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam("price") BigDecimal price,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam(value = "images", required = false) MultipartFile[] imageFiles,
			@RequestParam("shippingDuration") ShippingDuration shippingDuration,
			@RequestParam("shippingFeeBurden") ShippingFeeBurden shippingFeeBurden,
			@RequestParam("shippingRegion") ShippingRegion shippingRegion,
			@RequestParam("shippingMethod") ShippingMethod shippingMethod,
			@RequestParam(value = "cardInfo.cardName", required = false) String cardName,
			@RequestParam(value = "cardInfo.packName", required = false) String packName,
			@RequestParam(value = "cardInfo.rarity", required = false) Rarity rarity,
			@RequestParam(value = "cardInfo.condition", required = false) CardCondition condition,
			@RequestParam(value = "cardInfo.regulation", required = false) Regulation regulation,
			RedirectAttributes redirectAttributes) {

		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインしてください。");
			return "redirect:/login";
		}

		Item existingItem = itemService.getItemById(id)
				.orElseThrow(() -> new RuntimeException("Item not found"));

		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (existingItem.getSeller() == null || !existingItem.getSeller().getId().equals(currentUser.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "この商品は編集できません。");
			return "redirect:/items";
		}

		if (existingItem.getStatus() == ItemStatus.SOLD) {
			redirectAttributes.addFlashAttribute("errorMessage", "取引完了のため編集できません。");
			return "redirect:/items/{id}";
		}

		Category category = categoryService.getCategoryById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		existingItem.setName(name);
		existingItem.setDescription(description);
		existingItem.setPrice(price);
		existingItem.setCategory(category);

		existingItem.setShippingDuration(shippingDuration);
		existingItem.setShippingFeeBurden(shippingFeeBurden);
		existingItem.setShippingRegion(shippingRegion);
		existingItem.setShippingMethod(shippingMethod);

		if (category.getName() == null || !"カード".equals(category.getName())) {
			existingItem.setCardInfo(null);
		} else {
			if (cardName == null || cardName.isBlank()
					|| packName == null || packName.isBlank()
					|| rarity == null
					|| condition == null
					|| regulation == null) {
				redirectAttributes.addFlashAttribute("errorMessage",
						"カード名・レアリティ・封入パック・状態・レギュレーションはすべて必須です。");
				return "redirect:/items/{id}/edit";
			}

			if (existingItem.getCardInfo() == null) {
				existingItem.setCardInfo(new com.example.evolon.entity.CardInfo());
			}

			existingItem.getCardInfo().setCardName(cardName);
			existingItem.getCardInfo().setPackName(packName);
			existingItem.getCardInfo().setRarity(rarity);
			existingItem.getCardInfo().setCondition(condition);
			existingItem.getCardInfo().setRegulation(regulation);
			existingItem.getCardInfo().setItem(existingItem);
		}

		try {
			itemService.saveItem(existingItem, imageFiles);
			redirectAttributes.addFlashAttribute("successMessage", "商品を更新しました！");
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "画像のアップロードに失敗しました: " + e.getMessage());
			return "redirect:/items/{id}/edit";
		}

		return "redirect:/items/{id}";
	}

	@PostMapping("/{id}/delete")
	public String deleteItem(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {

		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインしてください。");
			return "redirect:/login";
		}

		Item itemToDelete = itemService.getItemById(id)
				.orElseThrow(() -> new RuntimeException("Item not found"));

		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (itemToDelete.getSeller() == null || !itemToDelete.getSeller().getId().equals(currentUser.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "この商品は削除できません。");
			return "redirect:/items";
		}

		if (itemToDelete.getStatus() == ItemStatus.SOLD) {
			redirectAttributes.addFlashAttribute("errorMessage", "取引完了のため削除できません。");
			return "redirect:/items/" + id;
		}

		itemService.deleteItem(id);
		redirectAttributes.addFlashAttribute("successMessage", "商品を削除しました。");
		return "redirect:/items";
	}

	@PostMapping("/{id}/favorite")
	public String addFavorite(
			@PathVariable("id") Long itemId,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {

		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインしてください。");
			return "redirect:/login";
		}

		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		try {
			favoriteService.addFavorite(currentUser, itemId);
			redirectAttributes.addFlashAttribute("successMessage", "お気に入りに追加しました！");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/items/{id}";
	}

	@PostMapping("/{id}/unfavorite")
	public String removeFavorite(
			@PathVariable("id") Long itemId,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {

		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインしてください。");
			return "redirect:/login";
		}

		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		try {
			favoriteService.removeFavorite(currentUser, itemId);
			redirectAttributes.addFlashAttribute("successMessage", "お気に入りから削除しました。");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/items/{id}";
	}

	private <E extends Enum<E>> E parseEnumSafely(String value, Class<E> enumClass) {
		if (value == null || value.isBlank())
			return null;
		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
