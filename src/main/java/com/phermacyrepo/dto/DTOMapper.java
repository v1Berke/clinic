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
                entity.getPurchasePrice(),
                entity.getSalePrice(),
                entity.getStock()
        );
    }

    public static Medicine toEntity(MedicineRequestDTO requestDTO) {
        if (requestDTO == null) return null;
        return new Medicine(
                requestDTO.getName(),
                requestDTO.getBarcode(),
                requestDTO.getType(),
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
        List<SaleItemDTO> items = itemDTOs == null ? List.of() : itemDTOs;
        // Toplamlar DB satirina veya bos entity'ye degil, kalemlere bakilarak
        // hesaplanir. Boylece SaleDAO mapper'i toplam tasimasa bile DTO dogru olur.
        double totalPrice = items.stream()
                .mapToDouble(SaleItemDTO::getTotalPrice)
                .sum();
        int totalQuantity = items.stream()
                .mapToInt(SaleItemDTO::getQuantity)
                .sum();
        return new SaleDTO(
                entity.getSaleId(),
                entity.getSaleDate(),
                totalPrice,
                totalQuantity,
                items
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
