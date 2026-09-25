package com.phermacyrepo.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Sale {
    private int saleId;
    private LocalDateTime saleDate;
    private List<SaleItem> items;

    public Sale(LocalDateTime saleDate) {
        this(0, saleDate);
    }

    public Sale(int saleId, LocalDateTime saleDate) {
        if (saleId < 0) throw new IllegalArgumentException("Sale id cannot be negative");
        if (saleDate == null) throw new IllegalArgumentException("Sale date cannot be null");
        if (saleDate.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Sale date cannot be in the future");
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.items = new ArrayList<>();
    }

    public void setSaleId(int saleId) {
        if (saleId <= 0) throw new IllegalArgumentException("Sale id must be positive");
        this.saleId = saleId;
    }
    
    public void addItem(SaleItem item) {
        if (item == null) throw new IllegalArgumentException("Sale item cannot be null");
        items.add(item);
    }
    
    public void removeItem(SaleItem item) {
        if (item == null) throw new IllegalArgumentException("Sale item cannot be null");
        items.remove(item);
    }
    
    public double getTotalPrice() {
        return items.stream()
            .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
            .sum();
    }
    
    public int getTotalQuantity() {
        return items.stream()
            .mapToInt(SaleItem::getQuantity)
            .sum();
    }

    public int getSaleId() { return saleId; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public List<SaleItem> getItems() { return Collections.unmodifiableList(items); }
}