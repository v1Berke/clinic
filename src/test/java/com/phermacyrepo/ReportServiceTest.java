package com.phermacyrepo;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.db.dao.StockMovementDAO;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.entity.Sale;
import com.phermacyrepo.domain.entity.SaleItem;
import com.phermacyrepo.domain.entity.StockMovement;
import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.ValidationException;
import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.MedicineRequestDTO;
import com.phermacyrepo.dto.SalesReportDTO;
import com.phermacyrepo.dto.StockReportDTO;
import com.phermacyrepo.service.MedicineService;
import com.phermacyrepo.service.ReportService;
import com.phermacyrepo.service.SaleService;
import com.phermacyrepo.service.StockMovementService;
import com.phermacyrepo.service.impl.MedicineServiceImpl;
import com.phermacyrepo.service.impl.ReportServiceImpl;
import com.phermacyrepo.service.impl.SaleServiceImpl;
import com.phermacyrepo.service.impl.StockMovementServiceImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReportServiceTest {

    private static MedicineService medicineService;
    private static SaleService saleService;
    private static StockMovementService stockMovementService;
    private static ReportService reportService;
    private static SaleDAO saleDAO;
    private static SaleItemDAO saleItemDAO;
    private static StockMovementDAO stockMovementDAO;

    private static long counter = 0;

    @BeforeClass
    public static void setUp() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.initialize();
        dbManager.clearAllData();

        MedicineDAO medicineDAO = new MedicineDAO(dbManager);
        saleDAO = new SaleDAO(dbManager);
        saleItemDAO = new SaleItemDAO(dbManager);
        stockMovementDAO = new StockMovementDAO(dbManager);

        medicineService = new MedicineServiceImpl(dbManager, medicineDAO, saleDAO, saleItemDAO);
        saleService = new SaleServiceImpl(dbManager, saleDAO, saleItemDAO, medicineDAO, stockMovementDAO);
        stockMovementService = new StockMovementServiceImpl(dbManager, medicineDAO, stockMovementDAO);
        reportService = new ReportServiceImpl(saleDAO, saleItemDAO, medicineDAO, stockMovementDAO);

        seedData();
    }

    @AfterClass
    public static void tearDown() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.clearAllData();
        dbManager.shutdown();
    }

    /** Gecmis gun verisi DAO ile (servisler hep bugune yazar). */
    private static void seedData() {
        MedicineDTO med1 = medicineService.createMedicine(request("RaporMed1", 100));
        MedicineDTO med2 = medicineService.createMedicine(request("RaporMed2", 100));

        // Bugun: servis uzerinden satis (3x med1 @10, 2x med2 @10) + stok girisi.
        saleService.processSale(new com.phermacyrepo.dto.SaleCreateDTO(List.of(
                new com.phermacyrepo.dto.SaleCreateDTO.SaleItemRequest(med1.getId(), 3),
                new com.phermacyrepo.dto.SaleCreateDTO.SaleItemRequest(med2.getId(), 2))));
        stockMovementService.registerStockEntry(med1.getId(), 10, "bugun giris");

        // 2 gun once: DAO ile gecmise kayit (4x med1 @10, 20 adet giris).
        LocalDateTime past = LocalDate.now().minusDays(2).atTime(10, 30);
        Sale pastSale = new Sale(past);
        pastSale.addItem(new SaleItem(med1.getId(), 4, 10.0));
        int saleId = saleDAO.save(pastSale);
        saleItemDAO.save(new SaleItem(med1.getId(), 4, 10.0), saleId);
        stockMovementDAO.save(new StockMovement(
                med1.getId(), StockMovementType.IN, 20, past, "gecmis giris"));
        stockMovementDAO.save(new StockMovement(
                med2.getId(), StockMovementType.OUT, 6, past, "gecmis cikis"));
    }

    @Test
    public void todaySalesReport() {
        SalesReportDTO report = reportService.getDailySalesReport(LocalDate.now());

        assertEquals(LocalDate.now(), report.getDate());
        assertEquals(1, report.getSaleCount());
        assertEquals(5, report.getTotalQuantity());
        assertEquals(50.0, report.getTotalRevenue(), 0.001);
        // Kar = (10 - 5) x adet = 25
        assertEquals(25.0, report.getTotalProfit(), 0.001);
        assertEquals(2, report.getLines().size());
    }

    @Test
    public void pastDaySalesReport() {
        LocalDate day = LocalDate.now().minusDays(2);
        SalesReportDTO report = reportService.getDailySalesReport(day);

        assertEquals(1, report.getSaleCount());
        assertEquals(4, report.getTotalQuantity());
        assertEquals(40.0, report.getTotalRevenue(), 0.001);
        assertEquals(20.0, report.getTotalProfit(), 0.001);
        assertEquals(1, report.getLines().size());
        assertEquals("RaporMed1", report.getLines().get(0).getMedicineName());
    }

    @Test
    public void emptyDayHasZeroTotals() {
        SalesReportDTO report = reportService.getDailySalesReport(LocalDate.now().minusDays(10));

        assertEquals(0, report.getSaleCount());
        assertEquals(0, report.getTotalQuantity());
        assertEquals(0.0, report.getTotalRevenue(), 0.001);
        assertTrue(report.getLines().isEmpty());
    }

    @Test
    public void stockReports() {
        StockReportDTO today = reportService.getDailyStockReport(LocalDate.now());
        // Giris 10 (med1), cikis 5 (satis: 3 + 2)
        assertEquals(10, today.getTotalIn());
        assertEquals(5, today.getTotalOut());

        StockReportDTO past = reportService.getDailyStockReport(LocalDate.now().minusDays(2));
        assertEquals(20, past.getTotalIn());
        assertEquals(6, past.getTotalOut());
        assertEquals(2, past.getLines().size());
    }

    @Test
    public void futureDateIsRejected() {
        try {
            reportService.getDailySalesReport(LocalDate.now().plusDays(1));
            fail("Future date should fail");
        } catch (ValidationException expected) {
            // beklenen durum
        }
    }

    private static MedicineRequestDTO request(String name, int stock) {
        long id = System.currentTimeMillis() + (counter++);
        return new MedicineRequestDTO(name, "TEST-RAP-" + id, MedicineType.TABLET, 5.0, 10.0, stock);
    }
}
