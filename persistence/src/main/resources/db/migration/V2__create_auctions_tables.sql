CREATE TABLE auctions (
    id                SERIAL PRIMARY KEY,
    pack_id           BIGINT NOT NULL UNIQUE,
    initial_price     DOUBLE PRECISION NOT NULL,
    current_bid       DOUBLE PRECISION,
    current_bidder_id BIGINT,
    end_time          TIMESTAMP NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE CASCADE,
    FOREIGN KEY (current_bidder_id) REFERENCES clients(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_auctions_status ON auctions(status);
CREATE INDEX idx_auctions_end_time ON auctions(end_time);
CREATE INDEX idx_auctions_pack_id ON auctions(pack_id);

CREATE TABLE bids (
    id         SERIAL PRIMARY KEY,
    auction_id BIGINT NOT NULL,
    client_id  BIGINT NOT NULL,
    amount     DOUBLE PRECISION NOT NULL,
    timestamp  TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_bids_auction_id ON bids(auction_id);
CREATE INDEX idx_bids_client_id ON bids(client_id);
