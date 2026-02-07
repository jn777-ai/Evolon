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

	public ParsedCardNumber extractCardNumberOnly(MultipartFile imageFile) throws IOException {
		BufferedImage original = ImageIO.read(imageFile.getInputStream());
		if (original == null) {
			throw new IllegalArgumentException("画像の読み込みに失敗しました");
		}

		// 1. 高解像度写真対策（1600px以上の場合はリサイズしてメモリ負荷を軽減）
		BufferedImage workingImage = original;
		if (original.getWidth() > 1600) {
			int targetW = 1200;
			int targetH = (int) (original.getHeight() * (1200.0 / original.getWidth()));
			BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
			Graphics g = resized.getGraphics();
			g.drawImage(original, 0, 0, targetW, targetH, null);
			g.dispose();
			workingImage = resized;
		}

		// 2. 読み取り範囲の切り出し（写真の余白を考慮し下部45%を抽出）
		int w = workingImage.getWidth();
		int h = workingImage.getHeight();
		int cropY = (int) (h * 0.55);
		int cropHeight = h - cropY;
		BufferedImage cropped = workingImage.getSubimage(0, cropY, w, cropHeight);

		// 3. OCR用前処理（グレースケール化）
		BufferedImage processed = new BufferedImage(w, cropHeight, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g2 = processed.getGraphics();
		g2.drawImage(cropped, 0, 0, null);
		g2.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(processed, "png", baos);
		ByteString imgBytes = ByteString.copyFrom(baos.toByteArray());

		// 4. Vision APIリクエスト（文書読み取りモード）
		Image image = Image.newBuilder().setContent(imgBytes).build();
		Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
				.setImage(image)
				.addFeatures(feature)
				.build();

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
			AnnotateImageResponse res = response.getResponses(0);

			if (res.hasError())
				throw new RuntimeException("Vision API Error: " + res.getError().getMessage());
			if (res.getTextAnnotationsList().isEmpty())
				return ParsedCardNumber.invalid();

			String ocrText = res.getTextAnnotations(0).getDescription();

			// 5. テキストの正規化（空白除去と特定パターンの整形）
			String formattedText = ocrText.toLowerCase().replaceAll("\\s+", "");
			formattedText = formattedText.replaceAll("(\\d+)/(\\d+)", " $1 / $2 ");

			return parseCardNumber(formattedText);
		}
	}

	private ParsedCardNumber parseCardNumber(String text) {
		if (text == null || text.isBlank())
			return ParsedCardNumber.invalid();

		String cleaned = text.replaceAll("\\s+", " ");

		// セットコード抽出
		Pattern setCodePattern = Pattern.compile("(sv|m|v)[a-z0-9]{1,5}", Pattern.CASE_INSENSITIVE);
		Matcher setCodeMatcher = setCodePattern.matcher(cleaned);

		String setCode = null;
		if (setCodeMatcher.find()) {
			setCode = setCodeMatcher.group().toLowerCase();
			if (setCode.startsWith("v"))
				setCode = "s" + setCode;
			setCode = setCode.replace("svba", "sv8a");
		}

		// カード番号抽出
		Pattern numberPattern = Pattern.compile("(\\d{1,3})\\s*/\\s*(\\d{1,3})");
		Matcher numberMatcher = numberPattern.matcher(cleaned);

		if (setCode != null && numberMatcher.find()) {
			String cardNumber = numberMatcher.group(1) + "/" + numberMatcher.group(2);
			log.info("🎯 抽出成功: setCode={}, cardNumber={}", setCode, cardNumber);
			return new ParsedCardNumber(setCode, cardNumber);
		}

		return ParsedCardNumber.invalid();
	}
}