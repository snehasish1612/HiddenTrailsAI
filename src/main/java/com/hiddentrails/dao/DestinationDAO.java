package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.util.DBConnection;

import java.sql.*;
import java.util.*;

public class DestinationDAO {

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM destinations WHERE is_active = 1 ORDER BY name ASC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("destinationId", rs.getInt("destination_id"));
                row.put("name",          rs.getString("name"));
                row.put("region",        rs.getString("region"));
                row.put("description",   rs.getString("description"));
                row.put("imageUrl",      rs.getString("image_url"));
                list.add(row);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding destinations", e);
        }
        return list;
    }
}