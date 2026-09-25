package com.phermacyrepo.ui;

import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.SaleCreateDTO;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

/**
 * Aktif satis sepeti. Tekrar okutulan barkod ayni satira adet olarak
 * islenir (xN). JavaFX toolkit baslatilmadan da test edilebilir.
 */
public class CartModel {

    public static class Row {
        private final int medicineId;
        private final String name;
        private final double unitPrice;
        private final int availableStock;
        private final IntegerProperty quantity = new SimpleIntegerProperty(1);

        public Row(int medicineId, String name, double unitPrice, int availableStock) {
            this.medicineId = medicineId;
            this.name = name;
            this.unitPrice = unitPrice;
            this.availableStock = availableStock;
        }

        public int getMedicineId() { return medicineId; }
        public String getName() { return name; }
        public double getUnitPrice() { return unitPrice; }
        public int getAvailableStock() { return availableStock; }
        public int getQuantity() { return quantity.get(); }
        public IntegerProperty quantityProperty() { return quantity; }
        public void setQuantity(int quantity) { this.quantity.set(quantity); }
        public double getLineTotal() { return unitPrice * quantity.get(); }
    }

    private final ObservableList<Row> rows = FXCollections.observableArrayList();

    public ObservableList<Row> getRows() { return rows; }

    /** Ilaci sepete ekler; sepette varsa adedini bir artirir. */
    public void add(MedicineDTO medicine) {
        for (Row row : rows) {
            if (row.getMedicineId() == medicine.getId()) {
                row.setQuantity(row.getQuantity() + 1);
                return;
            }
        }
        rows.add(new Row(medicine.getId(), medicine.getName(),
                medicine.getSalePrice(), medicine.getStock()));
    }

    public void remove(Row row) { rows.remove(row); }

    public void clear() { rows.clear(); }

    public boolean isEmpty() { return rows.isEmpty(); }

    public int quantityOf(int medicineId) {
        for (Row row : rows) {
            if (row.getMedicineId() == medicineId) {
                return row.getQuantity();
            }
        }
        return 0;
    }

    public int getTotalQuantity() {
        return rows.stream().mapToInt(Row::getQuantity).sum();
    }

    public double getTotalPrice() {
        return rows.stream().mapToDouble(Row::getLineTotal).sum();
    }

    public SaleCreateDTO toSaleCreateDTO() {
        return new SaleCreateDTO(rows.stream()
                .map(r -> new SaleCreateDTO.SaleItemRequest(r.getMedicineId(), r.getQuantity()))
                .collect(Collectors.toList()));
    }
}
