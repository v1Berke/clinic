package com.phermacyrepo.domain.entity;
import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.domain.exceptions.*;
import java.time.LocalDate;

public class Medicine {

    private int id;
    private String name;
    private String barcode;
    private MedicineType type;
    private LocalDate expirationDate;
    private double purchasePrice;
    private double salePrice;
    private int stock;
    private boolean active;

    public Medicine(
            String name,
            String barcode,
            MedicineType type,
            LocalDate expirationDate,
            double purchasePrice,
            double salePrice,
            int stock
    ) {
        validateFields(name, barcode, type, expirationDate, purchasePrice, salePrice, stock);

        this.name = name;
        this.barcode = barcode;
        this.type = type;
        this.expirationDate = expirationDate;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.stock = stock;
        this.active = true;
    }

    private static void validateFields(
            String name,
            String barcode,
            MedicineType type,
            LocalDate expirationDate,
            double purchasePrice,
            double salePrice,
            int stock
    ) {
        if (name == null || name.isBlank()) throw new ValidationException("Medicine name cannot be empty");
        if (barcode == null || barcode.isBlank()) throw new ValidationException("Barcode cannot be empty");
        if (type == null) throw new ValidationException("Medicine type cannot be null");
        if (expirationDate == null) throw new ValidationException("Expiration date cannot be null");
        if (purchasePrice <= 0) throw new ValidationException("Purchase price must be positive");
        if (salePrice <= 0) throw new ValidationException("Sale price must be positive");
        if (stock < 0) throw new ValidationException("Stock cannot be negative");
    }

    /**
     * Ilac bilgilerini gunceller. Stok ve aktiflik haric tum alanlar
     * service katmanindan bu metotla degistirilir (dogudella SQL yok).
     */
    public void updateDetails(
            String name,
            String barcode,
            MedicineType type,
            LocalDate expirationDate,
            double purchasePrice,
            double salePrice,
            int stock
    ) {
        validateFields(name, barcode, type, expirationDate, purchasePrice, salePrice, stock);

        this.name = name;
        this.barcode = barcode;
        this.type = type;
        this.expirationDate = expirationDate;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.stock = stock;
    }

    public void setId(int id) {
        if (id <= 0) throw new ValidationException("Invalid id");
        this.id = id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new ValidationException("Quantity must be positive");
        if (stock < quantity) throw new BusinessRuleException("Insufficient stock");
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new ValidationException("Quantity must be positive");
        stock += quantity;
    }

    public void deactivate() {this.active = false;}

    // getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public MedicineType getType() { return type; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public double getPurchasePrice() { return purchasePrice; }
    public double getSalePrice() { return salePrice; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }
}
