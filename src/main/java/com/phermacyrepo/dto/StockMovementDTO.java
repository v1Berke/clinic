package com.phermacyrepo.dto;

import com.phermacyrepo.domain.enum_.StockMovementType;
import java.time.LocalDateTime;

public class StockMovementDTO {
    private int id;
    private int medicineId;
    private String medicineName;
    private StockMovementType type;
    private int quantity;
    private LocalDateTime date;
    private String reason;

    public StockMovementDTO() {}

    public StockMovementDTO(int id, int medicineId, String medicineName,
                            StockMovementType type, int quantity,
                            LocalDateTime date, String reason) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.reason = reason;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public StockMovementType getType() { return type; }
    public void setType(StockMovementType type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
