package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.model.ItineraryDay;
import com.hiddentrails.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ItineraryDayDAO — all database operations for the itinerary_days table.
 *
 * Separated from ItineraryDAO so each class has a single responsibility.
 * ItineraryDAO delegates day-level operations to this class.
 *
 * Used by:
 *   - ItineraryGenerateServlet  (saveAll after AI generation)
 *   - ItineraryUpdateServlet    (updateSelections on option change)
 *   - PricingService            (findByItineraryId for cost recalc)
 */
public class ItineraryDayDAO {

    // ── Bulk insert all days for a new itinerary ─────────────────
    /**
     * Inserts all days in a single batch statement.
     * Called once after AI generation and initial pricing enrichment.
     *
     * @param days  list of ItineraryDay objects (itineraryId must be set on each)
     */
    public void saveAll(List<ItineraryDay> days) {
        if (days == null || days.isEmpty()) return;

        String sql = """
            INSERT INTO itinerary_days
              (itinerary_id, day_number, day_title, activity,
               location, estimated_cost, hotel_id, transport_id, food_id, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (ItineraryDay d : days) {
                ps.setInt       (1,  d.getItineraryId());
                ps.setInt       (2,  d.getDayNumber());
                ps.setString    (3,  d.getDayTitle());
                ps.setString    (4,  d.getActivity());
                ps.setString    (5,  d.getLocation());
                ps.setBigDecimal(6,  d.getEstimatedCost() != null
                                     ? d.getEstimatedCost() : BigDecimal.ZERO);
                setNullableInt  (ps, 7,  d.getHotelId());
                setNullableInt  (ps, 8,  d.getTransportId());
                setNullableInt  (ps, 9,  d.getFoodId());
                ps.setString    (10, d.getNotes());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            throw new DAOException("Error batch-inserting itinerary days", e);
        }
    }

    // ── Find all days for an itinerary (ordered by day_number) ───
    /**
     * Returns all days for a given itinerary, ordered by day number.
     * Does NOT populate option lists (hotelOptions, etc.) — that is
     * done by PricingService after fetching from the DAO.
     */
    public List<ItineraryDay> findByItineraryId(int itineraryId) {
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
                while (rs.next()) days.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding days for itinerary " + itineraryId, e);
        }
        return days;
    }

    // ── Find a single day by its ID ───────────────────────────────
    public ItineraryDay findById(int dayId) {
        String sql = "SELECT * FROM itinerary_days WHERE day_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding day by id " + dayId, e);
        }
        return null;
    }

    // ── Find a specific day number within an itinerary ────────────
    public ItineraryDay findByDayNumber(int itineraryId, int dayNumber) {
        String sql = """
            SELECT * FROM itinerary_days
             WHERE itinerary_id = ? AND day_number = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            ps.setInt(2, dayNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding day " + dayNumber, e);
        }
        return null;
    }

    // ── Update hotel/transport/food selection for one day ─────────
    /**
     * Called when the user changes an option on the itinerary result page.
     * Also updates the estimated_cost for that day.
     *
     * @param dayId         the day_id to update
     * @param hotelId       selected hotel FK (nullable)
     * @param transportId   selected transport FK (nullable)
     * @param foodId        selected food FK (nullable)
     * @param estimatedCost recalculated cost for the day
     */
    public void updateOptions(int dayId,
                               Integer hotelId,
                               Integer transportId,
                               Integer foodId,
                               BigDecimal estimatedCost) {
        String sql = """
            UPDATE itinerary_days
               SET hotel_id = ?,
                   transport_id = ?,
                   food_id = ?,
                   estimated_cost = ?
             WHERE day_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setNullableInt  (ps, 1, hotelId);
            setNullableInt  (ps, 2, transportId);
            setNullableInt  (ps, 3, foodId);
            ps.setBigDecimal(4, estimatedCost != null
                                ? estimatedCost : BigDecimal.ZERO);
            ps.setInt       (5, dayId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Error updating options for day " + dayId, e);
        }
    }

    // ── Update by itinerary_id + day_number (used by UpdateServlet) ─
    /**
     * Same as updateOptions() but identified by itinerary+dayNumber
     * instead of dayId — matches the API contract where the client
     * sends dayNumber, not dayId.
     */
    public void updateOptionsByDayNumber(int itineraryId,
                                          int dayNumber,
                                          Integer hotelId,
                                          Integer transportId,
                                          Integer foodId,
                                          BigDecimal estimatedCost) {
        String sql = """
            UPDATE itinerary_days
               SET hotel_id = ?,
                   transport_id = ?,
                   food_id = ?,
                   estimated_cost = ?
             WHERE itinerary_id = ? AND day_number = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setNullableInt  (ps, 1, hotelId);
            setNullableInt  (ps, 2, transportId);
            setNullableInt  (ps, 3, foodId);
            ps.setBigDecimal(4, estimatedCost != null
                                ? estimatedCost : BigDecimal.ZERO);
            ps.setInt       (5, itineraryId);
            ps.setInt       (6, dayNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException(
                "Error updating day " + dayNumber + " for itinerary " + itineraryId, e);
        }
    }

    // ── Recalculate and update estimated_cost for a single day ────
    public void updateEstimatedCost(int dayId, BigDecimal cost) {
        String sql = "UPDATE itinerary_days SET estimated_cost = ? WHERE day_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cost);
            ps.setInt       (2, dayId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error updating cost for day " + dayId, e);
        }
    }

    // ── Delete all days for an itinerary (called before re-generate) ─
    public void deleteByItineraryId(int itineraryId) {
        String sql = "DELETE FROM itinerary_days WHERE itinerary_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(
                "Error deleting days for itinerary " + itineraryId, e);
        }
    }

    // ── Sum estimated_cost across all days of an itinerary ────────
    /**
     * Returns the total estimated cost by summing all day costs.
     * Used after options are updated to refresh the itinerary total.
     */
    public BigDecimal sumTotalCost(int itineraryId) {
        String sql = """
            SELECT COALESCE(SUM(estimated_cost), 0)
              FROM itinerary_days
             WHERE itinerary_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itineraryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DAOException(
                "Error summing cost for itinerary " + itineraryId, e);
        }
        return BigDecimal.ZERO;
    }

    // ── Row mapper ────────────────────────────────────────────────
    private ItineraryDay mapRow(ResultSet rs) throws SQLException {
        ItineraryDay d = new ItineraryDay();
        d.setDayId        (rs.getInt        ("day_id"));
        d.setItineraryId  (rs.getInt        ("itinerary_id"));
        d.setDayNumber    (rs.getInt        ("day_number"));
        d.setDayTitle     (rs.getString     ("day_title"));
        d.setActivity     (rs.getString     ("activity"));
        d.setLocation     (rs.getString     ("location"));
        d.setEstimatedCost(rs.getBigDecimal ("estimated_cost"));
        d.setNotes        (rs.getString     ("notes"));

        int hotelId = rs.getInt("hotel_id");
        d.setHotelId(rs.wasNull() ? null : hotelId);

        int transportId = rs.getInt("transport_id");
        d.setTransportId(rs.wasNull() ? null : transportId);

        int foodId = rs.getInt("food_id");
        d.setFoodId(rs.wasNull() ? null : foodId);

        return d;
    }

    // ── Helper: set INT or NULL ───────────────────────────────────
    private void setNullableInt(PreparedStatement ps, int idx, Integer val)
            throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else             ps.setInt (idx, val);
    }
}