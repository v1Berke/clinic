package com.phermacyrepo.service.impl;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.db.dao.StockMovementDAO;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.entity.Sale;
import com.phermacyrepo.domain.entity.SaleItem;
import com.phermacyrepo.domain.entity.StockMovement;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.BusinessRuleException;
import com.phermacyrepo.domain.exceptions.ValidationException;
import com.phermacyrepo.dto.DTOMapper;
import com.phermacyrepo.dto.SaleCreateDTO;
import com.phermacyrepo.dto.SaleDTO;
import com.phermacyrepo.dto.SaleItemDTO;
import com.phermacyrepo.service.SaleService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service katmani: satis akisini orkestre eder.
 * - Girdi: DTO, cikti: DTO
 * - Kurallar domain entity'lerde (stok kontrolu Medicine.decreaseStock icinde)
 * - Veri erisimi sadece DAO ile, SQL yok
 * - Birden fazla DAO ayni akista calistigi icin transaction siniri buradadir
 */
public class SaleServiceImpl implements SaleService {

    private final DatabaseManager dbManager;
    private final SaleDAO saleDAO;
    private final SaleItemDAO saleItemDAO;
    private final MedicineDAO medicineDAO;
    private final StockMovementDAO stockMovementDAO;

    public SaleServiceImpl(DatabaseManager dbManager,
                           SaleDAO saleDAO,
                           SaleItemDAO saleItemDAO,
                           MedicineDAO medicineDAO,
                           StockMovementDAO stockMovementDAO) {
        if (dbManager == null) {
            throw new ValidationException("DatabaseManager cannot be null");
        }
        if (saleDAO == null || saleItemDAO == null
                || medicineDAO == null || stockMovementDAO == null) {
            throw new ValidationException("DAO dependencies cannot be null");
        }
        this.dbManager = dbManager;
        this.saleDAO = saleDAO;
        this.saleItemDAO = saleItemDAO;
        this.medicineDAO = medicineDAO;
        this.stockMovementDAO = stockMovementDAO;
    }

    @Override
    public SaleDTO processSale(SaleCreateDTO saleCreateDTO) {
        if (saleCreateDTO == null || saleCreateDTO.getItems() == null
                || saleCreateDTO.getItems().isEmpty()) {
            throw new ValidationException("Sale must contain at least one item");
        }

        // 1. Hazirlik: domain nesnelerini kur, satis oncesi kurallari denetle.
        // Stok henuz dusurulmez, hata varsa transaksiyon acilmadan hata verilir.
        List<Medicine> medicines = new ArrayList<>();
        List<SaleItem> saleItems = new ArrayList<>();
        for (SaleCreateDTO.SaleItemRequest req : saleCreateDTO.getItems()) {
            if (req == null) {
                throw new ValidationException("Sale item request cannot be null");
            }
            if (req.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be positive");
            }
            if (req.getMedicineId() <= 0) {
                throw new ValidationException("Invalid medicine id: " + req.getMedicineId());
            }

            Medicine medicine = medicineDAO.findById(req.getMedicineId())
                    .orElseThrow(() -> new BusinessRuleException(
                            "Medicine not found with id: " + req.getMedicineId()));
            checkSellable(medicine, req.getQuantity());

            medicines.add(medicine);
            // Satis fiyati guncel ilac satis fiyatidir, entity validasyonu uygular.
            saleItems.add(new SaleItem(medicine.getId(), req.getQuantity(), medicine.getSalePrice()));
        }

        // 2. Kayit: satis + kalemler + stok + hareketler tek transaksiyonda.
        // Toplam tutar/miktar Sale entity icinde kalemlerden hesaplanir.
        LocalDateTime now = LocalDateTime.now();
        Sale sale = new Sale(now);
        for (SaleItem item : saleItems) {
            sale.addItem(item);
        }

        dbManager.beginTransaction();
        try {
            int saleId = saleDAO.save(sale);
            sale.setSaleId(saleId);

            for (int i = 0; i < saleItems.size(); i++) {
                SaleItem item = saleItems.get(i);
                Medicine medicine = medicines.get(i);

                saleItemDAO.save(item, saleId);

                // Yetersiz stok kontrolu decreaseStock icindedir.
                medicine.decreaseStock(item.getQuantity());
                medicineDAO.update(medicine);

                stockMovementDAO.save(new StockMovement(
                        medicine.getId(),
                        StockMovementType.OUT,
                        item.getQuantity(),
                        now,
                        "Sale #" + saleId));
            }

            dbManager.commit();
            return getSaleById(saleId);
        } catch (RuntimeException e) {
            rollbackQuietly();
            throw e;
        }
    }

    @Override
    public SaleDTO getSaleById(int id) {
        requirePositiveId(id);

        Sale sale = saleDAO.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Sale not found with id: " + id));

        return toDTO(sale);
    }

    @Override
    public List<SaleDTO> getAllSales() {
        return saleDAO.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Sale + kalemler + ilac adlarini SaleDTO'ya cevirir.
     * Kalemler sale_item tablosundan okunur, toplamlar mapper'da hesaplanir.
     */
    private SaleDTO toDTO(Sale sale) {
        List<SaleItem> items = saleItemDAO.findBySaleId(sale.getSaleId());

        Sale fullSale = new Sale(sale.getSaleId(), sale.getSaleDate());
        for (SaleItem item : items) {
            fullSale.addItem(item);
        }

        List<SaleItemDTO> itemDTOs = items.stream()
                .map(item -> DTOMapper.toDTO(item, resolveMedicineName(item.getMedicineId())))
                .collect(Collectors.toList());

        return DTOMapper.toDTO(fullSale, itemDTOs);
    }

    private void checkSellable(Medicine medicine, int quantity) {
        if (medicine.getStock() < quantity) {
            throw new BusinessRuleException(
                    "Insufficient stock for '" + medicine.getName()
                            + "'. Available: " + medicine.getStock()
                            + ", requested: " + quantity);
        }
    }

    private String resolveMedicineName(int medicineId) {
        return medicineDAO.findById(medicineId)
                .map(Medicine::getName)
                .orElse("Unknown");
    }

    private void requirePositiveId(int id) {
        if (id <= 0) {
            throw new ValidationException("Id must be positive");
        }
    }

    private void rollbackQuietly() {
        try {
            dbManager.rollback();
        } catch (RuntimeException rollbackError) {
            // rollback hatasi orijinal hatayi maskelemesin
        }
    }
}
