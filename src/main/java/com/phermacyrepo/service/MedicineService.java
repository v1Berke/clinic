package com.phermacyrepo.service;

import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.MedicineRequestDTO;
import java.util.*;

public interface MedicineService {

    MedicineDTO createMedicine(MedicineRequestDTO requestDTO);
    MedicineDTO updateMedicine(int id, MedicineRequestDTO requestDTO);
    void deleteMedicine(int id);
    MedicineDTO getMedicine(int id);
    List<MedicineDTO> getAllMedicines();
    List<MedicineDTO> getLowStockMedicines(int threshold);
}
