package com.phermacyrepo.domain.entity;
import com.phermacyrepo.domain.exceptions.*;


public class SaleItem {

    private int id; 
    private final int medicineId;
    private int quantity;
    private final double unitPrice;

    public SaleItem(int medicineId, int quantity, double unitPrice) {
        if (medicineId <= 0) throw new ValidationException("Invalid medicine id");
        if (quantity <= 0) throw new ValidationException("Quantity must be positive");
        if (unitPrice <= 0) throw new ValidationException("Unit price must be positive");

        this.medicineId = medicineId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void setId(int id) {
        if (id <= 0) throw new ValidationException("Invalid sale item id");
        this.id = id;
    }

    void merge(int additionalQuantity) {
        if (additionalQuantity <= 0) throw new ValidationException("Invalid quantity");
        this.quantity += additionalQuantity;
    }

    public int getId() { return id; }
    public int getMedicineId() { return medicineId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
}
