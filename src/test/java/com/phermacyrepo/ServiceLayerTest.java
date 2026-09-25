package com.phermacyrepo;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.db.dao.StockMovementDAO;
import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.domain.exceptions.BusinessRuleException;
import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.MedicineRequestDTO;
import com.phermacyrepo.dto.SaleCreateDTO;
import com.phermacyrepo.dto.SaleDTO;
import com.phermacyrepo.dto.StockMovementDTO;
import com.phermacyrepo.service.MedicineService;
import com.phermacyrepo.service.SaleService;
import com.phermacyrepo.service.StockMovementService;
import com.phermacyrepo.service.impl.MedicineServiceImpl;
import com.phermacyrepo.service.impl.SaleServiceImpl;
import com.phermacyrepo.service.impl.StockMovementServiceImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Service katmani testi: satis akisi basta olmak uzere
 * service -> domain -> DAO zincirini uctan uca dogrular.
 */
public class ServiceLayerTest {

    private static MedicineService medicineService;
    private static SaleService saleService;
    private static StockMovementService stockMovementService;

    private static long counter = 0;

    @BeforeClass
    public static void setUp() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.initialize();
        dbManager.clearAllData();

        MedicineDAO medicineDAO = new MedicineDAO(dbManager);
        SaleDAO saleDAO = new SaleDAO(dbManager);
        SaleItemDAO saleItemDAO = new SaleItemDAO(dbManager);
        StockMovementDAO stockMovementDAO = new StockMovementDAO(dbManager);

        medicineService = new MedicineServiceImpl(dbManager, medicineDAO, saleDAO, saleItemDAO);
        saleService = new SaleServiceImpl(dbManager, saleDAO, saleItemDAO, medicineDAO,
                stockMovementDAO);
        stockMovementService = new StockMovementServiceImpl(dbManager, medicineDAO,
                stockMovementDAO);
    }

    @AfterClass
    public static void tearDown() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.clearAllData();
        dbManager.shutdown();
    }

    @Test
    public void createAndGetMedicine() {
        MedicineDTO created = medicineService.createMedicine(request("Parol", 100));

        assertTrue(created.getId() > 0);
        assertEquals("Parol", created.getName());
        assertEquals(100, created.getStock());

        MedicineDTO found = medicineService.getMedicine(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getBarcode(), found.getBarcode());
    }

    @Test
    public void updateMedicine() {
        MedicineDTO created = medicineService.createMedicine(request("Aspirin", 50));

        MedicineRequestDTO update = request("Aspirin Plus", 60);
        update.setBarcode(created.getBarcode());
        MedicineDTO updated = medicineService.updateMedicine(created.getId(), update);

        assertEquals("Aspirin Plus", updated.getName());
        assertEquals(60, updated.getStock());
    }

    @Test
    public void lowStockAndDeleteWithoutHistory() {
        MedicineDTO low = medicineService.createMedicine(request("LowMed", 2));

        List<MedicineDTO> lows = medicineService.getLowStockMedicines(5);
        assertTrue(lows.stream().anyMatch(m -> m.getId() == low.getId()));

        // Satis gecmisi yoksa hard-delete olur, kayit bulunamaz.
        medicineService.deleteMedicine(low.getId());
        try {
            medicineService.getMedicine(low.getId());
            fail("Deleted medicine should not be found");
        } catch (BusinessRuleException expected) {
            // beklenen durum
        }
    }

    @Test
    public void stockEntryAndRemoval() {
        MedicineDTO created = medicineService.createMedicine(request("StockMed", 10));

        stockMovementService.registerStockEntry(created.getId(), 5, "purchase");
        assertEquals(15, medicineService.getMedicine(created.getId()).getStock());

        stockMovementService.removeStockEntry(created.getId(), 4, "damaged");
        assertEquals(11, medicineService.getMedicine(created.getId()).getStock());

        List<StockMovementDTO> movements =
                stockMovementService.getMovementsByMedicineId(created.getId());
        assertEquals(2, movements.size());

        // Stoktan fazla dusus engellenir, stok degismez.
        try {
            stockMovementService.removeStockEntry(created.getId(), 99, "too much");
            fail("Over-removal should fail");
        } catch (BusinessRuleException expected) {
            // beklenen durum
        }
        assertEquals(11, medicineService.getMedicine(created.getId()).getStock());
    }

    @Test
    public void processSaleSuccess() {
        MedicineDTO med1 = medicineService.createMedicine(request("SaleMed1", 100));
        MedicineDTO med2 = medicineService.createMedicine(request("SaleMed2", 100));

        SaleCreateDTO saleRequest = new SaleCreateDTO(List.of(
                new SaleCreateDTO.SaleItemRequest(med1.getId(), 3),
                new SaleCreateDTO.SaleItemRequest(med2.getId(), 2)));

        SaleDTO sale = saleService.processSale(saleRequest);

        double expectedTotal = 3 * med1.getSalePrice() + 2 * med2.getSalePrice();
        assertEquals(expectedTotal, sale.getTotalPrice(), 0.001);
        assertEquals(5, sale.getTotalQuantity());
        assertEquals(2, sale.getItems().size());

        // Stok dustu mu?
        assertEquals(97, medicineService.getMedicine(med1.getId()).getStock());
        assertEquals(98, medicineService.getMedicine(med2.getId()).getStock());

        // OUT hareketleri yazildi mi?
        assertFalse(stockMovementService.getMovementsByMedicineId(med1.getId()).isEmpty());

        // Tekil ve toplu okuma tutarli mi?
        assertEquals(sale.getId(), saleService.getSaleById(sale.getId()).getId());
        assertTrue(saleService.getAllSales().stream()
                .anyMatch(s -> s.getId() == sale.getId()));
    }

    @Test
    public void processSaleWithInsufficientStockFails() {
        MedicineDTO med = medicineService.createMedicine(request("ScarceMed", 1));

        SaleCreateDTO saleRequest = new SaleCreateDTO(List.of(
                new SaleCreateDTO.SaleItemRequest(med.getId(), 5)));

        try {
            saleService.processSale(saleRequest);
            fail("Sale with insufficient stock should fail");
        } catch (BusinessRuleException expected) {
            // beklenen durum
        }

        // Basarisiz satis stogu degistirmez, satis olusmaz.
        assertEquals(1, medicineService.getMedicine(med.getId()).getStock());
        assertTrue(saleService.getAllSales().stream()
                .noneMatch(s -> s.getItems().stream()
                        .anyMatch(i -> i.getMedicineId() == med.getId())));
    }

    @Test
    public void deleteMedicineWithSaleHistoryDeletesEverything() {
        MedicineDTO med = medicineService.createMedicine(request("HistMed", 10));

        SaleDTO sale = saleService.processSale(new SaleCreateDTO(List.of(
                new SaleCreateDTO.SaleItemRequest(med.getId(), 1))));

        // Direkt silme: ilac + kalemleri silinir, bosen satis da silinir.
        medicineService.deleteMedicine(med.getId());
        try {
            medicineService.getMedicine(med.getId());
            fail("Deleted medicine should not be found");
        } catch (BusinessRuleException expected) {
            // beklenen durum
        }
        try {
            saleService.getSaleById(sale.getId());
            fail("Emptied sale should not be found");
        } catch (BusinessRuleException expected) {
            // beklenen durum
        }
    }

    private static MedicineRequestDTO request(String name, int stock) {
        long id = System.currentTimeMillis() + (counter++);
        return new MedicineRequestDTO(
                name,
                "TEST-BAR-" + id,
                MedicineType.TABLET,
                5.0,
                10.0,
                stock);
    }
}
