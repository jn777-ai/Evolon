package com.example.evolon.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.evolon.entity.User;
import com.example.evolon.form.ProfileEditForm;
import com.example.evolon.service.AppOrderService;
import com.example.evolon.service.FavoriteService;
import com.example.evolon.service.ItemService;
import com.example.evolon.service.ReviewService;
import com.example.evolon.service.ReviewStatsService;
import com.example.evolon.service.UserService;

@Controller
@RequestMapping("/my-page")
public class UserController {

	private final UserService userService;
	private final ItemService itemService;
	private final AppOrderService appOrderService;
	private final FavoriteService favoriteService;

	private final ReviewService reviewService;
	private final ReviewStatsService reviewStatsService;

	public UserController(
			UserService userService,
			ItemService itemService,
			AppOrderService appOrderService,
			FavoriteService favoriteService,
			ReviewService reviewService,
			ReviewStatsService reviewStatsService) {

		this.userService = userService;
		this.itemService = itemService;
		this.appOrderService = appOrderService;
		this.favoriteService = favoriteService;
		this.reviewService = reviewService;
		this.reviewStatsService = reviewStatsService;
	}

	/* =====================
	 * マイページ TOP
	 * ===================== */
	@GetMapping
	public String myPage(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);
		model.addAttribute("user", user);

		// 最近の購入履歴
		model.addAttribute("orders",
				appOrderService.findPurchasedOrdersByBuyer(user));

		// 出品中プレビュー用（一覧表示）
		model.addAttribute("items",
				itemService.getItemsBySeller(user));

		// 公開対象だけの評価サマリ
		model.addAttribute("reviewStats",
				reviewStatsService.getStats(user));

		//  購入者サマリ
		model.addAttribute("buyerTradingCount",
				appOrderService.countTradingByBuyer(user));

		model.addAttribute("buyerCompletedCount",
				appOrderService.countCompletedByBuyer(user));

		// =========================
		//  サマリ数字
		// =========================
		model.addAttribute("sellingCount", itemService.countSellingBySeller(user)); // 出品中
		model.addAttribute("tradingCount", itemService.countTradingBySeller(user)); // 取引中（なければ0でOK）
		model.addAttribute("soldCount", itemService.countSoldBySeller(user)); // 取引完了
		model.addAttribute("favoriteCount", favoriteService.countFavoritesByUser(user)); // お気に入り

		return "pages/mypage/my_page";
	}

	/* =====================
	 * プロフィール編集（表示）
	 * ===================== */
	@GetMapping("/profile/edit")
	public String editProfile(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "returnTo", required = false) String returnTo,
			Model model) {

		User user = getLoginUser(userDetails);

		ProfileEditForm form = new ProfileEditForm();
		form.setNickname(user.getNickname());
		form.setProfileImageUrl(user.getProfileImageUrl());
		form.setLastName(user.getLastName());
		form.setFirstName(user.getFirstName());
		form.setPostalCode(user.getPostalCode());
		form.setAddress(user.getAddress());
		form.setBio(user.getBio());

		model.addAttribute("profileEditForm", form);
		model.addAttribute("returnTo", returnTo);

		return "pages/mypage/profile_edit";
	}

	/* =====================
	 * プロフィール編集（保存）
	 * ===================== */
	@PostMapping("/profile/edit")
	public String updateProfile(
			@AuthenticationPrincipal UserDetails userDetails,
			ProfileEditForm profileEditForm,
			@RequestParam(value = "returnTo", required = false) String returnTo,
			RedirectAttributes ra) {

		User user = getLoginUser(userDetails);

		if (isBlank(profileEditForm.getNickname())) {
			profileEditForm.setNickname(user.getName());
		}

		userService.updateProfile(user, profileEditForm);
		ra.addFlashAttribute("successMessage", "プロフィールを更新しました");

		if (!isBlank(returnTo)) {
			return "redirect:" + returnTo;
		}
		return "redirect:/my-page";
	}

	/* =====================
	 * 購入履歴
	 * ===================== */
	@GetMapping("/orders")
	public String myOrders(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);

		model.addAttribute(
				"myOrders",
				appOrderService.findPurchasedOrdersByBuyer(user));

		return "pages/mypage/buyer_app_orders";
	}

	/* =====================
	 * 販売履歴（出品者）
	 * ===================== */
	@GetMapping("/sales")
	public String mySales(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);

		model.addAttribute(
				"mySales",
				appOrderService.findOrdersBySeller(user));

		return "pages/mypage/seller_app_orders";
	}

	/* =====================
	 * 出品中商品
	 * ===================== */
	@GetMapping("/selling")
	public String mySellingItems(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);

		model.addAttribute(
				"items",
				itemService.getItemsBySeller(user));

		return "pages/mypage/seller_items";
	}

	/* =====================
	 * お気に入り
	 * ===================== */
	@GetMapping("/favorites")
	public String myFavorites(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);

		model.addAttribute(
				"favoriteItems",
				favoriteService.getFavoriteItemsByUser(user));

		return "pages/mypage/my_favorites";
	}

	/* =====================
	 * 評価一覧（公開対象だけ）
	 * ===================== */
	@GetMapping("/reviews")
	public String myReviews(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);

		model.addAttribute("user", user);
		model.addAttribute("reviewStats", reviewStatsService.getStats(user));
		model.addAttribute("reviews", reviewService.findVisibleReviewsForUser(user));

		return "pages/mypage/my_reviews";
	}

	/* =====================
	 * アカウント管理（ハブ）
	 * ===================== */
	@GetMapping("/account")
	public String accountHub(
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User user = getLoginUser(userDetails);
		model.addAttribute("user", user);

		return "pages/mypage/account_hub";
	}

	/* =====================
	 * 共通
	 * ===================== */
	private User getLoginUser(UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("UserDetails is null (not logged in)");
		}
		return userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

}
