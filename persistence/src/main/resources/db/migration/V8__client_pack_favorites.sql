CREATE TABLE client_pack_favorites (
    client_id BIGINT NOT NULL,
    pack_id   BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (client_id, pack_id),
    FOREIGN KEY (client_id) REFERENCES clients(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pack_id)   REFERENCES packs(id)       ON DELETE CASCADE
);

CREATE INDEX idx_client_pack_favorites_client ON client_pack_favorites(client_id);
