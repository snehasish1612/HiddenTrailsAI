package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.model.Itinerary;
import com.hiddentrails.model.ItineraryDay;
import com.hiddentrails.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItineraryDAO {

    // ── Save new itinerary ────────────────────────────────────────
    public int save(Itinerary it) {
        String sql = """
            INSERT INTO itineraries
              (user_id, destination, title, start_date, end_date, total_days,
               adults, children, budget, travel_style, accommodation,
               transport_pref, food_pref, pace, entry_point, special_notes,
               ai_prompt, ai_response, estimated_cost, ai_confidence, status)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1,  it.getUserId());
            ps.setString(2,  it.getDestination());
            ps.setString(3,  it.getTitle());
            ps.setDate  (4,  it.getStartDate());
            ps.setDate  (5,  it.getEndDate());
            ps.setInt   (6,  it.getTotalDays());
            ps.setInt   (7,  it.getAdults());
            ps.setInt   (8,  it.getChildren());
            ps.setString(9,  it.getBudget());
            ps.setString(10, it.getTravelStyle());
            ps.setString(11, it.getAccommodation());
            ps.setString(12, it.getTransportPref());
            ps.setString(13, it.getFoodPref());
            ps.setInt   (14, it.getPace());
            ps.setString(15, it.getEntryPoint());
            ps.setString(16, it.getSpecialNotes());
            ps.setString(17, it.getAiPrompt());
            ps.setString(18, it.getAiResponse());
            ps.setBigDecimal(19, it.getEstimatedCost());
            ps.setBigDecimal(20, it.getAiConfidence());
            ps.setString(21, it.getStatus() != null ? it.getStatus() : "draft");

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error saving itinerary", e);
        }
        throw new DAOException("Itinerary insert returned no generated key", null);
    }

    // ── Save all days for an itinerary (bulk insert) ─────────────
    public void saveDays(List<ItineraryDay> days) {
        String sql = """
            INSERT INTO itinerary_days
              (itinerary_id, day_number, day_title, activity,
               location, estimated_cost, hotel_id, transport_id, food_id)
            VALUES (?,?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (ItineraryDay d : days) {
                ps.setInt   (1, d.getItineraryId());
                ps.setInt   (2, d.getDayNumber());
                ps.setString(3, d.getDayTitle());
                ps.setString(4, d.getActivity());
                ps.setString(5, d.getLocation());
                ps.setBigDecimal(6, d.getEstimatedCost());
                setNullableInt(ps, 7, d.getHotelId());
                setNullableInt(ps, 8, d.getTransportId());
                setNullableInt(ps, 9, d.getFoodId());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            throw new DAOException("Error saving itinerary days", e);
        }
    }

    // ── Find by ID (with days) ────────────────────────────────────
    public Itinerary findById(int itineraryId) {
        String sql = "SELECT * FROM itineraries WHERE itinerary_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Itinerary it = mapRow(rs);
                    it.setDays(findDaysByItineraryId(itineraryId));
                    return it;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding itinerary by id", e);
        }
        return null;
    }

    // ── Find all for a user (dashboard) ──────────────────────────
    public List<Itinerary> findByUserId(int userId) {
        String sql = """
            SELECT * FROM itineraries
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;
        List<Itinerary> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding itineraries for user", e);
        }
        return list;
    }

    // ── Find days for one itinerary ───────────────────────────────
    public List<ItineraryDay> findDaysByItineraryId(int itineraryId) {
        String sql = """
            SELECT * FROM itinerary_days
            WHERE itinerary_id = ?
            ORDER BY day_number ASC
            """;
        List<ItineraryDay> days = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) days.add(mapDayRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding itinerary days", e);
        }
        return days;
    }

    // ── Update hotel/transport/food selection for one day ─────────
    public void updateDaySelections(int itineraryId, int dayNumber,
                                    Integer hotelId, Integer transportId,
                                    Integer foodId) {
        String sql = """
            UPDATE itinerary_days
               SET hotel_id = ?, transport_id = ?, food_id = ?
             WHERE itinerary_id = ? AND day_number = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableInt(ps, 1, hotelId);
            setNullableInt(ps, 2, transportId);
            setNullableInt(ps, 3, foodId);
            ps.setInt(4, itineraryId);
            ps.setInt(5, dayNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error updating day selections", e);
        }
    }

    // ── Update estimated cost and status ─────────────────────────
    public void updateCostAndStatus(int itineraryId,
                                    java.math.BigDecimal cost,
                                    String status) {
        String sql = """
            UPDATE itineraries
               SET estimated_cost = ?, status = ?
             WHERE itinerary_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cost);
            ps.setString    (2, status);
            ps.setInt       (3, itineraryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error updating itinerary cost/status", e);
        }
    }

    // ── Update status to booked ───────────────────────────────────
    public void markAsBooked(int itineraryId) {
        String sql = "UPDATE itineraries SET status = 'booked' WHERE itinerary_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error marking itinerary as booked", e);
        }
    }

    // ── Delete (drafts only) ──────────────────────────────────────
    public boolean delete(int itineraryId, int userId) {
        String sql = """
            DELETE FROM itineraries
             WHERE itinerary_id = ? AND user_id = ? AND status = 'draft'
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error deleting itinerary", e);
        }
    }

    // ── Row mappers ───────────────────────────────────────────────
    private Itinerary mapRow(ResultSet rs) throws SQLException {
        Itinerary it = new Itinerary();
        it.setItineraryId (rs.getInt       ("itinerary_id"));
        it.setUserId      (rs.getInt       ("user_id"));
        it.setDestination (rs.getString    ("destination"));
        it.setTitle       (rs.getString    ("title"));
        it.setStartDate   (rs.getDate      ("start_date"));
        it.setEndDate     (rs.getDate      ("end_date"));
        it.setTotalDays   (rs.getInt       ("total_days"));
        it.setAdults      (rs.getInt       ("adults"));
        it.setChildren    (rs.getInt       ("children"));
        it.setBudget      (rs.getString    ("budget"));
        it.setTravelStyle (rs.getString    ("travel_style"));
        it.setAccommodation(rs.getString   ("accommodation"));
        it.setTransportPref(rs.getString   ("transport_pref"));
        it.setFoodPref    (rs.getString    ("food_pref"));
        it.setPace        (rs.getInt       ("pace"));
        it.setEntryPoint  (rs.getString    ("entry_point"));
        it.setSpecialNotes(rs.getString    ("special_notes"));
        it.setEstimatedCost(rs.getBigDecimal("estimated_cost"));
        it.setAiConfidence (rs.getBigDecimal("ai_confidence"));
        it.setStatus      (rs.getString    ("status"));
        it.setCreatedAt   (rs.getTimestamp ("created_at"));
        it.setUpdatedAt   (rs.getTimestamp ("updated_at"));
        return it;
    }

    private ItineraryDay mapDayRow(ResultSet rs) throws SQLException {
        ItineraryDay d = new ItineraryDay();
        d.setDayId       (rs.getInt       ("day_id"));
        d.setItineraryId (rs.getInt       ("itinerary_id"));
        d.setDayNumber   (rs.getInt       ("day_number"));
        d.setDayTitle    (rs.getString    ("day_title"));
        d.setActivity    (rs.getString    ("activity"));
        d.setLocation    (rs.getString    ("location"));
        d.setEstimatedCost(rs.getBigDecimal("estimated_cost"));
        d.setNotes       (rs.getString    ("notes"));
        int hotelId = rs.getInt("hotel_id");
        d.setHotelId(rs.wasNull() ? null : hotelId);
        int transportId = rs.getInt("transport_id");
        d.setTransportId(rs.wasNull() ? null : transportId);
        int foodId = rs.getInt("food_id");
        d.setFoodId(rs.wasNull() ? null : foodId);
        return d;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val)
            throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else             ps.setInt (idx, val);
    }
}