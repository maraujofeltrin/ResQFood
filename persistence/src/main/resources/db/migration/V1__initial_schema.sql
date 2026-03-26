CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50)
);

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
    id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    original_price DOUBLE PRECISION,
    final_price DOUBLE PRECISION,
    stock INTEGER,
    active BOOLEAN
);

CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    customer_id BIGINT,
    pack_id BIGINT,
    reservation_date TIMESTAMP,
    final_price DOUBLE PRECISION,
    status VARCHAR(50),
    pickup_code VARCHAR(255),
    pickup_confirmation_date TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES clients(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE CASCADE
);
