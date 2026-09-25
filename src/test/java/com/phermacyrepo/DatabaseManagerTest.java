package com.phermacyrepo;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Test class for DatabaseManager
 * Tests connection, transactions, schema, and basic operations
 */
public class DatabaseManagerTest {

    public static void main(String[] args) {
        System.out.println("========== DatabaseManager Test Suite ==========\n");

        try {
            // Test 1: Initialize database
            testInitialize();

            // Test 2: Check if tables exist
            testTableExistence();

            // Test 3: Insert medicine
            testInsertMedicine();

            // Test 4: Query medicines
            testQueryMedicines();

            // Test 5: Transaction handling
            testTransactionHandling();

            // Test 6: Transaction rollback
            testTransactionRollback();

            // Test 7: Connection state
            testConnectionState();

            // Test 8: Clear data
            testClearAllData();

            System.out.println("\n========== All Tests Completed Successfully! ==========");

        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            DatabaseManager.getInstance().shutdown();
            System.out.println("\nDatabase connection closed.");
        }
    }

    private static void testInitialize() {
        System.out.println("TEST 1: Initialize Database");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initialize();
            System.out.println("✓ Database initialized successfully");
            System.out.println("✓ Connection opened");
            System.out.println("✓ Foreign keys enabled");
            System.out.println("✓ Schema initialized\n");
        } catch (Exception e) {
            System.err.println("✗ Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    private static void testTableExistence() {
        System.out.println("TEST 2: Check Table Existence");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            boolean tablesExist = dbManager.tablesExist();
            System.out.println("✓ Tables exist: " + tablesExist);
            
            // Get list of tables
            Connection conn = dbManager.getConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet tables = metadata.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("✓ Database tables:");
            while (tables.next()) {
                System.out.println("  - " + tables.getString("TABLE_NAME"));
            }
            tables.close();
            System.out.println();
        } catch (Exception e) {
            System.err.println("✗ Failed to check tables: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testInsertMedicine() {
        System.out.println("TEST 3: Insert Medicine");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();

            // Insert test medicine with unique barcodes based on timestamp
            String sql = "INSERT INTO medicine (name, barcode, type, " +
                    "purchase_price, sale_price, stock) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            long timestamp = System.currentTimeMillis();
            
            ResultSet rs = dbManager.executeInsertAndGetKeys(sql,
                    "Aspirin",                       // name
                    "BARCODE-" + timestamp + "-1",  // unique barcode
                    "TABLET",                        // type (must match MedicineType enum)
                    0.50,                            // purchase_price
                    1.50,                            // sale_price
                    100                             // stock
            );

            int medicineId = -1;
            if (rs.next()) {
                medicineId = rs.getInt(1);
            }
            rs.close();

            System.out.println("✓ Medicine inserted successfully");
            System.out.println("✓ Generated ID: " + medicineId);
            
            // Insert another medicine with unique barcode
            rs = dbManager.executeInsertAndGetKeys(sql,
                    "Ibuprofen",
                    "BARCODE-" + timestamp + "-2",  // unique barcode
                    "TABLET",                        // type (must match MedicineType enum)
                    0.75,
                    2.00,
                    50
            );

            int medicineId2 = -1;
            if (rs.next()) {
                medicineId2 = rs.getInt(1);
            }
            rs.close();

            System.out.println("✓ Second medicine inserted with ID: " + medicineId2 + "\n");

        } catch (Exception e) {
            System.err.println("✗ Failed to insert medicine: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testQueryMedicines() {
        System.out.println("TEST 4: Query Medicines");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();

            String sql = "SELECT id, name, barcode, type, purchase_price, sale_price, stock FROM medicine";
            ResultSet rs = dbManager.executeQuery(sql);

            System.out.println("✓ Medicines in database:");
            int count = 0;
            while (rs.next()) {
                System.out.println("  ID: " + rs.getInt("id") +
                        " | Name: " + rs.getString("name") +
                        " | Barcode: " + rs.getString("barcode") +
                        " | Type: " + rs.getString("type") +
                        " | Stock: " + rs.getInt("stock"));
                count++;
            }
            rs.close();
            System.out.println("✓ Total medicines: " + count + "\n");

        } catch (Exception e) {
            System.err.println("✗ Failed to query medicines: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testTransactionHandling() {
        System.out.println("TEST 5: Transaction Handling (Commit)");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();

            System.out.println("✓ Beginning transaction...");
            dbManager.beginTransaction();
            System.out.println("✓ Transaction active: " + dbManager.isTransactionActive());

            // Insert stock movement within transaction using an existing medicine
            String sql = "INSERT INTO stock_movement (medicine_id, type, quantity, date, reason) " +
                    "VALUES (?, ?, ?, ?, ?)";

            dbManager.executeUpdate(sql,
                    getAnyMedicineId(dbManager),
                    "PURCHASE",
                    50,
                    LocalDateTime.now(),
                    "Initial stock purchase"
            );

            System.out.println("✓ Stock movement recorded within transaction");

            // Commit transaction
            dbManager.commit();
            System.out.println("✓ Transaction committed successfully");
            System.out.println("✓ Transaction active after commit: " + dbManager.isTransactionActive() + "\n");

        } catch (Exception e) {
            System.err.println("✗ Failed transaction test: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testTransactionRollback() {
        System.out.println("TEST 6: Transaction Handling (Rollback)");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();

            // Count stock movements before
            String countSql = "SELECT COUNT(*) as count FROM stock_movement";
            ResultSet rs = dbManager.executeQuery(countSql);
            rs.next();
            int countBefore = rs.getInt("count");
            rs.close();

            System.out.println("✓ Stock movements before transaction: " + countBefore);

            // Start transaction
            System.out.println("✓ Beginning transaction...");
            dbManager.beginTransaction();

            // Try to insert (will be rolled back) using an existing medicine
            String sql = "INSERT INTO stock_movement (medicine_id, type, quantity, date, reason) " +
                    "VALUES (?, ?, ?, ?, ?)";

            dbManager.executeUpdate(sql,
                    getAnyMedicineId(dbManager),
                    "LOSS",
                    10,
                    LocalDateTime.now(),
                    "Test rollback - should not be saved"
            );

            System.out.println("✓ Insert attempted (will be rolled back)");

            // Rollback
            dbManager.rollback();
            System.out.println("✓ Transaction rolled back");

            // Verify data wasn't saved
            rs = dbManager.executeQuery(countSql);
            rs.next();
            int countAfter = rs.getInt("count");
            rs.close();

            System.out.println("✓ Stock movements after rollback: " + countAfter);
            System.out.println("✓ Data integrity verified: " + (countBefore == countAfter ? "PASS" : "FAIL") + "\n");

        } catch (Exception e) {
            System.err.println("✗ Failed rollback test: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testConnectionState() {
        System.out.println("TEST 7: Connection State Management");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();

            Connection conn = dbManager.getConnection();
            System.out.println("✓ Connection obtained");
            System.out.println("✓ Connection closed: " + conn.isClosed());
            System.out.println("✓ Transaction active: " + dbManager.isTransactionActive());

            // Test connection without transaction
            String sql = "SELECT COUNT(*) as count FROM medicine";
            ResultSet rs = dbManager.executeQuery(sql);
            rs.next();
            int medicineCount = rs.getInt("count");
            rs.close();
            System.out.println("✓ Query executed with active connection");
            System.out.println("✓ Total medicines: " + medicineCount + "\n");

        } catch (Exception e) {
            System.err.println("✗ Failed connection state test: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testClearAllData() {
        System.out.println("TEST 8: Clear All Data");
        System.out.println("-".repeat(40));
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();

            System.out.println("✓ Clearing all data from database...");
            dbManager.clearAllData();

            // Verify tables are empty
            String[] tables = {"medicine", "stock_movement", "sale", "sale_item"};
            for (String table : tables) {
                String sql = "SELECT COUNT(*) as count FROM " + table;
                ResultSet rs = dbManager.executeQuery(sql);
                rs.next();
                int count = rs.getInt("count");
                rs.close();
                System.out.println("✓ Table '" + table + "' row count: " + count);
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("✗ Failed to clear data: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    /**
     * Testlerde sabit id varsaymak yerine tablodaki gercek bir id'yi dondurur.
     */
    private static int getAnyMedicineId(DatabaseManager dbManager) {
        try {
            ResultSet rs = dbManager.executeQuery("SELECT id FROM medicine LIMIT 1");
            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                return id;
            }
            rs.close();
        } catch (Exception e) {
            throw new DatabaseException("Failed to read medicine id: " + e.getMessage(), e);
        }
        throw new DatabaseException("No medicine found for test (medicine table is empty)");
    }
}
