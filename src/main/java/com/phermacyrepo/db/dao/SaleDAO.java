package com.phermacyrepo.db.dao;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.domain.entity.Sale;
import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object for Sale entity.
 * Handles all database operations for sales transactions.
 * 
 * IMPORTANT: This DAO only transfers data. It does NOT:
 * - Calculate totals or prices
 * - Validate business rules
 * - Manipulate stock levels
 */
public class SaleDAO {

    private final DatabaseManager dbManager;

    public SaleDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Save a new sale to the database.
     * Returns the generated ID.
     */
    public int save(Sale sale) {
        try {
            String sql = "INSERT INTO sale (sale_date, total_price, total_quantity) " +
                    "VALUES (?, ?, ?)";

            ResultSet rs = dbManager.executeInsertAndGetKeys(sql,
                    sale.getSaleDate(),
                    sale.getTotalPrice(),
                    sale.getTotalQuantity()
            );

            int generatedId = -1;
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
            rs.close();

            return generatedId;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save sale: " + e.getMessage(), e);
        }
    }

    /**
     * Find a sale by its ID.
     */
    public Optional<Sale> findById(int id) {
        try {
            String sql = "SELECT id, sale_date, total_price, total_quantity FROM sale WHERE id = ?";

            ResultSet rs = dbManager.executeQuery(sql, id);

            if (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                rs.close();
                return Optional.of(sale);
            }

            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sale by id: " + e.getMessage(), e);
        }
    }

    /**
     * Get all sales.
     */
    public List<Sale> findAll() {
        try {
            String sql = "SELECT id, sale_date, total_price, total_quantity FROM sale ORDER BY sale_date DESC";

            ResultSet rs = dbManager.executeQuery(sql);
            List<Sale> sales = new ArrayList<>();

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }

            rs.close();
            return sales;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all sales: " + e.getMessage(), e);
        }
    }

    /**
     * Get sales within a date range.
     */
    public List<Sale> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String sql = "SELECT id, sale_date, total_price, total_quantity FROM sale " +
                    "WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";

            ResultSet rs = dbManager.executeQuery(sql, startDate, endDate);
            List<Sale> sales = new ArrayList<>();

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }

            rs.close();
            return sales;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sales by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Get total sales amount (sum of total_price).
     */
    public double getTotalSalesAmount() {
        try {
            String sql = "SELECT SUM(total_price) as total FROM sale";
            ResultSet rs = dbManager.executeQuery(sql);
            rs.next();
            double total = rs.getDouble("total");
            rs.close();
            return total;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get total sales amount: " + e.getMessage(), e);
        }
    }

    /**
     * Get total sales amount within a date range.
     */
    public double getTotalSalesAmountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String sql = "SELECT SUM(total_price) as total FROM sale " +
                    "WHERE sale_date BETWEEN ? AND ?";
            ResultSet rs = dbManager.executeQuery(sql, startDate, endDate);
            rs.next();
            double total = rs.getDouble("total");
            rs.close();
            return total;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get sales amount by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a sale by ID (also deletes associated sale items due to foreign key cascade).
     */
    public void delete(int id) {
        try {
            String sql = "DELETE FROM sale WHERE id = ?";
            dbManager.executeUpdate(sql, id);
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete sale: " + e.getMessage(), e);
        }
    }

    /**
     * Get count of all sales.
     */
    public int countAll() {
        try {
            String sql = "SELECT COUNT(*) as count FROM sale";
            ResultSet rs = dbManager.executeQuery(sql);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count sales: " + e.getMessage(), e);
        }
    }

    /**
     * Get count of sales within a date range.
     */
    public int countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String sql = "SELECT COUNT(*) as count FROM sale WHERE sale_date BETWEEN ? AND ?";
            ResultSet rs = dbManager.executeQuery(sql, startDate, endDate);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count sales by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Map ResultSet row to Sale object.
     */
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        int saleId = rs.getInt("id");
        LocalDateTime saleDate = rs.getTimestamp("sale_date").toLocalDateTime();

        Sale sale = new Sale(saleId, saleDate);

        return sale;
    }
}
