CREATE TABLE client_commerce_favorites (
    client_id    BIGINT NOT NULL,
    commerce_id  BIGINT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (client_id, commerce_id),
    FOREIGN KEY (client_id)   REFERENCES clients(user_id)    ON DELETE CASCADE,
    FOREIGN KEY (commerce_id) REFERENCES commerces(user_id)  ON DELETE CASCADE
);

CREATE INDEX idx_client_commerce_favorites_client ON client_commerce_favorites(client_id);
