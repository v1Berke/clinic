package com.phermacyrepo.service.impl;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.exceptions.BusinessRuleException;
import com.phermacyrepo.domain.exceptions.ValidationException;
import com.phermacyrepo.dto.DTOMapper;
import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.MedicineRequestDTO;
import com.phermacyrepo.service.MedicineService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service katmani: disariya DTO verir, is kurallarini domain entity'leri
 * uzerinden calistirir, veritabani islerini sadece DAO'lar uzerinden yapar.
 * Burada SQL yazilmaz, hesaplama/stok mantigi entity icindedir.
 */
public class MedicineServiceImpl implements MedicineService {

    private final DatabaseManager dbManager;
    private final MedicineDAO medicineDAO;
    private final SaleDAO saleDAO;
    private final SaleItemDAO saleItemDAO;

    public MedicineServiceImpl(DatabaseManager dbManager, MedicineDAO medicineDAO,
                               SaleDAO saleDAO, SaleItemDAO saleItemDAO) {
        if (dbManager == null) {
            throw new ValidationException("DatabaseManager cannot be null");
        }
        if (medicineDAO == null) {
            throw new ValidationException("MedicineDAO cannot be null");
        }
        if (saleDAO == null) {
            throw new ValidationException("SaleDAO cannot be null");
        }
        if (saleItemDAO == null) {
            throw new ValidationException("SaleItemDAO cannot be null");
        }
        this.dbManager = dbManager;
        this.medicineDAO = medicineDAO;
        this.saleDAO = saleDAO;
        this.saleItemDAO = saleItemDAO;
    }

    @Override
    public MedicineDTO createMedicine(MedicineRequestDTO requestDTO) {
        requireRequest(requestDTO);

        if (medicineDAO.existsByBarcode(requestDTO.getBarcode())) {
            throw new BusinessRuleException(
                    "Medicine with barcode '" + requestDTO.getBarcode() + "' already exists");
        }

        // Alan validasyonu Medicine constructor icinde yapilir.
        Medicine medicine = DTOMapper.toEntity(requestDTO);
        int generatedId = medicineDAO.save(medicine);

        Medicine saved = medicineDAO.findById(generatedId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Medicine could not be reloaded after save, id: " + generatedId));
        return DTOMapper.toDTO(saved);
    }

    @Override
    public MedicineDTO updateMedicine(int id, MedicineRequestDTO requestDTO) {
        requirePositiveId(id);
        requireRequest(requestDTO);

        Medicine existing = medicineDAO.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Medicine not found with id: " + id));

        if (!existing.getBarcode().equals(requestDTO.getBarcode())
                && medicineDAO.existsByBarcode(requestDTO.getBarcode())) {
            throw new BusinessRuleException(
                    "Another medicine with barcode '" + requestDTO.getBarcode() + "' already exists");
        }

        // Guncelleme domain metodu + DAO uzerinden olur, service SQL yazmaz.
        existing.updateDetails(
                requestDTO.getName(),
                requestDTO.getBarcode(),
                requestDTO.getType(),
                requestDTO.getPurchasePrice(),
                requestDTO.getSalePrice(),
                requestDTO.getStock());
        medicineDAO.updateDetails(existing);

        Medicine updated = medicineDAO.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Medicine not found after update, id: " + id));
        return DTOMapper.toDTO(updated);
    }

    @Override
    public void deleteMedicine(int id) {
        requirePositiveId(id);

        medicineDAO.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Medicine not found with id: " + id));

        // Direkt silme: ilaca ait satis kalemleri once silinir (RESTRICT),
        // kalemi kalmayan satislar da temizlenir. Tek transaksiyonda.
        dbManager.beginTransaction();
        try {
            List<Integer> saleIds = saleItemDAO.findSaleIdsByMedicineId(id);
            saleItemDAO.deleteByMedicineId(id);
            for (int saleId : saleIds) {
                if (saleItemDAO.countBySaleId(saleId) == 0) {
                    saleDAO.delete(saleId);
                }
            }
            medicineDAO.delete(id);
            dbManager.commit();
        } catch (RuntimeException e) {
            try {
                dbManager.rollback();
            } catch (RuntimeException rollbackError) {
                // rollback hatasi orijinal hatayi maskelemesin
            }
            throw e;
        }
    }

    @Override
    public MedicineDTO getMedicine(int id) {
        requirePositiveId(id);

        Medicine medicine = medicineDAO.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Medicine not found with id: " + id));
        return DTOMapper.toDTO(medicine);
    }

    @Override
    public List<MedicineDTO> getAllMedicines() {
        return medicineDAO.findAll().stream()
                .map(DTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicineDTO> getLowStockMedicines(int threshold) {
        if (threshold < 0) {
            throw new ValidationException("Threshold cannot be negative");
        }
        return medicineDAO.findAll().stream()
                .filter(m -> m.getStock() <= threshold)
                .map(DTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void requireRequest(MedicineRequestDTO dto) {
        if (dto == null) {
            throw new ValidationException("Medicine request cannot be null");
        }
    }

    private void requirePositiveId(int id) {
        if (id <= 0) {
            throw new ValidationException("Id must be positive");
        }
    }
}
