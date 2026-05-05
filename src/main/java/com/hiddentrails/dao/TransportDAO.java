package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.model.Transport;
import com.hiddentrails.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportDAO {

    public List<Transport> findByRoute(String from, String to) {
        String sql = """
            SELECT * FROM transport
             WHERE route_from = ? AND route_to = ? AND is_active = 1
            """;
        List<Transport> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding transport by route", e);
        }
        return list;
    }

    public List<Transport> findByOrigin(String from) {
        String sql = "SELECT * FROM transport WHERE route_from = ? AND is_active = 1";
        List<Transport> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding transport by origin", e);
        }
        return list;
    }

    public Transport findById(int transportId) {
        String sql = "SELECT * FROM transport WHERE transport_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding transport by id", e);
        }
        return null;
    }

    private Transport mapRow(ResultSet rs) throws SQLException {
        Transport t = new Transport();
        t.setTransportId    (rs.getInt       ("transport_id"));
        t.setType           (rs.getString    ("type"));
        t.setRouteFrom      (rs.getString    ("route_from"));
        t.setRouteTo        (rs.getString    ("route_to"));
        t.setPricePerPerson (rs.getBigDecimal("price_per_person"));
        t.setTotalPrice     (rs.getBigDecimal("total_price"));
        t.setDurationMinutes(rs.getInt       ("duration_minutes"));
        t.setProvider       (rs.getString    ("provider"));
        t.setActive         (rs.getBoolean   ("is_active"));
        return t;
    }
}