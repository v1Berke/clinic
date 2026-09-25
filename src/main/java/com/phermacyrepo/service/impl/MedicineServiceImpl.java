package com.phermacyrepo.service.impl;

import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.exceptions.BusinessRuleException;
import com.phermacyrepo.domain.exceptions.ValidationException;
import com.phermacyrepo.dto.DTOMapper;
import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.MedicineRequestDTO;
import com.phermacyrepo.service.MedicineService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service katmani: disariya DTO verir, is kurallarini domain entity'leri
 * uzerinden calistirir, veritabani islerini sadece DAO'lar uzerinden yapar.
 * Burada SQL yazilmaz, hesaplama/stok mantigi entity icindedir.
 */
public class MedicineServiceImpl implements MedicineService {

    private final MedicineDAO medicineDAO;
    private final SaleItemDAO saleItemDAO;

    public MedicineServiceImpl(MedicineDAO medicineDAO, SaleItemDAO saleItemDAO) {
        if (medicineDAO == null) {
            throw new ValidationException("MedicineDAO cannot be null");
        }
        if (saleItemDAO == null) {
            throw new ValidationException("SaleItemDAO cannot be null");
        }
        this.medicineDAO = medicineDAO;
        this.saleItemDAO = saleItemDAO;
    }

    @Override
    public MedicineDTO createMedicine(MedicineRequestDTO requestDTO) {
        requireRequest(requestDTO);
        checkNotExpired(requestDTO.getExpirationDate());

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
        checkNotExpired(requestDTO.getExpirationDate());

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
                requestDTO.getExpirationDate(),
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

        // Satis gecmisi olan ilac FK RESTRICT yuzunden silinemez,
        // bu durumda soft-delete (deactivate) uygulanir.
        if (!saleItemDAO.findByMedicineId(id).isEmpty()) {
            medicineDAO.deactivate(id);
            return;
        }

        medicineDAO.delete(id);
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
        return medicineDAO.findAllActive().stream()
                .filter(m -> m.getStock() <= threshold)
                .map(DTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void requireRequest(MedicineRequestDTO dto) {
        if (dto == null) {
            throw new ValidationException("Medicine request cannot be null");
        }
    }

    private void checkNotExpired(LocalDate expirationDate) {
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Expiration date cannot be in the past: " + expirationDate);
        }
    }

    private void requirePositiveId(int id) {
        if (id <= 0) {
            throw new ValidationException("Id must be positive");
        }
    }
}
