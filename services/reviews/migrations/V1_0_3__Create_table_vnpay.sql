CREATE TABLE reviews.vnpay_transactions
(
    id               BIGSERIAL PRIMARY KEY,
    transaction_code VARCHAR(255),
    bank_code        VARCHAR(50),
    payment_method   VARCHAR(50),
    card_type        VARCHAR(50),
    amount           DECIMAL(18, 2),
    currency         VARCHAR(10),
    status           VARCHAR(10),
    order_info       TEXT,
    pay_date         VARCHAR(20),
    response_code    VARCHAR(10),
    tmn_code         VARCHAR(50),
    secure_hash      TEXT,
    order_id         BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
