-- =============================================================
--  HIDDEN TRAILS AI — 04_views.sql
--  Run AFTER 03_indexes.sql
--  Creates views used by AdminServlet and reporting queries.
-- =============================================================

USE hidden_trails;

-- ─────────────────────────────────────────
--  1. BOOKING SUMMARY VIEW
--  Used by: AdminServlet (booking list table)
--  Shows every booking joined with user and itinerary details.
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW booking_summary_view AS
SELECT
    b.booking_id,
    b.confirmation_no,
    b.booked_at,
    b.booking_status,
    b.payment_status,
    b.total_price,
    b.payment_method,
    u.user_id,
    u.name        AS user_name,
    u.email       AS user_email,
    u.phone       AS user_phone,
    i.itinerary_id,
    i.title       AS itinerary_title,
    i.destination,
    i.start_date,
    i.end_date,
    DATEDIFF(i.end_date, i.start_date) + 1  AS total_days,
    i.adults,
    i.children,
    i.budget
FROM bookings        b
JOIN users           u  ON b.user_id       = u.user_id
JOIN itineraries     i  ON b.itinerary_id  = i.itinerary_id;

-- ─────────────────────────────────────────
--  2. ITINERARY DETAIL VIEW
--  Used by: ItineraryServlet (GET /api/itinerary/{id})
--  Returns itinerary with user name attached.
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW itinerary_detail_view AS
SELECT
    i.itinerary_id,
    i.title,
    i.destination,
    i.start_date,
    i.end_date,
    i.total_days,
    i.adults,
    i.children,
    i.budget,
    i.travel_style,
    i.accommodation,
    i.transport_pref,
    i.food_pref,
    i.pace,
    i.entry_point,
    i.special_notes,
    i.estimated_cost,
    i.ai_confidence,
    i.status,
    i.created_at,
    i.user_id,
    u.name        AS user_name,
    u.email       AS user_email
FROM itineraries i
JOIN users       u ON i.user_id = u.user_id;

-- ─────────────────────────────────────────
--  3. ITINERARY DAY DETAIL VIEW
--  Used by: ItineraryServlet (day cards on result page)
--  Each day row enriched with selected hotel, transport, food names.
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW itinerary_day_detail_view AS
SELECT
    d.day_id,
    d.itinerary_id,
    d.day_number,
    d.day_title,
    d.activity,
    d.location,
    d.estimated_cost,
    d.notes,
    -- Selected hotel
    d.hotel_id,
    h.name             AS hotel_name,
    h.price_per_night  AS hotel_price,
    h.rating           AS hotel_rating,
    h.category         AS hotel_category,
    -- Selected transport
    d.transport_id,
    t.type             AS transport_type,
    t.route_from,
    t.route_to,
    t.price_per_person AS transport_price_pp,
    t.total_price      AS transport_total_price,
    t.provider         AS transport_provider,
    -- Selected food
    d.food_id,
    f.name             AS food_name,
    f.cuisine_type     AS food_cuisine,
    f.price_per_person AS food_price
FROM itinerary_days  d
LEFT JOIN hotels       h ON d.hotel_id     = h.hotel_id
LEFT JOIN transport    t ON d.transport_id = t.transport_id
LEFT JOIN food_options f ON d.food_id      = f.food_id;

-- ─────────────────────────────────────────
--  4. ADMIN DASHBOARD VIEW
--  Used by: AdminServlet (summary stats cards)
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW admin_dashboard_view AS
SELECT
    -- Total counts
    (SELECT COUNT(*) FROM users      WHERE role = 'user')       AS total_users,
    (SELECT COUNT(*) FROM itineraries)                          AS total_itineraries,
    (SELECT COUNT(*) FROM bookings   WHERE booking_status = 'confirmed') AS confirmed_bookings,
    (SELECT COUNT(*) FROM bookings   WHERE booking_status = 'pending')   AS pending_bookings,
    -- Revenue
    (SELECT COALESCE(SUM(total_price), 0)
     FROM bookings WHERE payment_status = 'paid')               AS total_revenue,
    -- Today's activity
    (SELECT COUNT(*) FROM bookings
     WHERE DATE(booked_at) = CURDATE())                         AS bookings_today,
    (SELECT COUNT(*) FROM itineraries
     WHERE DATE(created_at) = CURDATE())                        AS itineraries_today,
    -- Draft itineraries (not yet booked)
    (SELECT COUNT(*) FROM itineraries WHERE status = 'draft')   AS draft_itineraries;

-- ─────────────────────────────────────────
--  5. USER TRIP HISTORY VIEW
--  Used by: UserItineraryListServlet (dashboard.jsp)
--  Shows each itinerary with its booking status (if any).
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW user_trip_history_view AS
SELECT
    i.itinerary_id,
    i.user_id,
    i.title,
    i.destination,
    i.start_date,
    i.end_date,
    i.total_days,
    i.adults,
    i.children,
    i.budget,
    i.estimated_cost,
    i.status           AS itinerary_status,
    i.created_at,
    b.booking_id,
    b.confirmation_no,
    b.booking_status,
    b.payment_status,
    b.total_price      AS booking_total
FROM itineraries i
LEFT JOIN bookings b
    ON i.itinerary_id = b.itinerary_id
    AND b.booking_status != 'cancelled';

-- ─────────────────────────────────────────
--  6. REVENUE BY DESTINATION VIEW
--  Used by: Admin reports page
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW revenue_by_destination_view AS
SELECT
    i.destination,
    COUNT(DISTINCT b.booking_id)  AS total_bookings,
    SUM(b.total_price)            AS total_revenue,
    AVG(b.total_price)            AS avg_booking_value,
    COUNT(DISTINCT i.user_id)     AS unique_travellers
FROM itineraries i
JOIN bookings    b
    ON i.itinerary_id  = b.itinerary_id
    AND b.payment_status = 'paid'
GROUP BY i.destination
ORDER BY total_revenue DESC;
