package com.example.evolon.service;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.evolon.dto.ParsedCardNumber;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OcrService {

	/**
	 * カード番号専用OCR（下部領域のみ）
	 * 例: sv8a 212/187 → setCode=sv8a, cardNumber=212/187
	 */
	public ParsedCardNumber extractCardNumberOnly(MultipartFile imageFile) throws IOException {

		BufferedImage original = ImageIO.read(imageFile.getInputStream());
		if (original == null) {
			throw new IllegalArgumentException("画像の読み込みに失敗しました");
		}

		// ① 下部領域を切り出し
		BufferedImage cropped = cropBottomArea(original);

		// ② グレースケール化
		BufferedImage gray = new BufferedImage(
				cropped.getWidth(),
				cropped.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		Graphics g = gray.getGraphics();
		g.drawImage(cropped, 0, 0, null);
		g.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(gray, "png", baos);
		ByteString imgBytes = ByteString.copyFrom(baos.toByteArray());

		// ③ Vision API（TEXT_DETECTION）
		Image image = Image.newBuilder()
				.setContent(imgBytes)
				.build();

		Feature feature = Feature.newBuilder()
				.setType(Feature.Type.TEXT_DETECTION)
				.build();

		AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
				.setImage(image)
				.addFeatures(feature)
				.build();

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {

			BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
			AnnotateImageResponse res = response.getResponses(0);

			if (res.hasError()) {
				throw new RuntimeException("Vision API Error: " + res.getError().getMessage());
			}

			if (res.getTextAnnotationsList().isEmpty()) {
				log.warn("カード番号OCR結果なし");
				return ParsedCardNumber.invalid();
			}

			String ocrText = res.getTextAnnotations(0).getDescription();
			log.info("===== CARD NUMBER OCR RAW =====\n{}", ocrText);

			ParsedCardNumber parsed = parseCardNumber(ocrText);
			log.info("===== CARD NUMBER OCR PARSED ===== {}", parsed);

			return parsed;
		}
	}

	/* =========================
	 * 下部約28%を切り出す（カード番号領域）
	 * ========================= */
	private BufferedImage cropBottomArea(BufferedImage original) {
		int w = original.getWidth();
		int h = original.getHeight();

		int cropY = (int) (h * 0.72);
		int cropHeight = h - cropY;

		return original.getSubimage(0, cropY, w, cropHeight);
	}

	/* =========================
	 * OCR文字列 → setCode / cardNumber 抽出（安定版）
	 * ========================= */
	private ParsedCardNumber parseCardNumber(String text) {

		if (text == null || text.isBlank()) {
			log.warn("❌ カード番号抽出失敗: 空文字");
			return ParsedCardNumber.invalid();
		}

		// ① 全体正規化（全角/空白/スラッシュなど）
		String cleaned = text
				.replaceAll("(?m)^\\s*[HIJ]\\s+", "") // 行頭 H/I/J + 空白を除去
				.replace("／", "/")
				.replace("　", " ")
				.trim();

		// ② setCode抽出（sv/m/v を許容、誤認識補正）
		Pattern setCodePattern = Pattern.compile("\\b(sv|m|v)[a-z0-9]{1,4}\\b", Pattern.CASE_INSENSITIVE);
		Matcher setCodeMatcher = setCodePattern.matcher(cleaned);

		String setCode = null;
		if (setCodeMatcher.find()) {
			setCode = setCodeMatcher.group().toLowerCase();

			// v8a → sv8a
			if (setCode.startsWith("v")) {
				setCode = "s" + setCode;
			}

			// OCRの8↔b誤認識補正（必要なら追加）
			setCode = setCode.replace("svba", "sv8a");
		}

		// ③ cardNumber抽出（空白を許容）
		Pattern numberPattern = Pattern.compile("(\\d{1,3})\\s*/\\s*(\\d{1,3})");
		Matcher numberMatcher = numberPattern.matcher(cleaned);

		if (setCode != null && numberMatcher.find()) {
			String cardNumber = numberMatcher.group(1) + "/" + numberMatcher.group(2);
			log.info("🎯 抽出成功 setCode={}, cardNumber={}", setCode, cardNumber);
			return new ParsedCardNumber(setCode, cardNumber);
		}

		log.warn("❌ カード番号抽出失敗: cleaned={}", cleaned);
		return ParsedCardNumber.invalid();
	}
}
