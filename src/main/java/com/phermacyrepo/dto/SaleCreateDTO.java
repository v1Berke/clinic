package com.phermacyrepo.dto;

import java.util.ArrayList;
import java.util.List;

public class SaleCreateDTO {
    private List<SaleItemRequest> items = new ArrayList<>();

    public static class SaleItemRequest {
        private int medicineId;
        private int quantity;

        public SaleItemRequest() {}

        public SaleItemRequest(int medicineId, int quantity) {
            this.medicineId = medicineId;
            this.quantity = quantity;
        }

        public int getMedicineId() { return medicineId; }
        public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public SaleCreateDTO() {}

    public SaleCreateDTO(List<SaleItemRequest> items) {
        this.items = items;
    }

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }
}
