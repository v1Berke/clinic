package com.phermacyrepo.domain;

import java.time.LocalDateTime;

public class StockMovement {
    private int id;
    private int medicineId;
    private StockMovementType type;
    private int quantity;
    private LocalDateTime date;
    private String reason;

    public int getId() { return id; }
    public int getMedicineId() { return medicineId; }
    public StockMovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
    public String getReason() { return reason; }

    public StockMovement(int id, int medicineId, StockMovementType type, int quantity, LocalDateTime date, String reason) {
        if (id <= 0) throw new IllegalArgumentException("Stock movement id must be positive");
        if (medicineId <= 0) throw new IllegalArgumentException("Medicine id must be positive");
        if (type == null) throw new IllegalArgumentException("Stock movement type cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        if (date.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Date cannot be in the future");
        if (reason == null || reason.trim().isEmpty()) throw new IllegalArgumentException("Reason cannot be null or empty");
        this.id = id;
        this.medicineId = medicineId;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.reason = reason;
    }
}

enum StockMovementType {
    IN,
    OUT,
    CANCEL
}
