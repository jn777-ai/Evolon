#  Evolon  
### チーム開発 ECサイトアプリケーション

---

## Overview

Evolon は、**Spring Boot を用いてチームで開発したECサイト型Webアプリケーション**です。

実務を想定し、レイヤードアーキテクチャ（Controller / Service / Repository）を採用。  
Gitを用いたチーム開発を通して、設計・実装・レビュー・バグ修正まで一連の工程を経験しました。

---

##  Purpose

実務を想定したECサイト開発を通じて、
- レイヤードアーキテクチャ設計の理解
- Spring Securityによる認証実装
- チームでのGit運用経験
を目的として開発しました。

---

##  My Role（担当領域）

###  商品出品機能（メイン担当）

- 商品登録画面の設計・実装
- Controller / Service / Repository の処理実装
- 入力バリデーション
- データベース保存処理
- エラー時の画面制御

> MVC構造を意識し、責務分離を徹底しました。

---

###  バグ修正対応

- 不具合の原因調査
- ログ確認・デバッグ
- NullPointerException の修正
- 修正後の動作確認

> 他メンバーのコードを読み解き、原因分析から修正まで対応しました。

---

##  Features

- 👤 ユーザー登録 / ログイン
- 📦 商品一覧表示
- 🔍 商品詳細表示
- 🛍 商品出品機能（担当）
- 🛒 カート機能
- 💳 注文処理
- 🔐 管理者機能
- 🗃 FlywayによるDBマイグレーション

---
## 🏗 Architecture
- **Controller** : リクエスト制御
- **Service** : ビジネスロジック
- **Repository** : DBアクセス
- **Domain** : Entity / DTO / Enum / Form

責務を明確に分離し、保守性と拡張性を意識した設計を行いました。
---

## 📂 Directory Structure

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

###  Development Environment
- **Eclipse**  
  開発IDE

---

###  学んだこと

- チーム開発における役割分担と責任の重要性
- 他メンバーのコードを読み、理解する力の向上
- 不具合調査におけるログ分析とデバッグの重要性
- 設計を意識することで保守性・拡張性が大きく向上すること
- レイヤードアーキテクチャによる責務分離の実践的理解


- **Git / GitHub**  
  ブランチ運用によるチーム開発  
  Pull Request を用いたコードレビュー

  ---

