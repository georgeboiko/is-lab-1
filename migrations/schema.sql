BEGIN;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT app_users_id_positive CHECK (id > 0),
    CONSTRAINT app_users_username_not_blank CHECK (btrim(username) <> ''),
    CONSTRAINT app_users_password_hash_not_blank CHECK (btrim(password_hash) <> '')
);

CREATE TABLE IF NOT EXISTS auth_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT auth_tokens_user_fk FOREIGN KEY (user_id)
        REFERENCES app_users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS venues (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    type VARCHAR(16) NOT NULL,

    CONSTRAINT venues_id_positive CHECK (id > 0),
    CONSTRAINT venues_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT venues_capacity_positive CHECK (capacity > 0),
    CONSTRAINT venues_type_valid CHECK (type IN (
        'LOFT', 'OPEN_AREA', 'THEATRE', 'CINEMA', 'STADIUM'
    ))
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    event_date TIMESTAMP NOT NULL,
    min_age BIGINT NOT NULL,
    tickets_count INTEGER NOT NULL,
    description TEXT NOT NULL,

    CONSTRAINT events_id_positive CHECK (id > 0),
    CONSTRAINT events_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT events_tickets_count_positive CHECK (tickets_count > 0),
    CONSTRAINT events_description_not_blank CHECK (btrim(description) <> '')
);

CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    eye_color VARCHAR(16),
    hair_color VARCHAR(16) NOT NULL,
    height INTEGER,
    nationality VARCHAR(24),

    CONSTRAINT persons_id_positive CHECK (id > 0),
    CONSTRAINT persons_eye_color_valid CHECK (
        eye_color IS NULL OR eye_color IN ('RED', 'ORANGE', 'WHITE', 'BROWN')
    ),
    CONSTRAINT persons_hair_color_valid CHECK (
        hair_color IN ('RED', 'ORANGE', 'WHITE', 'BROWN')
    ),
    CONSTRAINT persons_height_positive CHECK (height IS NULL OR height > 0),
    CONSTRAINT persons_nationality_valid CHECK (
        nationality IS NULL OR nationality IN (
            'FRANCE', 'INDIA', 'VATICAN', 'NORTH_KOREA'
        )
    )
);

CREATE TABLE IF NOT EXISTS locations (
    person_id BIGINT PRIMARY KEY,
    x DOUBLE PRECISION NOT NULL,
    y INTEGER NOT NULL,
    z BIGINT NOT NULL,
    name TEXT NOT NULL,

    CONSTRAINT locations_person_fk FOREIGN KEY (person_id)
        REFERENCES persons (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tickets (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    coordinates_x INTEGER NOT NULL,
    coordinates_y BIGINT NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    person_id BIGINT,
    event_id BIGINT,
    price DOUBLE PRECISION,
    type VARCHAR(16),
    discount DOUBLE PRECISION NOT NULL,
    number INTEGER NOT NULL,
    venue_id BIGINT NOT NULL,

    CONSTRAINT tickets_id_positive CHECK (id > 0),
    CONSTRAINT tickets_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT tickets_coordinates_x_max CHECK (coordinates_x <= 222),
    CONSTRAINT tickets_price_positive CHECK (price IS NULL OR price > 0),
    CONSTRAINT tickets_type_valid CHECK (
        type IS NULL OR type IN ('VIP', 'USUAL', 'BUDGETARY', 'CHEAP')
    ),
    CONSTRAINT tickets_discount_range CHECK (discount > 0 AND discount <= 100),
    CONSTRAINT tickets_number_positive CHECK (number > 0),
    CONSTRAINT tickets_person_fk FOREIGN KEY (person_id)
        REFERENCES persons (id),
    CONSTRAINT tickets_event_fk FOREIGN KEY (event_id)
        REFERENCES events (id),
    CONSTRAINT tickets_venue_fk FOREIGN KEY (venue_id)
        REFERENCES venues (id)
);

COMMIT;
