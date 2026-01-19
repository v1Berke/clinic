package com.phermacyrepo.domain;

public class SaleItem {
    private int id;
    private int saleId;
    private int medicineId;
    private int quantitySold;
    private double unitPrice;

    public SaleItem(int id, int saleId, int medicineId, int quantitySold, double unitPrice) {
        if (id <= 0) throw new IllegalArgumentException("Sale item id must be positive");
        if (saleId <= 0) throw new IllegalArgumentException("Sale id must be positive");
        if (medicineId <= 0) throw new IllegalArgumentException("Medicine id must be positive");
        if (quantitySold <= 0) throw new IllegalArgumentException("Quantity sold must be positive");
        if (unitPrice <= 0) throw new IllegalArgumentException("Unit price must be positive");
        this.id = id;
        this.saleId = saleId;
        this.medicineId = medicineId;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
    }

    public int getId() {return id;}
    public int getSaleId() {return saleId;}
    public int getMedicineId() {return medicineId;}
    public int getQuantitySold() {return quantitySold;}
    public double getUnitPrice() {return unitPrice;}

}

