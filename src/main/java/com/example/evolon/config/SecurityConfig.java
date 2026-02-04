package com.example.evolon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// =========================
				// 認可ルール
				// =========================
				.authorizeHttpRequests(authorize -> authorize
						// 認証不要（公開）
						.requestMatchers(
								"/login",
								"/register",
								"/register/**",
								"/password/**",

								// 静的ファイル
								"/css/**",
								"/js/**",
								"/images/**",

								// 商品関連を公開（要件に合わせて）
								"/items/**",

								// OCR API（画像POSTするので permitAll 推奨）
								"/api/ocr",

								// Stripe webhook
								"/orders/stripe-webhook")
						.permitAll()

						// 管理者だけ
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// それ以外はログイン必須
						.anyRequest().authenticated())

				// =========================
				// ログイン
				// =========================
				.formLogin(form -> form
						.loginPage("/login")
						.successHandler(new LoginSuccessHandler())
						.permitAll())

				// =========================
				// ログアウト
				// =========================
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())

				// =========================
				// CSRF
				// =========================
				.csrf(csrf -> csrf
						// webhook は外部から POST されるので必須
						.ignoringRequestMatchers("/orders/stripe-webhook")
						// OCR は fetch で POST するので外す（403回避）
						.ignoringRequestMatchers("/api/ocr"));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
