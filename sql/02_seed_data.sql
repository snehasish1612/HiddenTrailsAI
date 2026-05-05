-- =============================================================
--  HIDDEN TRAILS AI — 02_seed_data.sql
--  Run AFTER 01_schema.sql
--  Populates destinations, hotels, transport, food options,
--  and one admin user for testing.
-- =============================================================

USE hidden_trails;

-- ─────────────────────────────────────────
--  DESTINATIONS
-- ─────────────────────────────────────────
INSERT INTO destinations (name, region, description, image_url) VALUES
('Darjeeling',   'North Bengal',
 'Queen of the Hills — tea estates, Tiger Hill sunrise, Toy Train, Himalayan views.',
 'images/destinations/darjeeling.jpg'),
('Gangtok',      'Sikkim',
 'Capital of Sikkim — MG Marg, Rumtek Monastery, Nathula Pass gateway.',
 'images/destinations/gangtok.jpg'),
('Pelling',      'Sikkim',
 'Kanchenjunga base views, Pemayangtse Monastery, Rabdentse ruins.',
 'images/destinations/pelling.jpg'),
('Lachung',      'Sikkim',
 'Gateway to Yumthang Valley — river valley drives, North Sikkim permits.',
 'images/destinations/lachung.jpg'),
('Namchi',       'Sikkim',
 'Char Dham replica, Samdruptse statue, South Sikkim culture.',
 'images/destinations/namchi.jpg'),
('Kalimpong',    'North Bengal',
 'Flower nurseries, Deolo Hill, colonial-era architecture.',
 'images/destinations/kalimpong.jpg'),
('North Sikkim', 'Sikkim',
 'Remote Gurudongmar Lake, Lachen, alpine meadows and restricted-area trails.',
 'images/destinations/north_sikkim.jpg'),
('Full Circuit',  'North Bengal & Sikkim',
 'Complete 10–14 day circuit covering Darjeeling, Gangtok, Pelling, and North Sikkim.',
 'images/destinations/full_circuit.jpg');

-- ─────────────────────────────────────────
--  HOTELS
-- ─────────────────────────────────────────
INSERT INTO hotels (name, location, address, price_per_night, rating, category, amenities) VALUES
-- Darjeeling
('Hotel Dekeling',          'Darjeeling', '51 Gandhi Road, Mall Road',        2200.00, 3, 'mid-range',  'WiFi,Breakfast,Room Service,Mountain View'),
('Revolver Hotel',          'Darjeeling', '1 Robertson Road',                  2800.00, 4, 'premium',    'WiFi,Breakfast,Bar,Rooftop Terrace'),
('Windamere Hotel',         'Darjeeling', 'Observatory Hill',                  8500.00, 5, 'luxury',     'WiFi,Full Board,Heritage Property,Garden'),
('Holidayz Inn Darjeeling', 'Darjeeling', 'AJC Bose Road',                     1400.00, 2, 'budget',     'WiFi,Hot Water,Parking'),
('Cedar Inn',               'Darjeeling', 'Zakir Hussain Road',                1800.00, 3, 'mid-range',  'WiFi,Breakfast,Mountain View'),

-- Gangtok
('Hotel Tashi Delek',       'Gangtok',   'MG Marg',                           2500.00, 3, 'mid-range',  'WiFi,Breakfast,Mountain View,Restaurant'),
('Elgin Nor-Khill',         'Gangtok',   'Stadium Road',                       5200.00, 5, 'luxury',     'WiFi,Full Board,Heritage,Spa,Garden'),
('Modern Central Hotel',    'Gangtok',   'MG Marg',                            1600.00, 2, 'budget',     'WiFi,Hot Water,Central Location'),
('Hotel Sonam Delek',       'Gangtok',   'Tibet Road',                         2200.00, 3, 'mid-range',  'WiFi,Breakfast,City View'),
('Mayfair Spa Resort',      'Gangtok',   'Jawaharlal Nehru Road',              7800.00, 5, 'luxury',     'WiFi,Full Board,Spa,Pool,Mountain View'),

-- Pelling
('Norbu Ghang Resort',      'Pelling',   'Upper Pelling',                      3200.00, 4, 'premium',    'WiFi,Breakfast,Kanchenjunga View,Garden'),
('Hotel Garuda',            'Pelling',   'Middle Pelling',                     1500.00, 2, 'budget',     'WiFi,Hot Water,Mountain View'),

-- Lachung
('Hotel Yabshi Phunkhang',  'Lachung',   'Lachung Village',                    2400.00, 3, 'mid-range',  'All Meals,Hot Water,Mountain View'),
('Lachung Homestay Tashi',  'Lachung',   'Near Lachung Monastery',             1800.00, 3, 'mid-range',  'All Meals,Local Experience,Bonfire'),

-- Kalimpong
('Silver Oaks',             'Kalimpong', 'Rinchenpong Road',                   3500.00, 4, 'premium',    'WiFi,Breakfast,Garden,Mountain View'),
('Lodge Kalimpong',         'Kalimpong', 'Rinkingpong Road',                   1200.00, 2, 'budget',     'Hot Water,Parking');

-- ─────────────────────────────────────────
--  TRANSPORT
-- ─────────────────────────────────────────
INSERT INTO transport (type, route_from, route_to, price_per_person, total_price, duration_minutes, provider) VALUES
-- NJP / Bagdogra departures
('shared-jeep',  'NJP',        'Darjeeling',  320.00,  0.00,    180, 'Local Jeep Union'),
('private-cab',  'NJP',        'Darjeeling',  0.00,    1800.00, 165, 'Self'),
('shared-jeep',  'NJP',        'Gangtok',     280.00,  0.00,    240, 'SNT / Local Union'),
('private-cab',  'NJP',        'Gangtok',     0.00,    2500.00, 210, 'Self'),
('private-cab',  'Bagdogra',   'Darjeeling',  0.00,    2200.00, 190, 'Self'),
('private-cab',  'Bagdogra',   'Gangtok',     0.00,    3000.00, 250, 'Self'),

-- Darjeeling transfers
('shared-jeep',  'Darjeeling', 'Gangtok',     350.00,  0.00,    270, 'Local Jeep Union'),
('private-cab',  'Darjeeling', 'Gangtok',     0.00,    2200.00, 240, 'Self'),
('shared-jeep',  'Darjeeling', 'Kalimpong',   180.00,  0.00,    90,  'Local Jeep Union'),
('private-cab',  'Darjeeling', 'Kalimpong',   0.00,    1200.00, 80,  'Self'),

-- Gangtok local & outstation
('permit-jeep',  'Gangtok',    'Nathula Pass', 0.00,    2800.00, 150, 'Permit Jeep Syndicate'),
('permit-jeep',  'Gangtok',    'Tsomgo Lake',  0.00,    1800.00, 90,  'Permit Jeep Syndicate'),
('shared-jeep',  'Gangtok',    'Pelling',      300.00,  0.00,    210, 'Local Jeep Union'),
('private-cab',  'Gangtok',    'Pelling',      0.00,    2800.00, 195, 'Self'),
('permit-jeep',  'Gangtok',    'Lachung',      0.00,    5000.00, 270, 'North Sikkim Union'),
('private-cab',  'Gangtok',    'NJP',          0.00,    2500.00, 210, 'Self'),

-- Darjeeling sightseeing
('toy-train',    'Darjeeling', 'Ghum Loop',    250.00,  0.00,    60,  'DHR (UNESCO)'),
('local-jeep',   'Darjeeling', 'Sightseeing',  0.00,    900.00,  480, 'Local Day Hire');

-- ─────────────────────────────────────────
--  FOOD OPTIONS
-- ─────────────────────────────────────────
INSERT INTO food_options (name, location, cuisine_type, price_per_person, meal_type, is_vegetarian) VALUES
-- Darjeeling
('Glenary\'s Bakery & Restaurant', 'Darjeeling', 'Multi-cuisine',     600.00, 'breakfast,lunch',       0),
('Kunga Restaurant',               'Darjeeling', 'Tibetan',           450.00, 'lunch,dinner',          0),
('Gatty\'s Café',                  'Darjeeling', 'Multi-cuisine',     700.00, 'breakfast,lunch,dinner',0),
('Sonam\'s Kitchen',               'Darjeeling', 'Nepali / Tibetan',  380.00, 'lunch,dinner',          0),
('Nathmull\'s Tea Room',           'Darjeeling', 'Sikkimese',         300.00, 'breakfast,snacks',      1),
('Frank Ross Café',                'Darjeeling', 'Bakery / Café',     350.00, 'breakfast,snacks',      1),

-- Gangtok
('The Square – MG Marg',          'Gangtok',   'Multi-cuisine',      750.00, 'lunch,dinner',           0),
('Roll House Gangtok',             'Gangtok',   'Street Food',        300.00, 'lunch,snacks',           0),
('Café Tibet',                     'Gangtok',   'Tibetan',            600.00, 'lunch,dinner',           1),
('Nimtho Restaurant',              'Gangtok',   'Sikkimese',          500.00, 'lunch,dinner',           0),
('House of Bamboo',                'Gangtok',   'Sikkimese / Nepali', 550.00, 'dinner',                 0),
('Bakers Café MG Marg',            'Gangtok',   'Bakery / Café',      350.00, 'breakfast,snacks',       1),

-- Lachung / North Sikkim
('Lachung Homestay Meals',         'Lachung',   'Sikkimese',          350.00, 'breakfast,lunch,dinner', 0),
('Roadside Dhaba – Tsomgo',        'Tsomgo',    'Tibetan / Nepali',   250.00, 'lunch,snacks',           0),
('Yumthang Dhaba',                 'Yumthang',  'Nepali / Tibetan',   200.00, 'snacks',                 0),

-- Kalimpong
('Gompu\'s Bar & Restaurant',      'Kalimpong', 'Multi-cuisine',      500.00, 'lunch,dinner',           0),
('Cloud Nine Restaurant',          'Kalimpong', 'Sikkimese / Nepali', 450.00, 'lunch,dinner',           0);

-- ─────────────────────────────────────────
--  ADMIN USER
--  Password = Admin@1234  (BCrypt hash below)
--  Change this before production!
-- ─────────────────────────────────────────
INSERT INTO users (name, email, phone, password_hash, role) VALUES
('Admin User',
 'admin@hiddentrails.com',
 '9800000000',
 '$2a$12$Xl0yhvzLIaJCDdKBS0Zg2uoMGg0A5G3t9fYk1xX4/Q.eOh3wHZ9Gy',
 'admin');

INSERT INTO admins (user_id, admin_role, permissions) VALUES
(LAST_INSERT_ID(), 'super-admin',
 '["manage_bookings","manage_users","manage_packages","view_reports"]');

-- ─────────────────────────────────────────
--  SAMPLE REGULAR USER (for dev/testing)
--  Password = Test@1234
-- ─────────────────────────────────────────
INSERT INTO users (name, email, phone, password_hash, role) VALUES
('Arjun Sharma',
 'arjun@example.com',
 '9800000001',
 '$2a$12$KIXv3Lz4kJ5aBmNW1ePqSOY/H5mZsV1cZH5eMt5Jx7dfMwN9bGoXi',
 'user');
