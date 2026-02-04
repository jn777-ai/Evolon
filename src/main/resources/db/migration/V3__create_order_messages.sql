CREATE TABLE order_messages (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_messages_order
        FOREIGN KEY (order_id)
        REFERENCES app_order(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_messages_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id)
);

-- 検索・表示用インデックス
CREATE INDEX idx_order_messages_order_id
    ON order_messages(order_id);

CREATE INDEX idx_order_messages_sender_id
    ON order_messages(sender_id);