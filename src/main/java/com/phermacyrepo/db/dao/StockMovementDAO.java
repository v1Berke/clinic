package com.phermacyrepo.db.dao;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.domain.entity.StockMovement;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object for StockMovement entity.
 * Handles all database operations for stock movements.
 * 
 * IMPORTANT: This DAO only transfers data. It does NOT:
 * - Perform calculations
 * - Decrease or increase stock in medicine table
 * - Make business decisions
 */
public class StockMovementDAO {

    private final DatabaseManager dbManager;

    public StockMovementDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Save a new stock movement to the database.
     * Returns the generated ID.
     */
    public int save(StockMovement stockMovement) {
        try {
            String sql = "INSERT INTO stock_movement (medicine_id, type, quantity, date, reason) " +
                    "VALUES (?, ?, ?, ?, ?)";

            ResultSet rs = dbManager.executeInsertAndGetKeys(sql,
                    stockMovement.getMedicineId(),
                    stockMovement.getType().toString(),
                    stockMovement.getQuantity(),
                    stockMovement.getDate(),
                    stockMovement.getReason()
            );

            int generatedId = -1;
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
            rs.close();

            return generatedId;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save stock movement: " + e.getMessage(), e);
        }
    }

    /**
     * Find a stock movement by its ID.
     */
    public Optional<StockMovement> findById(int id) {
        try {
            String sql = "SELECT id, medicine_id, type, quantity, date, reason " +
                    "FROM stock_movement WHERE id = ?";

            ResultSet rs = dbManager.executeQuery(sql, id);

            if (rs.next()) {
                StockMovement movement = mapResultSetToStockMovement(rs);
                rs.close();
                return Optional.of(movement);
            }

            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find stock movement by id: " + e.getMessage(), e);
        }
    }

    /**
     * Get all stock movements.
     */
    public List<StockMovement> findAll() {
        try {
            String sql = "SELECT id, medicine_id, type, quantity, date, reason " +
                    "FROM stock_movement ORDER BY date DESC";

            ResultSet rs = dbManager.executeQuery(sql);
            List<StockMovement> movements = new ArrayList<>();

            while (rs.next()) {
                movements.add(mapResultSetToStockMovement(rs));
            }

            rs.close();
            return movements;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all stock movements: " + e.getMessage(), e);
        }
    }

    /**
     * Get all stock movements for a specific medicine.
     */
    public List<StockMovement> findByMedicineId(int medicineId) {
        try {
            String sql = "SELECT id, medicine_id, type, quantity, date, reason " +
                    "FROM stock_movement WHERE medicine_id = ? ORDER BY date DESC";

            ResultSet rs = dbManager.executeQuery(sql, medicineId);
            List<StockMovement> movements = new ArrayList<>();

            while (rs.next()) {
                movements.add(mapResultSetToStockMovement(rs));
            }

            rs.close();
            return movements;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find stock movements for medicine: " + e.getMessage(), e);
        }
    }

    /**
     * Get stock movements by type (PURCHASE, SALE, ADJUSTMENT, LOSS).
     */
    public List<StockMovement> findByType(StockMovementType type) {
        try {
            String sql = "SELECT id, medicine_id, type, quantity, date, reason " +
                    "FROM stock_movement WHERE type = ? ORDER BY date DESC";

            ResultSet rs = dbManager.executeQuery(sql, type.toString());
            List<StockMovement> movements = new ArrayList<>();

            while (rs.next()) {
                movements.add(mapResultSetToStockMovement(rs));
            }

            rs.close();
            return movements;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find stock movements by type: " + e.getMessage(), e);
        }
    }

    /**
     * Get stock movements for a medicine within a date range.
     */
    public List<StockMovement> findByMedicineIdAndDateRange(int medicineId, 
            LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String sql = "SELECT id, medicine_id, type, quantity, date, reason " +
                    "FROM stock_movement WHERE medicine_id = ? AND date BETWEEN ? AND ? " +
                    "ORDER BY date DESC";

            ResultSet rs = dbManager.executeQuery(sql, medicineId, startDate, endDate);
            List<StockMovement> movements = new ArrayList<>();

            while (rs.next()) {
                movements.add(mapResultSetToStockMovement(rs));
            }

            rs.close();
            return movements;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find stock movements by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Get total quantity moved for a specific medicine.
     */
    public int getTotalQuantityByMedicineAndType(int medicineId, StockMovementType type) {
        try {
            String sql = "SELECT SUM(quantity) as total FROM stock_movement " +
                    "WHERE medicine_id = ? AND type = ?";

            ResultSet rs = dbManager.executeQuery(sql, medicineId, type.toString());
            rs.next();
            int total = rs.getInt("total");
            rs.close();

            return total;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to get total quantity: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a stock movement by ID.
     */
    public void delete(int id) {
        try {
            String sql = "DELETE FROM stock_movement WHERE id = ?";
            dbManager.executeUpdate(sql, id);
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete stock movement: " + e.getMessage(), e);
        }
    }

    /**
     * Get count of all stock movements.
     */
    public int countAll() {
        try {
            String sql = "SELECT COUNT(*) as count FROM stock_movement";
            ResultSet rs = dbManager.executeQuery(sql);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count stock movements: " + e.getMessage(), e);
        }
    }

    /**
     * Map ResultSet row to StockMovement object.
     */
    private StockMovement mapResultSetToStockMovement(ResultSet rs) throws SQLException {
        int medicineId = rs.getInt("medicine_id");
        String typeStr = rs.getString("type");
        int quantity = rs.getInt("quantity");
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        String reason = rs.getString("reason");

        StockMovementType type = StockMovementType.valueOf(typeStr);
        StockMovement movement = new StockMovement(
                medicineId, type, quantity, date, reason
        );

        // Set ID using reflection since setId is package-private
        try {
            java.lang.reflect.Field idField = StockMovement.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(movement, rs.getInt("id"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SQLException("Failed to set stock movement id: " + e.getMessage(), e);
        }

        return movement;
    }
}
