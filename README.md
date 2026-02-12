# Evolon  
<img width="1920" height="1080" alt="Evolon" src="https://github.com/user-attachments/assets/bab395d2-9cec-455c-8e5b-941c534d1c38" />

## Overview

Evolon は、OCRとポケカ特化設計により、トレーディングカード出品の手間を大幅に削減するフリマWebアプリです。

OCRによるカード情報の自動入力と、ポケモンカード専用のデータ設計を組み合わせることで、  
従来の汎用フリマでは難しかったトレカ出品の手間を軽減し、スムーズな取引体験を提供します。

---

## Problems

既存のフリマサービスでポケモンカードを出品する際、以下の課題がありました。

- カード名・型番・レアリティ・状態など入力項目が多く、出品に時間がかかる  
- 手入力による情報ミスが起きやすく、正確な出品に不安がある  
- トレカ特有の属性（レギュレーションや封入パックなど）が管理しづらい  
- 出品作業の心理的ハードルが高く、売りたいカードが後回しになってしまう  

---

## Solution

上記の課題に対し、Evolon では以下の仕組みを実装しました。

- OCR（Cloud Vision API）を用いてカード画像からカード名・型番・レアリティを自動取得し、手入力を最小化  
- ポケモンカード専用の属性（状態 / レアリティ / 封入パック / レギュレーション）を設計し、迷わず選択できる入力UIを実装  
- 出品カテゴリー（単品 / セット / まとめ売り）を用意し、ユーザーの出品スタイルに柔軟に対応  
- お気に入り・マイページ機能により、出品・購入状況を一元管理できる導線を構築  

これにより、  
**「入力が面倒」「ミスが不安」という心理的ハードルを下げ、誰でも気軽に正確な出品ができる体験**を実現しました。

---

## Features

### User Features

#### 出品・商品管理
- 商品の出品 / 編集 / 削除
- 画像アップロード（Cloudinary 連携）
- ポケモンカード専用属性管理  
  （状態 / レアリティ / 封入パック / レギュレーション）
- 出品カテゴリー対応（単品 / セット / まとめ売り）

---

#### OCRカード自動入力
- Cloud Vision API によるカード画像解析
- カード番号からカードマスタ照合
- カード名 / レアリティ / パック / レギュレーション自動補完
- OCR失敗・未登録時のフォールバック設計

---

#### 購入・取引
- Stripe 決済による購入処理
- 購入履歴管理
- 取引ステータス管理（取引中 / 完了）
- 購入後レビュー機能（GOOD / BAD + コメント）

---

#### チャット機能
- 商品ごとの購入者・出品者チャット
- リアルタイムメッセージ保存
- 出品者判定ロジック実装

---

#### お気に入り / マイページ
- 商品お気に入り登録 / 解除
- 出品一覧 / 購入履歴表示
- 取引サマリ表示
- 評価統計表示（公開レビューのみ）
- プロフィール編集
- パスワードリセット
- 退会機能（DELETE確認付き2段階）

---

#### 認証・ユーザー管理
- Spring Security による認証
- 新規登録 / ログイン / ログアウト
- アカウント論理削除
- ロール管理（USER）

---

### Admin Features  

#### 管理者機能
- 管理者ダッシュボード
- 商品管理（削除 / 公開切替）
- ユーザー管理（BAN / 有効無効切替）
- 問い合わせ管理（返信 / クローズ）
- 売上統計表示（期間指定）
- CSVエクスポート対応
- ADMINロールによるアクセス制御

---

### UX / Exception Handling

- GlobalExceptionHandler による共通エラーハンドリング
- フラッシュメッセージによるUX改善

    
## Architecture

Spring Boot + Layered Architecture を採用し、
責務分離を意識した設計を行いました。

- Controller  
  リクエスト受付・レスポンス制御のみ担当

- Service  
  OCR処理・購入処理・権限制御などビジネスロジックを集約

- Repository  
  JPAを用いた永続化処理

- Domain  
  Entity / DTO / Enum / Form を分離し、
  画面層とDB層の依存を低減

## My Contribution

### 認証・ユーザー管理機能

- Spring Security を用いたログイン / ログアウト / 新規登録機能の実装
- ロール管理（USER / ADMIN）
- パスワードリセット処理
- アカウント退会（論理削除）機能
- セッション制御・アクセス制限の設計

---

### OCRカード自動入力機能

- Cloud Vision API を用いたカード画像解析処理の実装
- OCR結果からカード番号を抽出し、カードマスタと照合
- カード名 / レアリティ / パック / レギュレーションの自動補完
- OCR失敗時のフォールバック処理設計


- OCRでカード種別まで判定すると精度が不安定になるため、番号のみ取得し、実際のカード情報は自前DBと照合する設計を採用
- スマホ撮影による高解像度画像に対応するため、画像をリサイズしてOCR精度とメモリ負荷を最適化
- カード番号が左下に配置されている特性を利用し、画像の下45%のみを切り出してノイズを削減

### 管理者機能

- 管理者ダッシュボードの実装
- 商品の公開 / 非公開切り替え
- ユーザー管理（有効 / 無効切り替え）
- 問い合わせ管理
- 売上データの集計・表示

   

## アプリ画面

<table>
<tr>
<td align="center">

<b>一覧画面</b><br>
<img src="https://github.com/user-attachments/assets/1fa89e0d-9365-41d1-a696-5782183d62a9" width="600">

</td>

<td align="center">

<b>マイページ</b><br>
<img src="https://github.com/user-attachments/assets/dac09e44-3dcd-4430-a044-306649f8ebcd" width="600">

</td>
</tr>

<tr>
<td align="center">

<b>出品画面</b><br>
<img src="https://github.com/user-attachments/assets/cfeaf435-37de-42a8-87fb-dd03e8782757" width="600">

</td>

<td align="center">

<b>詳細画面</b><br>
<img src="https://github.com/user-attachments/assets/f26ead05-eba3-43af-9535-b0cc952a2305" width="600">

</td>
</tr>
</table>

## Directory Structure

```text
evolon/
├── src/
│   └── main/
│       ├── java/com/example/evolon/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── domain/
│       │   │   ├── entity/
│       │   │   ├── dto/
│       │   │   ├── enums/
│       │   │   └── form/
│       │   ├── security/
│       │   └── EvolonApplication.java
│       └── resources/
│           ├── templates/
│           │   ├── layout/
│           │   ├── fragments/
│           │   └── pages/
│           ├── static/css/
│           ├── db/migration/
│           └── application.properties
├── target/
└── pom.xml
```

---


##  Tech Stack

###  Backend
- **Java**  
  オブジェクト指向設計を意識したドメインモデル設計を実施

- **Spring Boot**  
  Webアプリケーション全体の構築、DI管理

- **Spring MVC**  
  Controller層でのリクエスト処理・画面遷移制御

- **Spring Security**  
  ログイン認証・アクセス制御の実装

- **JPA / Hibernate**  
  エンティティ設計・Repository層でのデータ永続化

- **Flyway**  
  DBスキーマのバージョン管理・マイグレーション管理

---

###  Frontend
- **Thymeleaf**  
  サーバーサイドテンプレートエンジンとして画面表示を実装

- **HTML / CSS**  
  商品一覧・出品画面などのUI構築
---

- **Git / GitHub**
  ブランチ運用によるチーム開発  
  Pull Request を用いたコードレビュー

## Learned

- OCRでカード種別まで直接判定させると精度が不安定になる課題に対し、  
  OCRではカード番号のみを取得し、実際のカード情報は自前のマスタDBと照合する設計に変更。  
  外部APIに依存しすぎない構成にすることで、安定したデータ取得を実現した。

- カード番号が画像左下に配置されている点に着目し、  
  画像の下45%のみを切り出して Cloud Vision API に渡すことで、  
  背景やイラスト部分のノイズを除去し、OCR認識精度を大幅に改善した。

- OCR結果の揺れや未検出ケースに備え、  
  手動入力へフォールバックできる設計を行い、  
  失敗時でもユーザー体験を崩さない実装の重要性を学んだ。

- Git / GitHub を用いたチーム開発を通して、  
  ブランチ運用・Pull Request ベースの開発フロー、コンフリクト対応など  
  実践的なGit管理を習得した。

- チーム開発における役割分担と責任の重要性を理解し、  
  他メンバーのコードを読み解きながら機能連携を進めることで、  
  既存コードを前提とした開発スキルが向上した。

