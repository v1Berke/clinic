package com.phermacyrepo.ui;

import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.SaleCreateDTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CartModelTest {

    private static MedicineDTO medicine(int id, String name, double price, int stock) {
        return new MedicineDTO(id, name, "BAR-" + id, MedicineType.TABLET,
                5.0, price, stock);
    }

    @Test
    public void repeatedScansMergeIntoSingleRow() {
        CartModel cart = new CartModel();
        cart.add(medicine(1, "Aspirin", 10.0, 100));
        cart.add(medicine(1, "Aspirin", 10.0, 100));
        cart.add(medicine(1, "Aspirin", 10.0, 100));

        assertEquals(1, cart.getRows().size());
        assertEquals(3, cart.quantityOf(1));
        assertEquals(30.0, cart.getTotalPrice(), 0.001);
    }

    @Test
    public void differentMedicinesStaySeparate() {
        CartModel cart = new CartModel();
        cart.add(medicine(1, "Aspirin", 10.0, 100));
        cart.add(medicine(2, "Parol", 20.0, 50));

        assertEquals(2, cart.getRows().size());
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(30.0, cart.getTotalPrice(), 0.001);
    }

    @Test
    public void removeAndClear() {
        CartModel cart = new CartModel();
        cart.add(medicine(1, "Aspirin", 10.0, 100));
        assertFalse(cart.isEmpty());

        cart.remove(cart.getRows().get(0));
        assertTrue(cart.isEmpty());

        cart.add(medicine(1, "Aspirin", 10.0, 100));
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0.0, cart.getTotalPrice(), 0.001);
    }

    @Test
    public void toSaleCreateDTOUsesMergedQuantities() {
        CartModel cart = new CartModel();
        cart.add(medicine(1, "Aspirin", 10.0, 100));
        cart.add(medicine(1, "Aspirin", 10.0, 100));
        cart.add(medicine(2, "Parol", 20.0, 50));

        SaleCreateDTO dto = cart.toSaleCreateDTO();
        assertEquals(2, dto.getItems().size());
        assertEquals(1, dto.getItems().get(0).getMedicineId());
        assertEquals(2, dto.getItems().get(0).getQuantity());
        assertEquals(2, dto.getItems().get(1).getMedicineId());
        assertEquals(1, dto.getItems().get(1).getQuantity());
    }
}
