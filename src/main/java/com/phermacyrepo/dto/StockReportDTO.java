package com.phermacyrepo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Belirli bir gunun ilac bazinda stok hareket dokumu + toplamlar. */
public class StockReportDTO {

    public static class Line {
        private int medicineId;
        private String medicineName;
        private int inQuantity;
        private int outQuantity;

        public Line() {}

        public Line(int medicineId, String medicineName, int inQuantity, int outQuantity) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.inQuantity = inQuantity;
            this.outQuantity = outQuantity;
        }

        public int getMedicineId() { return medicineId; }
        public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public int getInQuantity() { return inQuantity; }
        public void setInQuantity(int inQuantity) { this.inQuantity = inQuantity; }

        public int getOutQuantity() { return outQuantity; }
        public void setOutQuantity(int outQuantity) { this.outQuantity = outQuantity; }

        public int getNetQuantity() { return inQuantity - outQuantity; }
    }

    private LocalDate date;
    private List<Line> lines = new ArrayList<>();
    private int totalIn;
    private int totalOut;

    public StockReportDTO() {}

    public StockReportDTO(LocalDate date, List<Line> lines, int totalIn, int totalOut) {
        this.date = date;
        this.lines = lines;
        this.totalIn = totalIn;
        this.totalOut = totalOut;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public int getTotalIn() { return totalIn; }
    public void setTotalIn(int totalIn) { this.totalIn = totalIn; }

    public int getTotalOut() { return totalOut; }
    public void setTotalOut(int totalOut) { this.totalOut = totalOut; }

    public int getNetTotal() { return totalIn - totalOut; }
}
