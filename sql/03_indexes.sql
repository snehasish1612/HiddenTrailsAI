-- =============================================================
--  HIDDEN TRAILS AI — 03_indexes.sql
--  Run AFTER 02_seed_data.sql
--  Adds indexes for all common query patterns.
-- =============================================================

USE hidden_trails;

-- ─────────────────────────────────────────
--  USERS
-- ─────────────────────────────────────────
-- Login lookup by email (most frequent query)
CREATE INDEX idx_users_email
    ON users (email);

-- Role filtering for admin panel
CREATE INDEX idx_users_role
    ON users (role);

-- ─────────────────────────────────────────
--  ITINERARIES
-- ─────────────────────────────────────────
-- Fetch all itineraries for a user (dashboard page)
CREATE INDEX idx_itineraries_user_id
    ON itineraries (user_id);

-- Filter by destination (browse/search)
CREATE INDEX idx_itineraries_destination
    ON itineraries (destination);

-- Filter by status (admin view: draft / booked / cancelled)
CREATE INDEX idx_itineraries_status
    ON itineraries (status);

-- Date range queries (upcoming trips)
CREATE INDEX idx_itineraries_start_date
    ON itineraries (start_date);

-- ─────────────────────────────────────────
--  ITINERARY DAYS
-- ─────────────────────────────────────────
-- Fetch all days for a given itinerary (result page load)
CREATE INDEX idx_itinerary_days_itinerary_id
    ON itinerary_days (itinerary_id);

-- Lookup by selected hotel (for pricing updates)
CREATE INDEX idx_itinerary_days_hotel_id
    ON itinerary_days (hotel_id);

-- Lookup by selected transport
CREATE INDEX idx_itinerary_days_transport_id
    ON itinerary_days (transport_id);

-- ─────────────────────────────────────────
--  BOOKINGS
-- ─────────────────────────────────────────
-- Fetch all bookings for a user (dashboard)
CREATE INDEX idx_bookings_user_id
    ON bookings (user_id);

-- Fetch booking for a specific itinerary
CREATE INDEX idx_bookings_itinerary_id
    ON bookings (itinerary_id);

-- Filter by payment status (admin: pending payments)
CREATE INDEX idx_bookings_payment_status
    ON bookings (payment_status);

-- Filter by booking status (admin: confirmed / cancelled)
CREATE INDEX idx_bookings_booking_status
    ON bookings (booking_status);

-- Lookup by confirmation number (email confirmation links)
CREATE INDEX idx_bookings_confirmation_no
    ON bookings (confirmation_no);

-- ─────────────────────────────────────────
--  BOOKING ITEMS
-- ─────────────────────────────────────────
-- Fetch all items for a booking
CREATE INDEX idx_booking_items_booking_id
    ON booking_items (booking_id);

-- ─────────────────────────────────────────
--  HOTELS
-- ─────────────────────────────────────────
-- PricingService queries hotels by location
CREATE INDEX idx_hotels_location
    ON hotels (location);

-- Filter by category (mid-range / luxury etc.)
CREATE INDEX idx_hotels_category
    ON hotels (category);

-- Active hotels only
CREATE INDEX idx_hotels_is_active
    ON hotels (is_active);

-- ─────────────────────────────────────────
--  TRANSPORT
-- ─────────────────────────────────────────
-- PricingService queries routes by origin
CREATE INDEX idx_transport_route_from
    ON transport (route_from);

-- Route pair lookup (from + to)
CREATE INDEX idx_transport_route_pair
    ON transport (route_from, route_to);

-- Filter by type (shared-jeep / private-cab etc.)
CREATE INDEX idx_transport_type
    ON transport (type);

-- ─────────────────────────────────────────
--  FOOD OPTIONS
-- ─────────────────────────────────────────
-- PricingService queries food by location
CREATE INDEX idx_food_location
    ON food_options (location);

-- Filter vegetarian options
CREATE INDEX idx_food_is_vegetarian
    ON food_options (is_vegetarian);

-- ─────────────────────────────────────────
--  PACKAGES
-- ─────────────────────────────────────────
-- Browse packages by destination (landing page)
CREATE INDEX idx_packages_destination
    ON packages (destination);

-- Active packages only
CREATE INDEX idx_packages_status
    ON packages (status);
