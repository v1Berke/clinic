package com.phermacyrepo;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.*;
import com.phermacyrepo.domain.entity.*;
import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.time.LocalDateTime;

/**
 * Test class for DAO Layer
 * Tests all DAO operations: save, find, update, delete
 */
public class DAOLayerTest {

    public static void main(String[] args) {
        System.out.println("========== DAO Layer Test Suite ==========\n");

        try {
            // Initialize database
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initialize();

            // Create DAO instances
            MedicineDAO medicineDAO = new MedicineDAO(dbManager);
            StockMovementDAO stockMovementDAO = new StockMovementDAO(dbManager);
            SaleDAO saleDAO = new SaleDAO(dbManager);
            SaleItemDAO saleItemDAO = new SaleItemDAO(dbManager);

            // Test 1: MedicineDAO
            testMedicineDAO(medicineDAO);

            // Test 2: StockMovementDAO
            testStockMovementDAO(stockMovementDAO);

            // Test 3: SaleDAO and SaleItemDAO
            testSaleDAO(saleDAO, saleItemDAO, medicineDAO);

            System.out.println("\n========== All DAO Tests Completed Successfully! ==========");

        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.getInstance().shutdown();
        }
    }

    private static void testMedicineDAO(MedicineDAO medicineDAO) {
        System.out.println("TEST 1: MedicineDAO Operations");
        System.out.println("-".repeat(50));

        try {
            long timestamp = System.currentTimeMillis();

            // Test save
            Medicine med1 = new Medicine(
                    "Aspirin",
                    "BAR-ASP-" + timestamp,
                    MedicineType.TABLET,
                    0.50,
                    1.50,
                    100
            );

            int medId1 = medicineDAO.save(med1);
            System.out.println("✓ Medicine saved with ID: " + medId1);

            // Test findById
            var foundMed = medicineDAO.findById(medId1);
            if (foundMed.isPresent()) {
                System.out.println("✓ Medicine found by ID: " + foundMed.get().getName());
            }

            // Test findByBarcode
            var foundByBarcode = medicineDAO.findByBarcode("BAR-ASP-" + timestamp);
            if (foundByBarcode.isPresent()) {
                System.out.println("✓ Medicine found by barcode: " + foundByBarcode.get().getBarcode());
            }

            // Save another medicine
            Medicine med2 = new Medicine(
                    "Ibuprofen",
                    "BAR-IBU-" + timestamp,
                    MedicineType.TABLET,
                    0.75,
                    2.00,
                    50
            );
            int medId2 = medicineDAO.save(med2);

            // Test findAll
            var allMedicines = medicineDAO.findAll();
            System.out.println("✓ Total medicines in database: " + allMedicines.size());

            // Test countAll
            int count = medicineDAO.countAll();
            System.out.println("✓ Medicine count: " + count);

            // Test existsByBarcode
            boolean exists = medicineDAO.existsByBarcode("BAR-ASP-" + timestamp);
            System.out.println("✓ Barcode exists: " + exists);

            // Test updateStock
            medicineDAO.updateStock(medId1, 75);
            var updatedMed = medicineDAO.findById(medId1);
            System.out.println("✓ Stock updated to: " + updatedMed.get().getStock());

            // Test delete
            Medicine med3 = new Medicine(
                    "Vitamin",
                    "BAR-VIT-" + timestamp,
                    MedicineType.TABLET,
                    1.00,
                    2.50,
                    20
            );
            int medId3 = medicineDAO.save(med3);
            medicineDAO.delete(medId3);
            System.out.println("✓ Medicine deleted, still present: "
                    + medicineDAO.findById(medId3).isPresent());

            System.out.println();

        } catch (Exception e) {
            System.err.println("✗ MedicineDAO test failed: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testStockMovementDAO(StockMovementDAO stockMovementDAO) {
        System.out.println("TEST 2: StockMovementDAO Operations");
        System.out.println("-".repeat(50));

        try {
            // Get the medicine ID from previous test
            MedicineDAO medicineDAO = new MedicineDAO(DatabaseManager.getInstance());
            var medicines = medicineDAO.findAll();
            int medicineId = medicines.get(0).getId();

            // Test save
            StockMovement movement1 = new StockMovement(
                    medicineId,
                    StockMovementType.IN,
                    100,
                    LocalDateTime.now().minusDays(5),
                    "Initial stock purchase"
            );

            int movementId1 = stockMovementDAO.save(movement1);
            System.out.println("✓ Stock movement saved with ID: " + movementId1);

            // Test findById
            var foundMovement = stockMovementDAO.findById(movementId1);
            if (foundMovement.isPresent()) {
                System.out.println("✓ Stock movement found: " + foundMovement.get().getType());
            }

            // Save more movements
            StockMovement movement2 = new StockMovement(
                    medicineId,
                    StockMovementType.OUT,
                    5,
                    LocalDateTime.now().minusDays(2),
                    "Expired stock removal"
            );
            stockMovementDAO.save(movement2);

            // Test findAll
            var allMovements = stockMovementDAO.findAll();
            System.out.println("✓ Total stock movements: " + allMovements.size());

            // Test findByMedicineId
            var medicineMovements = stockMovementDAO.findByMedicineId(medicineId);
            System.out.println("✓ Movements for medicine: " + medicineMovements.size());

            // Test findByType
            var purchaseMovements = stockMovementDAO.findByType(StockMovementType.IN);
            System.out.println("✓ Purchase movements: " + purchaseMovements.size());

            // Test getTotalQuantityByMedicineAndType
            int totalQuantity = stockMovementDAO.getTotalQuantityByMedicineAndType(
                    medicineId, StockMovementType.IN);
            System.out.println("✓ Total purchased quantity: " + totalQuantity);

            // Test countAll
            int count = stockMovementDAO.countAll();
            System.out.println("✓ Stock movement count: " + count);

            System.out.println();

        } catch (Exception e) {
            System.err.println("✗ StockMovementDAO test failed: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    private static void testSaleDAO(SaleDAO saleDAO, SaleItemDAO saleItemDAO, MedicineDAO medicineDAO) {
        System.out.println("TEST 3: SaleDAO and SaleItemDAO Operations");
        System.out.println("-".repeat(50));

        try {
            // Get medicine IDs from database
            var medicines = medicineDAO.findAll();
            int med1Id = medicines.get(0).getId();
            int med2Id = medicines.get(1).getId();

            // Test save Sale - add items to sale before saving
            Sale sale1 = new Sale(1, LocalDateTime.now().minusDays(3));
            
            // Add items to sale so getTotalQuantity() and getTotalPrice() work
            SaleItem item1 = new SaleItem(med1Id, 10, 1.50);
            SaleItem item2 = new SaleItem(med2Id, 5, 2.00);
            sale1.addItem(item1);
            sale1.addItem(item2);
            
            int saleId1 = saleDAO.save(sale1);
            System.out.println("✓ Sale saved with ID: " + saleId1);

            // Test save SaleItem
            int itemId1 = saleItemDAO.save(item1, saleId1);
            System.out.println("✓ Sale item saved with ID: " + itemId1);

            saleItemDAO.save(item2, saleId1);

            // Test findById Sale
            var foundSale = saleDAO.findById(saleId1);
            if (foundSale.isPresent()) {
                System.out.println("✓ Sale found by ID");
            }

            // Test findById SaleItem
            var foundItem = saleItemDAO.findById(itemId1);
            if (foundItem.isPresent()) {
                System.out.println("✓ Sale item found by ID");
            }

            // Test findAll Sales
            var allSales = saleDAO.findAll();
            System.out.println("✓ Total sales: " + allSales.size());

            // Test findBySaleId SaleItems
            var saleItems = saleItemDAO.findBySaleId(saleId1);
            System.out.println("✓ Items in sale: " + saleItems.size());

            // Test findByMedicineId SaleItems
            var medSaleItems = saleItemDAO.findByMedicineId(med1Id);
            System.out.println("✓ Sale items for medicine: " + medSaleItems.size());

            // Create another sale with items
            Sale sale2 = new Sale(2, LocalDateTime.now().minusDays(1));
            SaleItem item3 = new SaleItem(med1Id, 3, 1.50);
            sale2.addItem(item3);
            int saleId2 = saleDAO.save(sale2);

            // Test countAll
            int saleCount = saleDAO.countAll();
            System.out.println("✓ Sale count: " + saleCount);

            int itemCount = saleItemDAO.countAll();
            System.out.println("✓ Sale item count: " + itemCount);

            // Test getTotalQuantitySoldByMedicine
            int totalSold = saleItemDAO.getTotalQuantitySoldByMedicine(med1Id);
            System.out.println("✓ Total quantity sold for medicine: " + totalSold);

            // Test getTotalRevenueByMedicine
            double revenue = saleItemDAO.getTotalRevenueByMedicine(med1Id);
            System.out.println("✓ Total revenue for medicine: " + String.format("%.2f", revenue));

            // Test getAverageUnitPriceByMedicine
            double avgPrice = saleItemDAO.getAverageUnitPriceByMedicine(med1Id);
            System.out.println("✓ Average unit price: " + String.format("%.2f", avgPrice));

            System.out.println();

        } catch (Exception e) {
            System.err.println("✗ Sale DAO test failed: " + e.getMessage());
            throw new DatabaseException(e.getMessage(), e);
        }
    }
}
