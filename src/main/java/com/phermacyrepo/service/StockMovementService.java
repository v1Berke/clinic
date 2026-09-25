package com.phermacyrepo.service;

import com.phermacyrepo.dto.StockMovementDTO;
import java.util.*;

public interface StockMovementService {
    void registerStockEntry(int medicineId, int quantity, String note);
    void removeStockEntry(int medicineId, int quantity, String note);
    List<StockMovementDTO> getMovementsByMedicineId(int medicineId);
    List<StockMovementDTO> getAllMovements();
}
