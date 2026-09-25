package com.phermacyrepo.service.impl;

import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.db.dao.StockMovementDAO;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.entity.SaleItem;
import com.phermacyrepo.domain.entity.StockMovement;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.ValidationException;
import com.phermacyrepo.dto.SalesReportDTO;
import com.phermacyrepo.dto.StockReportDTO;
import com.phermacyrepo.service.ReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Salt-okunur rapor servisi. Bilgisayar saatine gore gunluk dokum uretir.
 * Kar = (satis fiyati - guncel alis fiyati) x adet.
 */
public class ReportServiceImpl implements ReportService {

    private final SaleDAO saleDAO;
    private final SaleItemDAO saleItemDAO;
    private final MedicineDAO medicineDAO;
    private final StockMovementDAO stockMovementDAO;

    public ReportServiceImpl(SaleDAO saleDAO, SaleItemDAO saleItemDAO,
                             MedicineDAO medicineDAO, StockMovementDAO stockMovementDAO) {
        if (saleDAO == null || saleItemDAO == null
                || medicineDAO == null || stockMovementDAO == null) {
            throw new ValidationException("DAO dependencies cannot be null");
        }
        this.saleDAO = saleDAO;
        this.saleItemDAO = saleItemDAO;
        this.medicineDAO = medicineDAO;
        this.stockMovementDAO = stockMovementDAO;
    }

    @Override
    public SalesReportDTO getDailySalesReport(LocalDate date) {
        requireDate(date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<SaleItem> items = new ArrayList<>();
        var sales = saleDAO.findByDateRange(start, end);
        for (var sale : sales) {
            items.addAll(saleItemDAO.findBySaleId(sale.getSaleId()));
        }

        Map<Integer, SalesReportDTO.Line> byMedicine = new LinkedHashMap<>();
        for (SaleItem item : items) {
            SalesReportDTO.Line line = byMedicine.computeIfAbsent(item.getMedicineId(),
                    id -> new SalesReportDTO.Line(id, resolveMedicineName(id), 0, 0, 0));
            double revenue = item.getUnitPrice() * item.getQuantity();
            double profit = (item.getUnitPrice() - resolvePurchasePrice(item.getMedicineId()))
                    * item.getQuantity();
            line.setQuantity(line.getQuantity() + item.getQuantity());
            line.setRevenue(line.getRevenue() + revenue);
            line.setProfit(line.getProfit() + profit);
        }

        List<SalesReportDTO.Line> lines = new ArrayList<>(byMedicine.values());
        lines.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));

        int totalQty = lines.stream().mapToInt(SalesReportDTO.Line::getQuantity).sum();
        double totalRevenue = lines.stream().mapToDouble(SalesReportDTO.Line::getRevenue).sum();
        double totalProfit = lines.stream().mapToDouble(SalesReportDTO.Line::getProfit).sum();

        return new SalesReportDTO(date, lines, totalQty, totalRevenue, totalProfit, sales.size());
    }

    @Override
    public StockReportDTO getDailyStockReport(LocalDate date) {
        requireDate(date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<StockMovement> movements = stockMovementDAO.findByDateRange(start, end);

        Map<Integer, StockReportDTO.Line> byMedicine = new LinkedHashMap<>();
        for (StockMovement movement : movements) {
            StockReportDTO.Line line = byMedicine.computeIfAbsent(movement.getMedicineId(),
                    id -> new StockReportDTO.Line(id, resolveMedicineName(id), 0, 0));
            if (movement.getType() == StockMovementType.IN) {
                line.setInQuantity(line.getInQuantity() + movement.getQuantity());
            } else {
                line.setOutQuantity(line.getOutQuantity() + movement.getQuantity());
            }
        }

        List<StockReportDTO.Line> lines = new ArrayList<>(byMedicine.values());
        lines.sort((a, b) -> Integer.compare(
                b.getInQuantity() + b.getOutQuantity(),
                a.getInQuantity() + a.getOutQuantity()));

        int totalIn = lines.stream().mapToInt(StockReportDTO.Line::getInQuantity).sum();
        int totalOut = lines.stream().mapToInt(StockReportDTO.Line::getOutQuantity).sum();

        return new StockReportDTO(date, lines, totalIn, totalOut);
    }

    private String resolveMedicineName(int medicineId) {
        return medicineDAO.findById(medicineId)
                .map(Medicine::getName)
                .orElse("Silinmis ilac");
    }

    private double resolvePurchasePrice(int medicineId) {
        return medicineDAO.findById(medicineId)
                .map(Medicine::getPurchasePrice)
                .orElse(0.0);
    }

    private void requireDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Report date cannot be in the future");
        }
    }
}
