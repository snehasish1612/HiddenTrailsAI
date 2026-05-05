package com.hiddentrails.servlet;

import com.google.gson.*;
import com.hiddentrails.dao.*;
import com.hiddentrails.model.Booking;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * AdminServlet — admin-only management endpoints.
 *
 * GET  /api/admin/bookings          → list all bookings
 * GET  /api/admin/bookings/{id}     → get booking detail
 * PUT  /api/admin/bookings/{id}     → update booking/payment status
 * GET  /api/admin/dashboard         → summary stats
 * GET  /api/admin/users             → list all users
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final BookingDAO     bookingDAO     = new BookingDAO();
    private final UserDAO        userDAO        = new UserDAO();
    private final ItineraryDAO   itineraryDAO   = new ItineraryDAO();

    // ── GET dispatcher ────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (!isAdmin(req, resp)) return;

        String path = req.getPathInfo(); // e.g. "/bookings" or "/bookings/5"
        if (path == null) path = "/";

        if (path.equals("/dashboard")) {
            handleDashboard(resp);
        } else if (path.equals("/bookings") || path.equals("/bookings/")) {
            handleListBookings(resp);
        } else if (path.startsWith("/bookings/")) {
            handleGetBooking(path, resp);
        } else if (path.equals("/users") || path.equals("/users/")) {
            handleListUsers(resp);
        } else {
            ResponseUtil.sendError(resp, 404, "NOT_FOUND", "Admin endpoint not found");
        }
    }

    // ── PUT dispatcher ────────────────────────────────────────────
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (!isAdmin(req, resp)) return;

        String path = req.getPathInfo();
        if (path != null && path.startsWith("/bookings/")) {
            handleUpdateBooking(path, req, resp);
        } else {
            ResponseUtil.sendError(resp, 404, "NOT_FOUND", "Admin endpoint not found");
        }
    }

    // ── Dashboard stats ───────────────────────────────────────────
    private void handleDashboard(HttpServletResponse resp) throws IOException {
        try (java.sql.Connection conn =
                 com.hiddentrails.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM admin_dashboard_view");
             java.sql.ResultSet rs = ps.executeQuery()) {

            Map<String, Object> stats = new LinkedHashMap<>();
            if (rs.next()) {
                stats.put("totalUsers",         rs.getInt("total_users"));
                stats.put("totalItineraries",   rs.getInt("total_itineraries"));
                stats.put("confirmedBookings",  rs.getInt("confirmed_bookings"));
                stats.put("pendingBookings",    rs.getInt("pending_bookings"));
                stats.put("totalRevenue",       rs.getBigDecimal("total_revenue"));
                stats.put("bookingsToday",      rs.getInt("bookings_today"));
                stats.put("itinerariesToday",   rs.getInt("itineraries_today"));
                stats.put("draftItineraries",   rs.getInt("draft_itineraries"));
            }
            ResponseUtil.sendJson(resp, stats);

        } catch (java.sql.SQLException e) {
            ResponseUtil.sendError(resp, 500, "DB_ERROR",
                "Could not load dashboard data");
        }
    }

    // ── List all bookings ─────────────────────────────────────────
    private void handleListBookings(HttpServletResponse resp) throws IOException {
        List<Booking> bookings = bookingDAO.findAll();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total",    bookings.size());
        response.put("bookings", bookings);
        ResponseUtil.sendJson(resp, response);
    }

    // ── Get single booking by ID ──────────────────────────────────
    private void handleGetBooking(String path, HttpServletResponse resp)
            throws IOException {
        try {
            int bookingId = Integer.parseInt(path.replace("/bookings/", ""));
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                ResponseUtil.sendError(resp, 404, "NOT_FOUND",
                    "Booking " + bookingId + " not found");
                return;
            }
            ResponseUtil.sendJson(resp, booking);
        } catch (NumberFormatException e) {
            ResponseUtil.sendError(resp, 400, "INVALID_ID", "Booking ID must be a number");
        }
    }

    // ── Update booking status ─────────────────────────────────────
    private void handleUpdateBooking(String path, HttpServletRequest req,
                                      HttpServletResponse resp)
            throws IOException {
        try {
            int bookingId = Integer.parseInt(path.replace("/bookings/", ""));

            JsonObject body = parseBody(req);
            String bookingStatus = body.has("bookingStatus")
                ? body.get("bookingStatus").getAsString() : null;
            String paymentStatus = body.has("paymentStatus")
                ? body.get("paymentStatus").getAsString() : null;

            Booking existing = bookingDAO.findById(bookingId);
            if (existing == null) {
                ResponseUtil.sendError(resp, 404, "NOT_FOUND",
                    "Booking not found");
                return;
            }

            bookingDAO.updateStatus(
                bookingId,
                bookingStatus != null ? bookingStatus : existing.getBookingStatus(),
                paymentStatus != null ? paymentStatus : existing.getPaymentStatus());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("bookingId",     bookingId);
            result.put("bookingStatus", bookingStatus);
            result.put("paymentStatus", paymentStatus);
            result.put("message",       "Booking updated successfully");
            ResponseUtil.sendJson(resp, result);

        } catch (NumberFormatException e) {
            ResponseUtil.sendError(resp, 400, "INVALID_ID", "Booking ID must be a number");
        }
    }

    // ── List all users ────────────────────────────────────────────
    private void handleListUsers(HttpServletResponse resp) throws IOException {
        try (java.sql.Connection conn =
                 com.hiddentrails.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT user_id, name, email, phone, role, is_active, created_at FROM users ORDER BY created_at DESC");
             java.sql.ResultSet rs = ps.executeQuery()) {

            List<Map<String, Object>> users = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId",    rs.getInt("user_id"));
                row.put("name",      rs.getString("name"));
                row.put("email",     rs.getString("email"));
                row.put("phone",     rs.getString("phone"));
                row.put("role",      rs.getString("role"));
                row.put("isActive",  rs.getBoolean("is_active"));
                row.put("createdAt", rs.getTimestamp("created_at"));
                users.add(row);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("total", users.size());
            response.put("users", users);
            ResponseUtil.sendJson(resp, response);

        } catch (java.sql.SQLException e) {
            ResponseUtil.sendError(resp, 500, "DB_ERROR", "Could not load users");
        }
    }

    // ── Auth check: only admin role allowed ───────────────────────
    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String role = (String) req.getAttribute("role");
        if (!"admin".equals(role)) {
            ResponseUtil.sendError(resp, 403, "FORBIDDEN",
                "Admin access required");
            return false;
        }
        return true;
    }

    private JsonObject parseBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        }
        try { return JsonParser.parseString(sb.toString()).getAsJsonObject(); }
        catch (Exception e) { return new JsonObject(); }
    }
}