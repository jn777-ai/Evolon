package com.example.evolon.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.evolon.domain.enums.Rarity;
import com.example.evolon.domain.enums.Regulation;
import com.example.evolon.entity.CardMaster;
import com.example.evolon.repository.CardMasterRepository;

@Configuration
public class InitCardMasterRunner {

	@Bean
	CommandLineRunner initCardMasters(CardMasterRepository cardMasters) {
		return args -> {

			// すでに card_master にデータがあれば何もしない
			if (cardMasters.count() > 0) {
				return;
			}

			cardMasters
					.save(new CardMaster(null, "sv8a", "212/187", "ニンフィアex", Rarity.SAR, "テラスタルフェスex", Regulation.H));

			System.out.println("✅ 初期カードマスタを登録しました");
		};
	}
}
