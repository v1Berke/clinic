package com.phermacyrepo.dto;

import com.phermacyrepo.domain.enum_.MedicineType;

public class MedicineRequestDTO {
    private String name;
    private String barcode;
    private MedicineType type;
    private double purchasePrice;
    private double salePrice;
    private int stock;

    public MedicineRequestDTO() {}

    public MedicineRequestDTO(String name, String barcode, MedicineType type,
                              double purchasePrice,
                              double salePrice, int stock) {
        this.name = name;
        this.barcode = barcode;
        this.type = type;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.stock = stock;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public MedicineType getType() { return type; }
    public void setType(MedicineType type) { this.type = type; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}