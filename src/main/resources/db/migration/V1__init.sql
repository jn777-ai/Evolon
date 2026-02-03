-- V1__init.sql
-- 初期スキーマ（Flyway用 / DROPなし）
-- PK/FK は JPA(Long) と合わせて BIGINT 統一


-- ========== USERS ==========ß
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,

    -- ★ V3 を統合
    line_notify_token TEXT,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    banned BOOLEAN NOT NULL DEFAULT FALSE,
    ban_reason TEXT,
    banned_at TIMESTAMP,
    banned_by_admin_id BIGINT,
    reset_token VARCHAR(255),
    reset_token_expires_at TIMESTAMP,

    -- profile
    nickname VARCHAR(50),
    profile_image_url TEXT,
    last_name VARCHAR(50),
    first_name VARCHAR(50),
    postal_code VARCHAR(8),
    address TEXT,
    bio VARCHAR(500)
);

-- ========== CATEGORY ==========
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ========== ITEM ==========
CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,

    category_id BIGINT,

    status VARCHAR(20) NOT NULL DEFAULT 'SELLING',

    image_url TEXT,
    image_url2 TEXT,
    image_url3 TEXT,
    image_url4 TEXT,
    image_url5 TEXT,
    image_url6 TEXT,
    image_url7 TEXT,
    image_url8 TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    shipping_duration VARCHAR(50) NOT NULL,
    shipping_fee_burden VARCHAR(50) NOT NULL,
    shipping_region VARCHAR(50) NOT NULL,
    shipping_method VARCHAR(50) NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- ========== CARD INFO ==========
CREATE TABLE card_info (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE,

    card_name VARCHAR(255) NOT NULL,
    pack_name VARCHAR(255),
    rarity VARCHAR(50) NOT NULL,
    regulation VARCHAR(50) NOT NULL,
    condition VARCHAR(50) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

-- ========== ORDER ==========
CREATE TABLE app_order (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    price NUMERIC(10,2) NOT NULL,

    status VARCHAR(20) DEFAULT '購入済',
    order_status VARCHAR(30) NOT NULL DEFAULT 'PAYMENT_PENDING',

    payment_intent_id VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    cancelled_at TIMESTAMP,
    cancelled_by VARCHAR(20),
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,

    shipping_postal_code VARCHAR(20),
    shipping_address     VARCHAR(255),
    shipping_last_name   VARCHAR(100),
    shipping_first_name  VARCHAR(100),

    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- ========== CHAT ==========
CREATE TABLE chat (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,

    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- ========== FAVORITE ==========
CREATE TABLE favorite_item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (user_id, item_id),

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

-- ========== REVIEW ==========
CREATE TABLE review (
    id BIGSERIAL PRIMARY KEY,

    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,

    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    result VARCHAR(10) NOT NULL CHECK (result IN ('GOOD','BAD')),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (order_id, reviewer_id),

    FOREIGN KEY (order_id) REFERENCES app_order(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (reviewee_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

-- ========== REVIEW STATS ==========
CREATE TABLE review_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    avg_rating NUMERIC(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========== COMPLAINT ==========
CREATE TABLE user_complaint (
    id BIGSERIAL PRIMARY KEY,
    reported_user_id BIGINT NOT NULL,
    reporter_user_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reported_user_id) REFERENCES users(id),
    FOREIGN KEY (reporter_user_id) REFERENCES users(id)
);

-- ========== INQUIRY ==========
CREATE TABLE inquiry (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    admin_reply TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    replied_at TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========== CARD MASTER ==========
CREATE TABLE card_master (
    id BIGSERIAL PRIMARY KEY,
    set_code VARCHAR(20) NOT NULL,
    card_number VARCHAR(20) NOT NULL,
    card_name VARCHAR(255) NOT NULL,
    pack_name VARCHAR(255),
    rarity VARCHAR(50) NOT NULL,
    printed_regulation VARCHAR(5) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (set_code, card_number)
);

-- ========== INDEX ==========
CREATE INDEX idx_item_user_id ON item(user_id);
CREATE INDEX idx_item_category_id ON item(category_id);

CREATE INDEX idx_order_item_id ON app_order(item_id);
CREATE INDEX idx_order_buyer_id ON app_order(buyer_id);

CREATE INDEX idx_chat_item_id ON chat(item_id);
CREATE INDEX idx_chat_sender_id ON chat(sender_id);

CREATE INDEX idx_fav_user_id ON favorite_item(user_id);
CREATE INDEX idx_fav_item_id ON favorite_item(item_id);

CREATE INDEX idx_review_order_id ON review(order_id);
CREATE INDEX idx_review_reviewee_id ON review(reviewee_id);

-- ========== SEED: CATEGORY（★ V2 を統合） ==========
INSERT INTO category (name)
SELECT 'カード'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'カード');

INSERT INTO category (name)
SELECT 'サプライ'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'サプライ');

INSERT INTO category (name)
SELECT 'デッキ・構築済み'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'デッキ・構築済み');

INSERT INTO category (name)
SELECT 'その他'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'その他');
