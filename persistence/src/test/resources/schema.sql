DROP TABLE IF EXISTS tokens CASCADE;
DROP TABLE IF EXISTS bids CASCADE;
DROP TABLE IF EXISTS auctions CASCADE;
DROP TABLE IF EXISTS reservation_tokens CASCADE;
DROP TABLE IF EXISTS pack_tags CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS packs CASCADE;
DROP TABLE IF EXISTS commerces CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS images CASCADE;

CREATE TABLE images (
    id INTEGER IDENTITY PRIMARY KEY,
    data BLOB NOT NULL,
    content_type VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id INTEGER IDENTITY PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50),
    verified BOOLEAN DEFAULT FALSE NOT NULL,
    locale VARCHAR(10) NOT NULL,
    profile_image_id BIGINT,
    FOREIGN KEY (profile_image_id) REFERENCES images(id) ON DELETE SET NULL
);

ALTER TABLE users ALTER COLUMN locale SET DEFAULT 'es';

CREATE TABLE clients (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    last_name VARCHAR(255),
    notifications_visibility_preferences BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE commerces (
    user_id BIGINT PRIMARY KEY,
    commercial_name VARCHAR(255),
    category VARCHAR(50),
    street VARCHAR(255),
    street_number INTEGER,
    city VARCHAR(255),
    province VARCHAR(255),
    postal_code VARCHAR(50),
    opening_time VARCHAR(50),
    closing_time VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE packs (
    id INTEGER IDENTITY PRIMARY KEY,
    commerce_id BIGINT NOT NULL,
    title VARCHAR(255),
    description VARCHAR(2000),
    original_price DOUBLE PRECISION,
    final_price DOUBLE PRECISION,
    stock INTEGER,
    active BOOLEAN,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    image_id BIGINT,
    FOREIGN KEY (commerce_id) REFERENCES commerces(user_id) ON DELETE CASCADE,
    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE SET NULL
);

CREATE TABLE reservations (
    id INTEGER IDENTITY PRIMARY KEY,
    customer_id BIGINT,
    pack_id BIGINT,
    reservation_date TIMESTAMP,
    final_price DOUBLE PRECISION,
    status VARCHAR(50),
    pickup_code VARCHAR(255) UNIQUE,
    pickup_confirmation_date TIMESTAMP,
    quantity INTEGER DEFAULT 1 NOT NULL,
    pickup_window VARCHAR(512),
    FOREIGN KEY (customer_id) REFERENCES clients(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE CASCADE
);

CREATE TABLE pack_tags (
    pack_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (pack_id, tag),
    FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE CASCADE
);

CREATE TABLE reservation_tokens (
    token VARCHAR(255) PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

CREATE TABLE auctions (
    id INTEGER IDENTITY PRIMARY KEY,
    pack_id BIGINT NOT NULL UNIQUE,
    initial_price DOUBLE PRECISION NOT NULL,
    min_bid_increment DOUBLE PRECISION NOT NULL,
    current_bid DOUBLE PRECISION,
    current_bidder_id BIGINT,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE CASCADE,
    FOREIGN KEY (current_bidder_id) REFERENCES clients(user_id) ON DELETE SET NULL
);

CREATE TABLE bids (
    id INTEGER IDENTITY PRIMARY KEY,
    auction_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(user_id) ON DELETE CASCADE
);

CREATE TABLE tokens (
    token VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
