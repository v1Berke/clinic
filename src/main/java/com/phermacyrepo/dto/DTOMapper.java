package com.phermacyrepo.dto;

import com.phermacyrepo.domain.entity.Medicine;
import com.phermacyrepo.domain.entity.Sale;
import com.phermacyrepo.domain.entity.SaleItem;
import com.phermacyrepo.domain.entity.StockMovement;

import java.util.List;

public class DTOMapper {

    public static MedicineDTO toDTO(Medicine entity) {
        if (entity == null) return null;
        return new MedicineDTO(
                entity.getId(),
                entity.getName(),
                entity.getBarcode(),
                entity.getType(),
                entity.getExpirationDate(),
                entity.getPurchasePrice(),
                entity.getSalePrice(),
                entity.getStock(),
                entity.isActive()
        );
    }

    public static Medicine toEntity(MedicineRequestDTO requestDTO) {
        if (requestDTO == null) return null;
        return new Medicine(
                requestDTO.getName(),
                requestDTO.getBarcode(),
                requestDTO.getType(),
                requestDTO.getExpirationDate(),
                requestDTO.getPurchasePrice(),
                requestDTO.getSalePrice(),
                requestDTO.getStock()
        );
    }

    public static SaleItemDTO toDTO(SaleItem entity, String medicineName) {
        if (entity == null) return null;
        return new SaleItemDTO(
                entity.getId(),
                entity.getMedicineId(),
                medicineName,
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getUnitPrice() * entity.getQuantity()
        );
    }

    public static SaleDTO toDTO(Sale entity, List<SaleItemDTO> itemDTOs) {
        if (entity == null) return null;
        return new SaleDTO(
                entity.getSaleId(),
                entity.getSaleDate(),
                entity.getTotalPrice(),
                entity.getTotalQuantity(),
                itemDTOs
        );
    }

    public static StockMovementDTO toDTO(StockMovement entity, String medicineName) {
        if (entity == null) return null;
        return new StockMovementDTO(
                entity.getId(),
                entity.getMedicineId(),
                medicineName,
                entity.getType(),
                entity.getQuantity(),
                entity.getDate(),
                entity.getReason()
        );
    }
}
