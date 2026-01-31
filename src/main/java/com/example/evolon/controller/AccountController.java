package com.example.evolon.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.evolon.service.UserService;

@Controller
@RequestMapping("/account")
public class AccountController {

	private final UserService userService;

	public AccountController(UserService userService) {
		this.userService = userService;
	}

	/* =====================
	 * 段階①：注意ページ
	 * ===================== */
	@GetMapping("/delete/confirm")
	public String confirmDelete(
			@AuthenticationPrincipal UserDetails principal,
			Model model) {

		// 表示用（テンプレ側で th:text="${email}" などが使える）
		if (principal != null) {
			model.addAttribute("email", principal.getUsername());
		}

		return "account_delete_confirm";
	}

	/* =====================
	 * 段階②：最終確認ページ（DELETE 入力）
	 * ===================== */
	@GetMapping("/delete")
	public String deletePage(
			@AuthenticationPrincipal UserDetails principal,
			Model model) {

		if (principal != null) {
			model.addAttribute("email", principal.getUsername());
		}

		return "account_delete";
	}

	/* =====================
	 * 段階②：退会実行
	 * ===================== */
	@PostMapping("/delete")
	public String deleteAccount(
			@AuthenticationPrincipal UserDetails principal,
			@RequestParam(value = "confirmWord", required = false) String confirmWord,
			HttpServletRequest request,
			RedirectAttributes ra) {

		// principal が null は基本起きない想定だけど保険
		if (principal == null) {
			return "redirect:/login";
		}

		// DELETE 入力チェック（null/空も弾く）
		if (confirmWord == null || !"DELETE".equals(confirmWord.trim())) {
			ra.addFlashAttribute("errorMessage", "DELETE と正確に入力してください");
			return "redirect:/account/delete";
		}

		// 論理削除
		userService.deactivateAccountByEmail(principal.getUsername());

		// ログアウト
		try {
			request.logout();
		} catch (Exception ignored) {
		}

		return "redirect:/login?deleted";
	}
}
