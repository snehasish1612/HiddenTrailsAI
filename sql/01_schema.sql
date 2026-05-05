-- =============================================================
--  HIDDEN TRAILS AI — 01_schema.sql
--  Run this FIRST in MySQL Workbench or terminal:
--  mysql -u root -p < 01_schema.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS hidden_trails
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hidden_trails;

-- ─────────────────────────────────────────
--  1. USERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id       INT             NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)    NOT NULL,
    email         VARCHAR(150)    NOT NULL UNIQUE,
    phone         VARCHAR(15),
    password_hash VARCHAR(255)    NOT NULL,
    role          ENUM('user','admin') NOT NULL DEFAULT 'user',
    is_active     TINYINT(1)      NOT NULL DEFAULT 1,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  2. DESTINATIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS destinations (
    destination_id  INT           NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)  NOT NULL,
    region          VARCHAR(100)  NOT NULL COMMENT 'North Bengal / Sikkim',
    description     TEXT,
    image_url       VARCHAR(255),
    is_active       TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (destination_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  3. HOTELS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hotels (
    hotel_id        INT           NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)  NOT NULL,
    location        VARCHAR(100)  NOT NULL,
    address         VARCHAR(255),
    price_per_night DECIMAL(10,2) NOT NULL,
    rating          TINYINT       NOT NULL DEFAULT 3
                                  COMMENT '1–5 star rating',
    category        ENUM('budget','mid-range','premium','luxury')
                                  NOT NULL DEFAULT 'mid-range',
    amenities       TEXT          COMMENT 'Comma-separated list',
    image_url       VARCHAR(255),
    is_active       TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (hotel_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  4. TRANSPORT
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transport (
    transport_id      INT           NOT NULL AUTO_INCREMENT,
    type              VARCHAR(80)   NOT NULL
                                    COMMENT 'shared-jeep / private-cab / toy-train / helicopter',
    route_from        VARCHAR(100)  NOT NULL,
    route_to          VARCHAR(100)  NOT NULL,
    price_per_person  DECIMAL(10,2) NOT NULL DEFAULT 0.00
                                    COMMENT '0.00 means this route is priced per vehicle — use total_price',
    total_price       DECIMAL(10,2) NOT NULL DEFAULT 0.00
                                    COMMENT 'Fixed price for full vehicle',
    duration_minutes  INT,
    provider          VARCHAR(100),
    is_active         TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (transport_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  5. FOOD OPTIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS food_options (
    food_id          INT           NOT NULL AUTO_INCREMENT,
    name             VARCHAR(150)  NOT NULL,
    location         VARCHAR(100)  NOT NULL,
    cuisine_type     VARCHAR(80)   NOT NULL
                                   COMMENT 'Sikkimese / Tibetan / Nepali / Multi-cuisine',
    price_per_person DECIMAL(10,2) NOT NULL,
    meal_type        SET('breakfast','lunch','dinner','snacks')
                                   NOT NULL DEFAULT 'lunch,dinner',
    is_vegetarian    TINYINT(1)    NOT NULL DEFAULT 0,
    image_url        VARCHAR(255),
    is_active        TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (food_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  6. ITINERARIES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS itineraries (
    itinerary_id    INT           NOT NULL AUTO_INCREMENT,
    user_id         INT           NOT NULL,
    destination     VARCHAR(150)  NOT NULL,
    title           VARCHAR(200),
    start_date      DATE          NOT NULL,
    end_date        DATE          NOT NULL,
    total_days      INT           NOT NULL DEFAULT 1,
    group_size      INT           NOT NULL DEFAULT 2,
    adults          INT           NOT NULL DEFAULT 2,
    children        INT           NOT NULL DEFAULT 0,
    budget          ENUM('budget','mid-range','premium','luxury')
                                  NOT NULL DEFAULT 'mid-range',
    travel_style    VARCHAR(255)  COMMENT 'JSON array e.g. ["adventure","nature"]',
    accommodation   VARCHAR(80),
    transport_pref  VARCHAR(80),
    food_pref       VARCHAR(255)  COMMENT 'JSON array',
    pace            TINYINT       NOT NULL DEFAULT 3
                                  COMMENT '1=relaxed … 5=packed',
    entry_point     VARCHAR(80),
    special_notes   TEXT,
    ai_prompt       TEXT,
    ai_response     LONGTEXT,
    estimated_cost  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ai_confidence   DECIMAL(5,2),
    status          ENUM('draft','customised','booked','cancelled')
                                  NOT NULL DEFAULT 'draft',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (itinerary_id),
    CONSTRAINT fk_itinerary_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  7. ITINERARY DAYS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS itinerary_days (
    day_id          INT           NOT NULL AUTO_INCREMENT,
    itinerary_id    INT           NOT NULL,
    day_number      INT           NOT NULL,
    day_title       VARCHAR(200),
    activity        TEXT          COMMENT 'AI-generated activity description',
    location        VARCHAR(150),
    estimated_cost  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hotel_id        INT,
    transport_id    INT,
    food_id         INT,
    notes           TEXT,
    PRIMARY KEY (day_id),
    UNIQUE KEY uq_itinerary_day (itinerary_id, day_number),
    CONSTRAINT fk_day_itinerary
        FOREIGN KEY (itinerary_id) REFERENCES itineraries (itinerary_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_day_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels (hotel_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_day_transport
        FOREIGN KEY (transport_id) REFERENCES transport (transport_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_day_food
        FOREIGN KEY (food_id) REFERENCES food_options (food_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  8. BOOKINGS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    booking_id       INT           NOT NULL AUTO_INCREMENT,
    user_id          INT           NOT NULL,
    itinerary_id     INT           NOT NULL,
    total_price      DECIMAL(12,2) NOT NULL,
    payment_method   VARCHAR(50)   COMMENT 'razorpay / cash / bank-transfer',
    payment_token    VARCHAR(255)  COMMENT 'Gateway transaction reference',
    payment_status   ENUM('pending','paid','failed','refunded')
                                   NOT NULL DEFAULT 'pending',
    booking_status   ENUM('pending','confirmed','cancelled','completed')
                                   NOT NULL DEFAULT 'pending',
    confirmation_no  VARCHAR(30)   UNIQUE
                                   COMMENT 'e.g. HT-2025-0880',
    booked_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_booking_itinerary
        FOREIGN KEY (itinerary_id) REFERENCES itineraries (itinerary_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  9. BOOKING ITEMS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS booking_items (
    item_id      INT           NOT NULL AUTO_INCREMENT,
    booking_id   INT           NOT NULL,
    day_number   INT           NOT NULL,
    item_type    ENUM('hotel','transport','food')  NOT NULL,
    ref_id       INT           NOT NULL
                               COMMENT 'FK to hotel_id / transport_id / food_id',
    item_name    VARCHAR(200),
    quantity     INT           NOT NULL DEFAULT 1,
    unit_price   DECIMAL(10,2) NOT NULL,
    total_price  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (item_id),
    CONSTRAINT fk_item_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (booking_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  10. PACKAGES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS packages (
    package_id     INT           NOT NULL AUTO_INCREMENT,
    title          VARCHAR(200)  NOT NULL,
    destination    VARCHAR(150)  NOT NULL,
    description    TEXT,
    base_price     DECIMAL(12,2) NOT NULL,
    duration_days  INT           NOT NULL,
    max_persons    INT           NOT NULL DEFAULT 10,
    image_url      VARCHAR(255),
    status         ENUM('active','inactive','draft')
                                 NOT NULL DEFAULT 'active',
    created_by     INT           COMMENT 'admin user_id',
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (package_id),
    CONSTRAINT fk_package_admin
        FOREIGN KEY (created_by) REFERENCES users (user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  11. PACKAGE ITEMS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS package_items (
    pkg_item_id  INT    NOT NULL AUTO_INCREMENT,
    package_id   INT    NOT NULL,
    item_type    ENUM('hotel','transport','food') NOT NULL,
    ref_id       INT    NOT NULL,
    day_number   INT    NOT NULL DEFAULT 1,
    PRIMARY KEY (pkg_item_id),
    CONSTRAINT fk_pkgitem_package
        FOREIGN KEY (package_id) REFERENCES packages (package_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
--  12. ADMINS (extended profile for admin users)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS admins (
    admin_id     INT           NOT NULL AUTO_INCREMENT,
    user_id      INT           NOT NULL UNIQUE,
    admin_role   ENUM('super-admin','manager','support')
                               NOT NULL DEFAULT 'support',
    permissions  TEXT          COMMENT 'JSON array of allowed actions',
    last_login   TIMESTAMP,
    PRIMARY KEY (admin_id),
    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
