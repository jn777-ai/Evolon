package com.example.evolon.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * Cloudinary 画像アップロード用サービス
 * 
 * ・同時アップロードを1件に制限
 * ・短時間の連続アップロードを抑制（レート制限対策）
 */
@Service
public class CloudinaryService {

	// Cloudinary クライアント
	private final Cloudinary cloudinary;

	// Cloudinary 有効/無効フラグ
	private final boolean enabled;

	// アップロード間隔（ミリ秒）
	// Free プラン対策：500ms 推奨
	private static final long UPLOAD_INTERVAL_MS = 500;

	// 前回アップロード時刻
	private long lastUploadTime = 0L;

	/**
	 * Cloudinary 設定を application.properties から注入
	 */
	public CloudinaryService(
			@Value("${cloudinary.cloud_name:}") String cloudName,
			@Value("${cloudinary.api_key:}") String apiKey,
			@Value("${cloudinary.api_secret:}") String apiSecret,
			@Value("${cloudinary.enabled:false}") boolean enabled) {

		this.enabled = enabled;

		if (enabled) {
			this.cloudinary = new Cloudinary(ObjectUtils.asMap(
					"cloud_name", cloudName,
					"api_key", apiKey,
					"api_secret", apiSecret));
		} else {
			this.cloudinary = null;
		}
	}

	/**
	 * 画像を Cloudinary にアップロードし、公開 URL を返す
	 * 
	 * ・synchronized により同時アップロードを防止
	 * ・アップロード間隔を制御し、レート制限を回避
	 */
	public synchronized String uploadFile(MultipartFile file) {

		// 無効時・空ファイルはアップロードしない
		if (!enabled || file == null || file.isEmpty()) {
			return null;
		}

		try {
			// 前回アップロードから一定時間経過するまで待機
			long now = System.currentTimeMillis();
			long waitTime = UPLOAD_INTERVAL_MS - (now - lastUploadTime);
			if (waitTime > 0) {
				Thread.sleep(waitTime);
			}

			// Cloudinary へアップロード
			Map uploadResult = cloudinary.uploader().upload(
					file.getBytes(),
					ObjectUtils.emptyMap());

			// アップロード完了時刻を更新
			lastUploadTime = System.currentTimeMillis();

			// 公開 URL を返却
			return uploadResult.get("secure_url").toString();

		} catch (Exception e) {

			// Cloudinary のレート制限エラー対策
			if (e.getMessage() != null && e.getMessage().contains("Too many")) {
				throw new IllegalStateException(
						"画像アップロードが集中しています。少し時間を置いて再度お試しください。", e);
			}

			throw new IllegalStateException("画像アップロードに失敗しました", e);
		}
	}

	/**
	 * Cloudinary 上の画像を削除
	 * 
	 * @param imageUrl Cloudinary の画像URL
	 */
	public void deleteFile(String imageUrl) {

		if (!enabled || imageUrl == null)
			return;

		try {
			// URL から public_id を抽出
			String[] parts = imageUrl.split("/");
			String fileName = parts[parts.length - 1];
			String publicId = fileName.substring(0, fileName.lastIndexOf('.'));

			cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

		} catch (Exception e) {
			System.err.println("Cloudinary delete failed: " + e.getMessage());
		}
	}
}
