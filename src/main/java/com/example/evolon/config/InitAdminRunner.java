package com.example.evolon.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.evolon.entity.User;
import com.example.evolon.repository.UserRepository;

@Configuration
public class InitAdminRunner {

	@Bean
	CommandLineRunner initAdmin(UserRepository users, PasswordEncoder encoder) {
		return args -> {

			String email = "admin@evolon.com";

			// すでに存在したら何もしない
			if (users.findByEmailIgnoreCase(email).isPresent()) {
				return;
			}

			User admin = new User();
			admin.setEmail(email);
			admin.setName("Admin");
			admin.setPassword(encoder.encode("admin123")); // ← 平文OK
			admin.setRole("ADMIN");
			admin.setEnabled(true);
			//			admin.setBanned(false);

			users.save(admin);

			System.out.println("✅ 初期ADMINユーザーを作成しました");
			System.out.println("email: admin@evolon.com");
			System.out.println("password: admin123");
		};
	}
}
