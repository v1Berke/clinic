package com.phermacyrepo.db.dao;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.domain.entity.SaleItem;
import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.sql.*;
import java.util.*;

/**
 * Data Access Object for SaleItem entity.
 * Handles all database operations for individual items within sales.
 * 
 * IMPORTANT: This DAO only transfers data. It does NOT:
 * - Perform price calculations
 * - Validate stock levels
 * - Manipulate sale totals
 */
public class SaleItemDAO {

    private final DatabaseManager dbManager;

    public SaleItemDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Save a new sale item to the database.
     * Returns the generated ID.
     */
    public int save(SaleItem saleItem, int saleId) {
        try {
            String sql = "INSERT INTO sale_item (sale_id, medicine_id, quantity, unit_price) " +
                    "VALUES (?, ?, ?, ?)";

            ResultSet rs = dbManager.executeInsertAndGetKeys(sql,
                    saleId,
                    saleItem.getMedicineId(),
                    saleItem.getQuantity(),
                    saleItem.getUnitPrice()
            );

            int generatedId = -1;
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
            rs.close();

            return generatedId;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save sale item: " + e.getMessage(), e);
        }
    }

    /**
     * Find a sale item by its ID.
     */
    public Optional<SaleItem> findById(int id) {
        try {
            String sql = "SELECT id, sale_id, medicine_id, quantity, unit_price " +
                    "FROM sale_item WHERE id = ?";

            ResultSet rs = dbManager.executeQuery(sql, id);

            if (rs.next()) {
                SaleItem item = mapResultSetToSaleItem(rs);
                rs.close();
                return Optional.of(item);
            }

            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sale item by id: " + e.getMessage(), e);
        }
    }

    /**
     * Get all sale items.
     */
    public List<SaleItem> findAll() {
        try {
            String sql = "SELECT id, sale_id, medicine_id, quantity, unit_price FROM sale_item";

            ResultSet rs = dbManager.executeQuery(sql);
            List<SaleItem> items = new ArrayList<>();

            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }

            rs.close();
            return items;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all sale items: " + e.getMessage(), e);
        }
    }

    /**
     * Get all sale items for a specific sale.
     */
    public List<SaleItem> findBySaleId(int saleId) {
        try {
            String sql = "SELECT id, sale_id, medicine_id, quantity, unit_price " +
                    "FROM sale_item WHERE sale_id = ?";

            ResultSet rs = dbManager.executeQuery(sql, saleId);
            List<SaleItem> items = new ArrayList<>();

            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }

            rs.close();
            return items;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sale items for sale: " + e.getMessage(), e);
        }
    }

    /**
     * Get all sale items for a specific medicine.
     */
    public List<SaleItem> findByMedicineId(int medicineId) {
        try {
            String sql = "SELECT id, sale_id, medicine_id, quantity, unit_price " +
                    "FROM sale_item WHERE medicine_id = ?";

            ResultSet rs = dbManager.executeQuery(sql, medicineId);
            List<SaleItem> items = new ArrayList<>();

            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }

            rs.close();
            return items;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sale items for medicine: " + e.getMessage(), e);
        }
    }

    /**
     * Get total quantity sold for a specific medicine.
     */
    public int getTotalQuantitySoldByMedicine(int medicineId) {
        try {
            String sql = "SELECT SUM(quantity) as total FROM sale_item WHERE medicine_id = ?";

            ResultSet rs = dbManager.executeQuery(sql, medicineId);
            rs.next();
            int total = rs.getInt("total");
            rs.close();

            return total;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to get total quantity sold: " + e.getMessage(), e);
        }
    }

    /**
     * Get total revenue for a specific medicine.
     */
    public double getTotalRevenueByMedicine(int medicineId) {
        try {
            String sql = "SELECT SUM(quantity * unit_price) as total FROM sale_item WHERE medicine_id = ?";

            ResultSet rs = dbManager.executeQuery(sql, medicineId);
            rs.next();
            double total = rs.getDouble("total");
            rs.close();

            return total;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to get total revenue: " + e.getMessage(), e);
        }
    }

    /**
     * Get average unit price for a specific medicine.
     */
    public double getAverageUnitPriceByMedicine(int medicineId) {
        try {
            String sql = "SELECT AVG(unit_price) as average FROM sale_item WHERE medicine_id = ?";

            ResultSet rs = dbManager.executeQuery(sql, medicineId);
            rs.next();
            double average = rs.getDouble("average");
            rs.close();

            return average;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to get average unit price: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a sale item by ID.
     */
    public void delete(int id) {
        try {
            String sql = "DELETE FROM sale_item WHERE id = ?";
            dbManager.executeUpdate(sql, id);
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete sale item: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all sale items for a specific sale.
     */
    public void deleteBySaleId(int saleId) {
        try {
            String sql = "DELETE FROM sale_item WHERE sale_id = ?";
            dbManager.executeUpdate(sql, saleId);
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete sale items: " + e.getMessage(), e);
        }
    }

    /**
     * Get count of all sale items.
     */
    public int countAll() {
        try {
            String sql = "SELECT COUNT(*) as count FROM sale_item";
            ResultSet rs = dbManager.executeQuery(sql);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count sale items: " + e.getMessage(), e);
        }
    }

    /**
     * Get count of sale items for a specific sale.
     */
    public int countBySaleId(int saleId) {
        try {
            String sql = "SELECT COUNT(*) as count FROM sale_item WHERE sale_id = ?";
            ResultSet rs = dbManager.executeQuery(sql, saleId);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count sale items for sale: " + e.getMessage(), e);
        }
    }

    /**
     * Map ResultSet row to SaleItem object.
     */
    private SaleItem mapResultSetToSaleItem(ResultSet rs) throws SQLException {
        int medicineId = rs.getInt("medicine_id");
        int quantity = rs.getInt("quantity");
        double unitPrice = rs.getDouble("unit_price");

        SaleItem item = new SaleItem(medicineId, quantity, unitPrice);

        // Set ID using reflection since setId is package-private
        try {
            java.lang.reflect.Field idField = SaleItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, rs.getInt("id"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SQLException("Failed to set sale item id: " + e.getMessage(), e);
        }

        return item;
    }
}
