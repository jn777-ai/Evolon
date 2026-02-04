package com.example.evolon.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.evolon.dto.ParsedCardNumber;
import com.example.evolon.service.CardMasterService;
import com.example.evolon.service.OcrService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

	private final OcrService ocrService;
	private final CardMasterService cardMasterService;

	@PostMapping
	public ResponseEntity<Map<String, Object>> ocr(
			@RequestParam(value = "image", required = false) MultipartFile image) {

		Map<String, Object> body = new HashMap<>();
		body.put("parsed", null);
		body.put("card", null);

		// ① 画像が無い / 空 → 400にせず 200で返す（フロントのres.ok落ち防止）
		if (image == null || image.isEmpty()) {
			body.put("message", "画像が選択されていません");
			return ResponseEntity.ok(body);
		}

		try {
			// ② OCRして setCode/cardNumber を抽出
			ParsedCardNumber parsed = ocrService.extractCardNumberOnly(image);

			if (parsed == null || !parsed.isValid()) {
				body.put("message", "カード番号を検出できませんでした");
				return ResponseEntity.ok(body);
			}

			// ③ デバッグ用に返す（Networkで parsed を確認できる）
			body.put("parsed", parsed);
			log.info("[OCR parsed] {}", parsed);

			// ④ DB検索（存在しないなら未登録）
			return cardMasterService.findByParsedNumber(parsed)
					.map(card -> {
						body.put("card", card);
						body.put("message", "OK");
						return ResponseEntity.ok(body);
					})
					.orElseGet(() -> {
						body.put("message", "カードマスタ未登録");
						return ResponseEntity.ok(body);
					});

		} catch (Exception e) {
			// ⑤ 例外も 200で返す（UIは「サーバエラー」表示だけで落ちない）
			log.error("OCR failed", e);
			body.put("message", "OCRサーバーエラー: " + e.getClass().getSimpleName());
			return ResponseEntity.ok(body);
		}
	}
}
