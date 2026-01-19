package com.phermacyrepo.domain;

import java.time.LocalDate;

public class Medicine {
    private String name;
    private int id;
    private String barcode;
    private MedicineType type;
    private LocalDate expirationDate;
    private double price;
    private int stock;

    public String getName() {return name;}
    public int getId() {return id;}
    public String getBarcode() {return barcode;}
    public MedicineType getType() {return type;}
    public LocalDate getExpirationDate() {return expirationDate;}
    public double getPrice() {return price;}
    public int getStock() {return stock;}
    
    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative");
        this.stock = stock;
    }
    
    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (this.stock < quantity) throw new IllegalArgumentException("Insufficient stock. Available: " + this.stock + ", Requested: " + quantity);
        this.stock -= quantity;
    }
    
    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.stock += quantity;
    }

    public Medicine(String name, int id, String barcode, MedicineType type, LocalDate expirationDate, double price, int stock) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Medicine name cannot be null or empty");
        if (id <= 0) throw new IllegalArgumentException("Medicine id must be positive");
        if (barcode == null || barcode.trim().isEmpty()) throw new IllegalArgumentException("Barcode cannot be null or empty");
        if (type == null) throw new IllegalArgumentException("Medicine type cannot be null");
        if (expirationDate == null) throw new IllegalArgumentException("Expiration date cannot be null");
        if (expirationDate.isBefore(LocalDate.now())) throw new IllegalArgumentException("Expiration date cannot be in the past");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative");
        this.name = name;
        this.id = id;
        this.barcode = barcode;
        this.type = type;
        this.expirationDate = expirationDate;
        this.price = price;
        this.stock = stock;
    }
}

enum MedicineType {
    TABLET,
    SYRUP,
    INJECTION,
    OINTMENT
}
