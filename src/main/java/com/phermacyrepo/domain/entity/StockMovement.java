package com.phermacyrepo.domain.entity;
import com.phermacyrepo.domain.enum_.StockMovementType;
import com.phermacyrepo.domain.exceptions.ValidationException;
import java.time.LocalDateTime;

public class StockMovement {

    private int id; 
    private final int medicineId;
    private final StockMovementType type;
    private final int quantity;
    private final LocalDateTime date;
    private final String reason;

    public StockMovement(
            int medicineId,
            StockMovementType type,
            int quantity,
            LocalDateTime date,
            String reason
    ) {
        if (medicineId <= 0) throw new ValidationException("Invalid medicine id");
        if (type == null) throw new ValidationException("Movement type cannot be null");
        if (quantity <= 0) throw new ValidationException("Quantity must be positive");
        if (date == null || date.isAfter(LocalDateTime.now()))
            throw new ValidationException("Invalid date");
        if (reason == null || reason.isBlank())
            throw new ValidationException("Reason cannot be empty");

        this.medicineId = medicineId;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.reason = reason;
    }

    public void setId(int id) {
        if (id <= 0) throw new ValidationException("Invalid stock movement id");
        this.id = id;
    }

    public int getId() { return id; }
    public int getMedicineId() { return medicineId; }
    public StockMovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
    public String getReason() { return reason; }
}
