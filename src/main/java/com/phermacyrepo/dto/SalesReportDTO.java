package com.phermacyrepo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Belirli bir gunun ilac bazinda satis dokumu + toplamlar. */
public class SalesReportDTO {

    public static class Line {
        private int medicineId;
        private String medicineName;
        private int quantity;
        private double revenue;
        private double profit;

        public Line() {}

        public Line(int medicineId, String medicineName, int quantity,
                    double revenue, double profit) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.quantity = quantity;
            this.revenue = revenue;
            this.profit = profit;
        }

        public int getMedicineId() { return medicineId; }
        public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }

        public double getProfit() { return profit; }
        public void setProfit(double profit) { this.profit = profit; }
    }

    private LocalDate date;
    private List<Line> lines = new ArrayList<>();
    private int totalQuantity;
    private double totalRevenue;
    private double totalProfit;
    private int saleCount;

    public SalesReportDTO() {}

    public SalesReportDTO(LocalDate date, List<Line> lines, int totalQuantity,
                          double totalRevenue, double totalProfit, int saleCount) {
        this.date = date;
        this.lines = lines;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
        this.saleCount = saleCount;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }

    public int getSaleCount() { return saleCount; }
    public void setSaleCount(int saleCount) { this.saleCount = saleCount; }
}
