package com.example.evolon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class EvolonApplication {

	public static void main(String[] args) {

		System.out.println(
				new BCryptPasswordEncoder().encode("admin123"));

		SpringApplication.run(EvolonApplication.class, args);
	}

}
