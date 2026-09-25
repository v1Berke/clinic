package com.phermacyrepo.ui;

import com.phermacyrepo.domain.enum_.MedicineType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UiHelpersTest {

    @Test
    public void medicineTypesAreTurkish() {
        assertEquals("Tablet", UiHelpers.medicineTypeText(MedicineType.TABLET));
        assertEquals("\u015Eurup", UiHelpers.medicineTypeText(MedicineType.SYRUP));
        assertEquals("Enjeksiyon", UiHelpers.medicineTypeText(MedicineType.INJECTION));
        assertEquals("Merhem", UiHelpers.medicineTypeText(MedicineType.OINTMENT));
        assertEquals("-", UiHelpers.medicineTypeText(null));
    }
}
