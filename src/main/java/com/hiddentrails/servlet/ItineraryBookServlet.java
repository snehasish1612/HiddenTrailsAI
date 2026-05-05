package com.hiddentrails.servlet;

import com.google.gson.*;
import com.hiddentrails.dao.*;
import com.hiddentrails.model.*;
import com.hiddentrails.service.EmailService;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ItineraryBookServlet — POST /api/itinerary/{id}/book
 *
 * Creates a Booking record after the user completes payment.
 * Request: { paymentMethod, paymentToken, totalAmount }
 * Response:{ bookingId, itineraryId, status, paymentStatus, confirmationNo }
 */
@WebServlet("/api/itinerary/*/book")
public class ItineraryBookServlet extends HttpServlet {

    private static final Logger LOGGER =
        Logger.getLogger(ItineraryBookServlet.class.getName());

    private final ItineraryDAO itineraryDAO = new ItineraryDAO();
    private final BookingDAO   bookingDAO   = new BookingDAO();
    private final UserDAO      userDAO      = new UserDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int itineraryId = extractItineraryId(req, resp);
        if (itineraryId < 0) return;

        int userId = (int) req.getAttribute("userId");

        // Verify itinerary exists and belongs to user
        Itinerary it = itineraryDAO.findById(itineraryId);
        if (it == null) {
            ResponseUtil.sendError(resp, 404, "NOT_FOUND", "Itinerary not found");
            return;
        }
        if (it.getUserId() != userId) {
            ResponseUtil.sendError(resp, 403, "FORBIDDEN",
                "You do not have access to this itinerary");
            return;
        }
        if ("booked".equals(it.getStatus())) {
            ResponseUtil.sendError(resp, 400, "ALREADY_BOOKED",
                "This itinerary has already been booked");
            return;
        }

        // Parse body
        JsonObject body = parseBody(req);
        String paymentMethod = getString(body, "paymentMethod", "razorpay");
        String paymentToken  = getString(body, "paymentToken",  "");
        BigDecimal amount    = it.getEstimatedCost();
        if (body.has("totalAmount") && !body.get("totalAmount").isJsonNull()) {
            amount = body.get("totalAmount").getAsBigDecimal();
        }

        try {
            // Generate unique confirmation number: HT-YYYY-{bookingId padded}
            // We pre-build a temp one; update after insert
            Booking booking = new Booking();
            booking.setUserId       (userId);
            booking.setItineraryId  (itineraryId);
            booking.setTotalPrice   (amount);
            booking.setPaymentMethod(paymentMethod);
            booking.setPaymentToken (paymentToken);
            booking.setPaymentStatus("paid");       // Assume verified by gateway
            booking.setBookingStatus("confirmed");

            // Temp confirmation number (will be updated with real bookingId)
            booking.setConfirmationNo("HT-TEMP-" + System.currentTimeMillis());

            int bookingId = bookingDAO.save(booking);
            booking.setBookingId(bookingId);

            // Update confirmation number with actual booking ID
            String confirmationNo = "HT-"
                + java.time.Year.now().getValue()
                + "-"
                + String.format("%04d", bookingId);
            bookingDAO.updateStatus(bookingId, "confirmed", "paid");

            // Update the confirmation no directly
            updateConfirmationNo(bookingId, confirmationNo);
            booking.setConfirmationNo(confirmationNo);

            // Mark itinerary as booked
            itineraryDAO.markAsBooked(itineraryId);

            // Send confirmation email (non-fatal if it fails)
            try {
                User user = userDAO.findById(userId);
                if (user != null) {
                    emailService.sendBookingConfirmation(
                        user.getEmail(),
                        user.getName(),
                        booking,
                        it.getDestination(),
                        it.getStartDate().toString(),
                        it.getEndDate().toString());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Email send failed (non-fatal)", e);
            }

            // Build response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("bookingId",      bookingId);
            response.put("itineraryId",    itineraryId);
            response.put("status",         "confirmed");
            response.put("paymentStatus",  "paid");
            response.put("confirmationNo", confirmationNo);
            response.put("totalAmount",    amount);
            response.put("message",        "Booking confirmed! Check your email.");
            ResponseUtil.sendJson(resp, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Booking error", e);
            ResponseUtil.sendError(resp, 500, "BOOKING_ERROR",
                "Booking could not be completed. Please try again.");
        }
    }

    private void updateConfirmationNo(int bookingId, String confirmationNo) {
        try (java.sql.Connection conn =
                 com.hiddentrails.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "UPDATE bookings SET confirmation_no = ? WHERE booking_id = ?")) {
            ps.setString(1, confirmationNo);
            ps.setInt   (2, bookingId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            LOGGER.log(Level.WARNING, "Could not update confirmation number", e);
        }
    }

    private int extractItineraryId(HttpServletRequest req,
                                    HttpServletResponse resp)
            throws IOException {
        String[] parts = req.getRequestURI().split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("itinerary".equals(parts[i]) && i + 1 < parts.length) {
                try { return Integer.parseInt(parts[i + 1]); }
                catch (NumberFormatException ignored) { break; }
            }
        }
        ResponseUtil.sendError(resp, 400, "INVALID_ID", "Cannot extract itinerary ID");
        return -1;
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

    private String getString(JsonObject body, String key, String defaultVal) {
        if (body.has(key) && !body.get(key).isJsonNull())
            return body.get(key).getAsString();
        return defaultVal;
    }
}