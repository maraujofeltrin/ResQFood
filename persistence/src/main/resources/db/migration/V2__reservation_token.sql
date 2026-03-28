CREATE TABLE reservation_tokens (
    token VARCHAR(255) PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,       -- 'ACCEPT' o 'REJECT'
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);