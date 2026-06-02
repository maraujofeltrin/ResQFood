CREATE SEQUENCE notifications_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE client_notification_preferences_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE notifications (
    id              BIGINT PRIMARY KEY DEFAULT nextval('notifications_id_seq'),
    recipient_id    BIGINT NOT NULL,
    type            VARCHAR(64) NOT NULL,
    reservation_id  BIGINT,
    auction_id      BIGINT,
    pack_id         BIGINT,
    pack_title      VARCHAR(255),
    commerce_name   VARCHAR(255),
    amount          DOUBLE PRECISION,
    pickup_code     VARCHAR(32),
    pickup_date     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMP,
    deleted_at      TIMESTAMP,
    FOREIGN KEY (recipient_id)   REFERENCES users(id)             ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)       ON DELETE SET NULL,
    FOREIGN KEY (auction_id)     REFERENCES auctions(id)           ON DELETE SET NULL,
    FOREIGN KEY (pack_id)        REFERENCES packs(id)              ON DELETE SET NULL
);

CREATE INDEX idx_notifications_recipient_inbox
    ON notifications(recipient_id, deleted_at, read_at, created_at);
CREATE INDEX idx_notifications_recipient_created
    ON notifications(recipient_id, created_at);

CREATE TABLE client_notification_preferences (
    id           BIGINT PRIMARY KEY DEFAULT nextval('client_notification_preferences_id_seq'),
    client_id    BIGINT NOT NULL,
    type         VARCHAR(64) NOT NULL,
    mail_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (client_id) REFERENCES clients(user_id) ON DELETE CASCADE,
    CONSTRAINT uk_client_notification_preferences_client_type UNIQUE (client_id, type)
);

CREATE INDEX idx_client_notification_preferences_client_type
    ON client_notification_preferences(client_id, type);
