package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.model.Hotel;
import com.hiddentrails.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    public List<Hotel> findByLocation(String location) {
        String sql = "SELECT * FROM hotels WHERE location = ? AND is_active = 1 ORDER BY rating DESC";
        List<Hotel> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding hotels by location", e);
        }
        return list;
    }

    public Hotel findById(int hotelId) {
        String sql = "SELECT * FROM hotels WHERE hotel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hotelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding hotel by id", e);
        }
        return null;
    }

    private Hotel mapRow(ResultSet rs) throws SQLException {
        Hotel h = new Hotel();
        h.setHotelId      (rs.getInt       ("hotel_id"));
        h.setName         (rs.getString    ("name"));
        h.setLocation     (rs.getString    ("location"));
        h.setAddress      (rs.getString    ("address"));
        h.setPricePerNight(rs.getBigDecimal("price_per_night"));
        h.setRating       (rs.getInt       ("rating"));
        h.setCategory     (rs.getString    ("category"));
        h.setAmenities    (rs.getString    ("amenities"));
        h.setImageUrl     (rs.getString    ("image_url"));
        h.setActive       (rs.getBoolean   ("is_active"));
        return h;
    }
}