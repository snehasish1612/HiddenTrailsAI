package com.hiddentrails.dao;

import com.hiddentrails.exception.DAOException;
import com.hiddentrails.model.FoodOption;
import com.hiddentrails.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodOptionDAO {

    public List<FoodOption> findByLocation(String location) {
        String sql = "SELECT * FROM food_options WHERE location = ? AND is_active = 1 ORDER BY price_per_person ASC";
        List<FoodOption> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding food options by location", e);
        }
        return list;
    }

    public FoodOption findById(int foodId) {
        String sql = "SELECT * FROM food_options WHERE food_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding food option by id", e);
        }
        return null;
    }

    public List<FoodOption> findAll() {
        String sql = "SELECT * FROM food_options WHERE is_active = 1";
        List<FoodOption> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Error finding all food options", e);
        }
        return list;
    }

    private FoodOption mapRow(ResultSet rs) throws SQLException {
        FoodOption f = new FoodOption();
        f.setFoodId        (rs.getInt       ("food_id"));
        f.setName          (rs.getString    ("name"));
        f.setLocation      (rs.getString    ("location"));
        f.setCuisineType   (rs.getString    ("cuisine_type"));
        f.setPricePerPerson(rs.getBigDecimal("price_per_person"));
        f.setMealType      (rs.getString    ("meal_type"));
        f.setVegetarian    (rs.getBoolean   ("is_vegetarian"));
        f.setImageUrl      (rs.getString    ("image_url"));
        f.setActive        (rs.getBoolean   ("is_active"));
        return f;
    }
}