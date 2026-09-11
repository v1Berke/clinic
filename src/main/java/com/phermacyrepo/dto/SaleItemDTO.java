package com.phermacyrepo.dto;

public class SaleItemDTO {
    private int id;
    private int medicineId;
    private String medicineName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public SaleItemDTO() {}

    public SaleItemDTO(int id, int medicineId, String medicineName,
                       int quantity, double unitPrice, double totalPrice) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
