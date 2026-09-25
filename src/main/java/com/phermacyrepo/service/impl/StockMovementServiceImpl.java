package com.phermacyrepo.service.impl;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.StockMovementDAO;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.entity.StockMovement;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.BusinessRuleException;
import com.phermacyrepo.domain.exceptions.ValidationException;
import com.phermacyrepo.dto.DTOMapper;
import com.phermacyrepo.dto.StockMovementDTO;
import com.phermacyrepo.service.StockMovementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service katmani: stok giris/cikis akisini yonetir.
 * - Stok degisimi Medicine.increaseStock/decreaseStock ile olur
 * - Kayit MedicineDAO.update + StockMovementDAO.save ile olur, SQL yok
 * - Iki yazma tek transaksiyonda yapilir
 */
public class StockMovementServiceImpl implements StockMovementService {

    private final DatabaseManager dbManager;
    private final MedicineDAO medicineDAO;
    private final StockMovementDAO stockMovementDAO;

    public StockMovementServiceImpl(DatabaseManager dbManager,
                                    MedicineDAO medicineDAO,
                                    StockMovementDAO stockMovementDAO) {
        if (dbManager == null) {
            throw new ValidationException("DatabaseManager cannot be null");
        }
        if (medicineDAO == null || stockMovementDAO == null) {
            throw new ValidationException("DAO dependencies cannot be null");
        }
        this.dbManager = dbManager;
        this.medicineDAO = medicineDAO;
        this.stockMovementDAO = stockMovementDAO;
    }

    @Override
    public void registerStockEntry(int medicineId, int quantity, String note) {
        requirePositiveId(medicineId);
        requirePositiveQuantity(quantity);
        String reason = requireNote(note);

        Medicine medicine = findMedicine(medicineId);

        dbManager.beginTransaction();
        try {
            medicine.increaseStock(quantity);
            medicineDAO.update(medicine);
            stockMovementDAO.save(new StockMovement(
                    medicineId, StockMovementType.IN, quantity, LocalDateTime.now(), reason));
            dbManager.commit();
        } catch (RuntimeException e) {
            rollbackQuietly();
            throw e;
        }
    }

    @Override
    public void removeStockEntry(int medicineId, int quantity, String note) {
        requirePositiveId(medicineId);
        requirePositiveQuantity(quantity);
        String reason = requireNote(note);

        Medicine medicine = findMedicine(medicineId);

        dbManager.beginTransaction();
        try {
            // Yetersiz stok kontrolu decreaseStock icindedir.
            medicine.decreaseStock(quantity);
            medicineDAO.update(medicine);
            stockMovementDAO.save(new StockMovement(
                    medicineId, StockMovementType.OUT, quantity, LocalDateTime.now(), reason));
            dbManager.commit();
        } catch (RuntimeException e) {
            rollbackQuietly();
            throw e;
        }
    }

    @Override
    public List<StockMovementDTO> getMovementsByMedicineId(int medicineId) {
        requirePositiveId(medicineId);
        return stockMovementDAO.findByMedicineId(medicineId).stream()
                .map(m -> DTOMapper.toDTO(m, resolveMedicineName(m.getMedicineId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getAllMovements() {
        return stockMovementDAO.findAll().stream()
                .map(m -> DTOMapper.toDTO(m, resolveMedicineName(m.getMedicineId())))
                .collect(Collectors.toList());
    }

    private Medicine findMedicine(int medicineId) {
        return medicineDAO.findById(medicineId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Medicine not found with id: " + medicineId));
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

    private void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
    }

    private String requireNote(String note) {
        if (note == null || note.isBlank()) {
            throw new ValidationException("Reason/note cannot be empty");
        }
        return note.trim();
    }

    private void rollbackQuietly() {
        try {
            dbManager.rollback();
        } catch (RuntimeException rollbackError) {
            // rollback hatasi orijinal hatayi maskelemesin
        }
    }
}
