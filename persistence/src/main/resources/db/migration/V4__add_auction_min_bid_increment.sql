ALTER TABLE auctions
    ADD COLUMN min_bid_increment DOUBLE PRECISION NOT NULL DEFAULT 500;

COMMENT ON COLUMN auctions.min_bid_increment IS 'Minimum amount each new bid must exceed the current standing price.';
