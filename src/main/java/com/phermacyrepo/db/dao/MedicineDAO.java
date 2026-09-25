package com.phermacyrepo.db.dao;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Data Access Object for Medicine entity.
 * Handles all database operations for medicines.
 * 
 * IMPORTANT: This DAO only transfers data. It does NOT:
 * - Perform calculations
 * - Set prices
 * - Decrease or increase stock (use business logic for that)
 */
public class MedicineDAO {

    private final DatabaseManager dbManager;

    public MedicineDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Save a new medicine to the database.
     * Returns the generated ID.
     */
    public int save(Medicine medicine) {
        try {
            String sql = "INSERT INTO medicine (name, barcode, type, expiration_date, " +
                    "purchase_price, sale_price, stock, active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            ResultSet rs = dbManager.executeInsertAndGetKeys(sql,
                    medicine.getName(),
                    medicine.getBarcode(),
                    medicine.getType().toString(),
                    medicine.getExpirationDate(),
                    medicine.getPurchasePrice(),
                    medicine.getSalePrice(),
                    medicine.getStock(),
                    medicine.isActive() ? 1 : 0
            );

            int generatedId = -1;
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
            rs.close();

            return generatedId;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save medicine: " + e.getMessage(), e);
        }
    }

    
    //Find a medicine by its ID.

    public Optional<Medicine> findById(int id) {
        try {
            String sql = "SELECT id, name, barcode, type, expiration_date, " +
                    "purchase_price, sale_price, stock, active FROM medicine WHERE id = ?";

            ResultSet rs = dbManager.executeQuery(sql, id);

            if (rs.next()) {
                Medicine medicine = mapResultSetToMedicine(rs);
                rs.close();
                return Optional.of(medicine);
            }

            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find medicine by id: " + e.getMessage(), e);
        }
    }

    
    // Find a medicine by its barcode.
    
    public Optional<Medicine> findByBarcode(String barcode) {
        try {
            String sql = "SELECT id, name, barcode, type, expiration_date, " +
                    "purchase_price, sale_price, stock, active FROM medicine WHERE barcode = ?";

            ResultSet rs = dbManager.executeQuery(sql, barcode);

            if (rs.next()) {
                Medicine medicine = mapResultSetToMedicine(rs);
                rs.close();
                return Optional.of(medicine);
            }

            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find medicine by barcode: " + e.getMessage(), e);
        }
    }

    
    //Get all medicines.
    
    public List<Medicine> findAll() {
        try {
            String sql = "SELECT id, name, barcode, type, expiration_date, " +
                    "purchase_price, sale_price, stock, active FROM medicine";

            ResultSet rs = dbManager.executeQuery(sql);
            List<Medicine> medicines = new ArrayList<>();

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }

            rs.close();
            return medicines;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all medicines: " + e.getMessage(), e);
        }
    }

    
    //Get all active medicines.
    
    public List<Medicine> findAllActive() {
        try {
            String sql = "SELECT id, name, barcode, type, expiration_date, " +
                    "purchase_price, sale_price, stock, active FROM medicine WHERE active = 1";

            ResultSet rs = dbManager.executeQuery(sql);
            List<Medicine> medicines = new ArrayList<>();

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }

            rs.close();
            return medicines;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find active medicines: " + e.getMessage(), e);
        }
    }

    /**
     * Update all medicine fields.
     * Service katmani urun bilgisi guncellerken bu metodu kullanir.
     */
    public void updateDetails(Medicine medicine) {
        try {
            String sql = "UPDATE medicine SET name = ?, barcode = ?, type = ?, expiration_date = ?, " +
                    "purchase_price = ?, sale_price = ?, stock = ?, active = ? WHERE id = ?";

            dbManager.executeUpdate(sql,
                    medicine.getName(),
                    medicine.getBarcode(),
                    medicine.getType().toString(),
                    medicine.getExpirationDate(),
                    medicine.getPurchasePrice(),
                    medicine.getSalePrice(),
                    medicine.getStock(),
                    medicine.isActive() ? 1 : 0,
                    medicine.getId()
            );

        } catch (Exception e) {
            throw new DatabaseException("Failed to update medicine details: " + e.getMessage(), e);
        }
    }

    /**
     * Update medicine stock and active status.
     * NOTE: Only updates stock and active status, not other attributes.
     */
    public void update(Medicine medicine) {
        try {
            String sql = "UPDATE medicine SET stock = ?, active = ? WHERE id = ?";

            dbManager.executeUpdate(sql,
                    medicine.getStock(),
                    medicine.isActive() ? 1 : 0,
                    medicine.getId()
            );

        } catch (Exception e) {
            throw new DatabaseException("Failed to update medicine: " + e.getMessage(), e);
        }
    }

    
    //Update medicine stock only.
     
    public void updateStock(int medicineId, int newStock) {
        try {
            String sql = "UPDATE medicine SET stock = ? WHERE id = ?";
            dbManager.executeUpdate(sql, newStock, medicineId);
        } catch (Exception e) {
            throw new DatabaseException("Failed to update medicine stock: " + e.getMessage(), e);
        }
    }

    
    //Deactivate a medicine.
    
    public void deactivate(int medicineId) {
        try {
            String sql = "UPDATE medicine SET active = 0 WHERE id = ?";
            dbManager.executeUpdate(sql, medicineId);
        } catch (Exception e) {
            throw new DatabaseException("Failed to deactivate medicine: " + e.getMessage(), e);
        }
    }

    
    // Delete a medicine by ID.
    
    public void delete(int id) {
        try {
            String sql = "DELETE FROM medicine WHERE id = ?";
            dbManager.executeUpdate(sql, id);
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete medicine: " + e.getMessage(), e);
        }
    }

    
    // Check if a medicine exists by barcode.
    
    public boolean existsByBarcode(String barcode) {
        try {
            String sql = "SELECT COUNT(*) as count FROM medicine WHERE barcode = ?";
            ResultSet rs = dbManager.executeQuery(sql, barcode);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check medicine existence: " + e.getMessage(), e);
        }
    }

    
    //Get count of all medicines.
    
    public int countAll() {
        try {
            String sql = "SELECT COUNT(*) as count FROM medicine";
            ResultSet rs = dbManager.executeQuery(sql);
            rs.next();
            int count = rs.getInt("count");
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count medicines: " + e.getMessage(), e);
        }
    }

    
    // Map ResultSet row to Medicine object.
     
    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String barcode = rs.getString("barcode");
        String typeStr = rs.getString("type");
        LocalDate expirationDate = rs.getDate("expiration_date").toLocalDate();
        double purchasePrice = rs.getDouble("purchase_price");
        double salePrice = rs.getDouble("sale_price");
        int stock = rs.getInt("stock");

        MedicineType type = MedicineType.valueOf(typeStr);
        Medicine medicine = new Medicine(
                name, barcode, type, expirationDate,
                purchasePrice, salePrice, stock
        );
        medicine.setId(rs.getInt("id"));
        medicine.setActive(rs.getInt("active") == 1);

        return medicine;
    }
}
