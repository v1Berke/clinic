package com.phermacyrepo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDTO {
    private int id;
    private LocalDateTime saleDate;
    private double totalPrice;
    private int totalQuantity;
    private List<SaleItemDTO> items = new ArrayList<>();

    public SaleDTO() {}

    public SaleDTO(int id, LocalDateTime saleDate, double totalPrice,
                   int totalQuantity, List<SaleItemDTO> items) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
        this.items = items;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public List<SaleItemDTO> getItems() { return items; }
    public void setItems(List<SaleItemDTO> items) { this.items = items; }
}
