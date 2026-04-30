CREATE TABLE IF NOT EXISTS commerce_reviews (
    id               SERIAL PRIMARY KEY,
    commerce_user_id BIGINT NOT NULL,
    client_user_id   BIGINT NOT NULL,
    rating           SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body             TEXT NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (commerce_user_id) REFERENCES commerces(user_id) ON DELETE CASCADE,
    FOREIGN KEY (client_user_id) REFERENCES clients(user_id) ON DELETE CASCADE,
    UNIQUE (client_user_id, commerce_user_id)
);

CREATE INDEX idx_commerce_reviews_commerce_user_id ON commerce_reviews(commerce_user_id);
