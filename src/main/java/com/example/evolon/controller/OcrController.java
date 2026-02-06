package com.example.evolon.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.evolon.domain.enums.Regulation;
import com.example.evolon.dto.CardAutoFillResponse;
import com.example.evolon.dto.ParsedCardNumber;
import com.example.evolon.service.CardMasterService;
import com.example.evolon.service.OcrService;
import com.example.evolon.service.RegulationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

	private final OcrService ocrService;
	private final CardMasterService cardMasterService;
	private final RegulationService regulationService;

	@PostMapping
	public ResponseEntity<Map<String, Object>> ocr(
			@RequestParam(value = "image", required = false) MultipartFile image) {

		Map<String, Object> body = new HashMap<>();
		body.put("parsed", null);
		body.put("card", null);

		if (image == null || image.isEmpty()) {
			body.put("message", "画像が選択されていません");
			return ResponseEntity.ok(body);
		}

		try {
			ParsedCardNumber parsed = ocrService.extractCardNumberOnly(image);

			if (parsed == null || !parsed.isValid()) {
				body.put("message", "カード番号を検出できませんでした");
				return ResponseEntity.ok(body);
			}

			body.put("parsed", parsed);
			log.info("[OCR parsed] {}", parsed);

			return cardMasterService.findByParsedNumber(parsed)
					.map(card -> {
						Regulation reg = regulationService.resolve(card.getPrintedRegulation());

						body.put("card", new CardAutoFillResponse(
								card.getCardName(),
								card.getRarity(),
								card.getPackName(),
								reg));
						body.put("message", "OK");
						return ResponseEntity.ok(body);
					})
					.orElseGet(() -> {
						body.put("message", "カードマスタ未登録");
						return ResponseEntity.ok(body);
					});

		} catch (Exception e) {
			log.error("OCR failed", e);
			body.put("message", "OCRサーバーエラー: " + e.getClass().getSimpleName());
			return ResponseEntity.ok(body);
		}
	}
}
