package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.model.Booking;
import com.hiddentrails.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public int save(Booking booking) {
        String sql = """
            INSERT INTO bookings
              (user_id, itinerary_id, total_price, payment_method,
               payment_token, payment_status, booking_status, confirmation_no)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt       (1, booking.getUserId());
            ps.setInt       (2, booking.getItineraryId());
            ps.setBigDecimal(3, booking.getTotalPrice());
            ps.setString    (4, booking.getPaymentMethod());
            ps.setString    (5, booking.getPaymentToken());
            ps.setString    (6, booking.getPaymentStatus() != null
                                ? booking.getPaymentStatus() : "pending");
            ps.setString    (7, booking.getBookingStatus() != null
                                ? booking.getBookingStatus() : "pending");
            ps.setString    (8, booking.getConfirmationNo());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error saving booking", e);
        }
        throw new DAOException("Booking insert returned no generated key", null);
    }

    public Booking findById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding booking by id", e);
        }
        return null;
    }

    public List<Booking> findByUserId(int userId) {
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY booked_at DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding bookings for user", e);
        }
        return list;
    }

    public List<Booking> findAll() {
        String sql = "SELECT * FROM bookings ORDER BY booked_at DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Error finding all bookings", e);
        }
        return list;
    }

    public void updateStatus(int bookingId, String bookingStatus, String paymentStatus) {
        String sql = """
            UPDATE bookings
               SET booking_status = ?, payment_status = ?
             WHERE booking_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingStatus);
            ps.setString(2, paymentStatus);
            ps.setInt   (3, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error updating booking status", e);
        }
    }

    public Booking findByConfirmationNo(String confirmationNo) {
        String sql = "SELECT * FROM bookings WHERE confirmation_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confirmationNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding booking by confirmation number", e);
        }
        return null;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId     (rs.getInt       ("booking_id"));
        b.setUserId        (rs.getInt       ("user_id"));
        b.setItineraryId   (rs.getInt       ("itinerary_id"));
        b.setTotalPrice    (rs.getBigDecimal("total_price"));
        b.setPaymentMethod (rs.getString    ("payment_method"));
        b.setPaymentToken  (rs.getString    ("payment_token"));
        b.setPaymentStatus (rs.getString    ("payment_status"));
        b.setBookingStatus (rs.getString    ("booking_status"));
        b.setConfirmationNo(rs.getString    ("confirmation_no"));
        b.setBookedAt      (rs.getTimestamp ("booked_at"));
        b.setUpdatedAt     (rs.getTimestamp ("updated_at"));
        return b;
    }
}