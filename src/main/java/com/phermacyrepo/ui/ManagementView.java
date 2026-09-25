package com.phermacyrepo.ui;

import com.phermacyrepo.domain.enum_.MedicineType;
import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.MedicineRequestDTO;
import com.phermacyrepo.dto.SaleDTO;
import com.phermacyrepo.dto.SaleItemDTO;
import com.phermacyrepo.dto.StockMovementDTO;
import com.phermacyrepo.service.MedicineService;
import com.phermacyrepo.service.SaleService;
import com.phermacyrepo.service.StockMovementService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

/**
 * MODE 2 - Yonetim ekrani. Tum islemler mevcut servisler uzerinden
 * yapilir; hayali ozellik yoktur.
 */
public class ManagementView {

    private final MedicineService medicineService;
    private final SaleService saleService;
    private final StockMovementService stockMovementService;

    private TableView<MedicineDTO> medicineTable;
    private ComboBox<MedicineDTO> stockMedicineBox;
    private TableView<StockMovementDTO> movementTable;
    private CheckBox onlySelectedBox;
    private TableView<SaleDTO> saleTable;
    private TableView<SaleItemDTO> saleItemTable;

    public ManagementView(MedicineService medicineService,
                          SaleService saleService,
                          StockMovementService stockMovementService) {
        this.medicineService = medicineService;
        this.saleService = saleService;
        this.stockMovementService = stockMovementService;
    }

    public Node view() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab medicineTab = new Tab("\u0130la\u00E7lar", medicinePane());
        Tab stockTab = new Tab("Stok", stockPane());
        Tab salesTab = new Tab("Sat\u0131\u015F Ge\u00E7mi\u015Fi", salesPane());
        tabs.getTabs().addAll(medicineTab, stockTab, salesTab);
        return tabs;
    }

    /** Moda her geciste cagrilir. */
    public void onShown() {
        refreshMedicines();
        refreshStockMedicines();
        refreshMovements();
        refreshSales();
    }

    // ---------- ILACLAR ----------

    private Node medicinePane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));

        medicineTable = new TableView<>();
        medicineTable.setPlaceholder(new Label("Kay\u0131tl\u0131 ila\u00E7 yok"));
        medicineTable.getColumns().add(idCol("ID", "id", 60));
        medicineTable.getColumns().add(textCol("\u0130la\u00E7", "name", 170));
        medicineTable.getColumns().add(textCol("Barkod", "barcode", 140));
        TableColumn<MedicineDTO, String> kindCol = new TableColumn<>("T\u00FCr");
        kindCol.setCellValueFactory(c ->
                new SimpleStringProperty(UiHelpers.medicineTypeText(c.getValue().getType())));
        kindCol.setMaxWidth(170);
        medicineTable.getColumns().add(kindCol);
        medicineTable.getColumns().add(moneyCol("Al\u0131\u015F", MedicineDTO::getPurchasePrice, 100));
        medicineTable.getColumns().add(moneyCol("Sat\u0131\u015F", MedicineDTO::getSalePrice, 100));
        medicineTable.getColumns().add(idCol("Stok", "stock", 70));

        Button addButton = new Button("Yeni \u0130la\u00E7");
        addButton.getStyleClass().add("normal-button");
        addButton.setOnAction(e -> openMedicineDialog(null));

        Button editButton = new Button("D\u00FCzenle");
        editButton.getStyleClass().add("normal-button");
        editButton.setOnAction(e -> {
            MedicineDTO selected = medicineTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UiHelpers.info("Se\u00E7im yok", "L\u00FCtfen \u00F6nce listeden bir ila\u00E7 se\u00E7in.");
                return;
            }
            openMedicineDialog(selected);
        });

        Button deleteButton = new Button("Sil");
        deleteButton.getStyleClass().add("normal-button");
        deleteButton.setOnAction(e -> deleteSelectedMedicine());

        Button refreshButton = new Button("Yenile");
        refreshButton.getStyleClass().add("normal-button");
        refreshButton.setOnAction(e -> refreshMedicines());

        HBox buttons = new HBox(10, addButton, editButton, deleteButton, refreshButton);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        root.setCenter(medicineTable);
        root.setBottom(buttons);
        return root;
    }

    private void openMedicineDialog(MedicineDTO existing) {
        Dialog<MedicineRequestDTO> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Yeni \u0130la\u00E7" : "\u0130lac\u0131 D\u00FCzenle");

        TextField nameField = new TextField();
        TextField barcodeField = new TextField();
        ComboBox<MedicineType> typeBox = new ComboBox<>(
                FXCollections.observableArrayList(MedicineType.values()));
        typeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MedicineType type) {
                return UiHelpers.medicineTypeText(type);
            }
            @Override
            public MedicineType fromString(String s) { return null; }
        });
        typeBox.setCellFactory(box -> new ListCell<>() {
            @Override
            protected void updateItem(MedicineType type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty || type == null ? null : UiHelpers.medicineTypeText(type));
            }
        });
        TextField purchaseField = new TextField();
        TextField saleField = new TextField();
        TextField stockField = new TextField();

        if (existing != null) {
            nameField.setText(existing.getName());
            barcodeField.setText(existing.getBarcode());
            typeBox.setValue(existing.getType());
            purchaseField.setText(String.valueOf(existing.getPurchasePrice()));
            saleField.setText(String.valueOf(existing.getSalePrice()));
            stockField.setText(String.valueOf(existing.getStock()));
        } else {
            stockField.setText("0");
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("\u0130la\u00E7 Ad\u0131:"), nameField);
        grid.addRow(1, new Label("Barkod:"), barcodeField);
        grid.addRow(2, new Label("T\u00FCr:"), typeBox);
        grid.addRow(3, new Label("Al\u0131\u015F Fiyat\u0131:"), purchaseField);
        grid.addRow(4, new Label("Sat\u0131\u015F Fiyat\u0131:"), saleField);
        grid.addRow(5, new Label("Stok:"), stockField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            try {
                return new MedicineRequestDTO(
                        nameField.getText().trim(),
                        barcodeField.getText().trim(),
                        typeBox.getValue(),
                        Double.parseDouble(purchaseField.getText().trim().replace(',', '.')),
                        Double.parseDouble(saleField.getText().trim().replace(',', '.')),
                        Integer.parseInt(stockField.getText().trim()));
            } catch (NumberFormatException ex) {
                UiHelpers.error("Ge\u00E7ersiz say\u0131",
                        "Fiyat ve stok alanlar\u0131na ge\u00E7erli say\u0131lar girin.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(request -> {
            try {
                if (existing == null) {
                    medicineService.createMedicine(request);
                    UiHelpers.info("Kaydedildi", "\u0130la\u00E7 ba\u015Far\u0131yla eklendi.");
                } else {
                    medicineService.updateMedicine(existing.getId(), request);
                    UiHelpers.info("G\u00FCncellendi", "\u0130la\u00E7 ba\u015Far\u0131yla g\u00FCncellendi.");
                }
                refreshMedicines();
            } catch (RuntimeException e) {
                UiHelpers.error("Kaydedilemedi", UiHelpers.friendlyError(e));
            }
        });
    }

    private void deleteSelectedMedicine() {
        MedicineDTO selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiHelpers.info("Se\u00E7im yok", "L\u00FCtfen \u00F6nce listeden bir ila\u00E7 se\u00E7in.");
            return;
        }
        boolean ok = UiHelpers.confirm("'" + selected.getName() + "' silinsin mi?",
                "Bu i\u015Flem geri al\u0131namaz.");
        if (!ok) {
            return;
        }
        try {
            medicineService.deleteMedicine(selected.getId());
            UiHelpers.info("Silindi", "\u0130la\u00E7 ba\u015Far\u0131yla silindi.");
            refreshMedicines();
        } catch (RuntimeException e) {
            UiHelpers.error("Silinemedi", UiHelpers.friendlyError(e));
        }
    }

    private void refreshMedicines() {
        try {
            medicineTable.setItems(FXCollections.observableArrayList(
                    medicineService.getAllMedicines()));
        } catch (RuntimeException e) {
            UiHelpers.error("Liste al\u0131namad\u0131", UiHelpers.friendlyError(e));
        }
    }

    // ---------- STOK ----------

    private Node stockPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));

        stockMedicineBox = new ComboBox<>();
        stockMedicineBox.setPrefWidth(360);
        stockMedicineBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MedicineDTO m) {
                return m == null ? "" : m.getName() + "  (Stok: " + m.getStock() + ")";
            }
            @Override
            public MedicineDTO fromString(String s) { return null; }
        });

        TextField qtyField = new TextField();
        qtyField.setPromptText("Adet");
        qtyField.setPrefWidth(120);

        TextField noteField = new TextField();
        noteField.setPromptText("A\u00E7\u0131klama (zorunlu)");
        HBox.setHgrow(noteField, Priority.ALWAYS);

        Button inButton = new Button("Stok Ekle");
        inButton.getStyleClass().add("normal-button");
        inButton.setOnAction(e -> moveStock(qtyField, noteField, true));

        Button outButton = new Button("Stok \u00C7\u0131kar");
        outButton.getStyleClass().add("normal-button");
        outButton.setOnAction(e -> moveStock(qtyField, noteField, false));

        HBox form = new HBox(10, new Label("\u0130la\u00E7:"), stockMedicineBox,
                new Label("Adet:"), qtyField, noteField, inButton, outButton);
        form.setAlignment(Pos.CENTER_LEFT);

        movementTable = new TableView<>();
        movementTable.setPlaceholder(new Label("Hareket kayd\u0131 yok"));
        movementTable.getColumns().add(idCol("ID", "id", 70));
        movementTable.getColumns().add(textCol("\u0130la\u00E7", "medicineName", 220));
        TableColumn<StockMovementDTO, String> dirCol = new TableColumn<>("Y\u00F6n");
        dirCol.setCellValueFactory(c -> new SimpleStringProperty(directionText(c.getValue())));
        dirCol.setMaxWidth(100);
        movementTable.getColumns().add(dirCol);
        movementTable.getColumns().add(idCol("Adet", "quantity", 90));
        movementTable.getColumns().add(dateTimeCol("Tarih", StockMovementDTO::getDate, 170));
        movementTable.getColumns().add(textCol("A\u00E7\u0131klama", "reason", 260));

        onlySelectedBox = new CheckBox("Sadece se\u00E7ili ilac\u0131 g\u00F6ster");
        onlySelectedBox.setOnAction(e -> refreshMovements());
        stockMedicineBox.setOnAction(e -> {
            if (onlySelectedBox.isSelected()) {
                refreshMovements();
            }
        });

        Button refreshButton = new Button("Yenile");
        refreshButton.getStyleClass().add("normal-button");
        refreshButton.setOnAction(e -> {
            refreshStockMedicines();
            refreshMovements();
        });
        HBox bottom = new HBox(10, onlySelectedBox, refreshButton);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(12, 0, 0, 0));

        root.setTop(form);
        root.setCenter(movementTable);
        root.setBottom(bottom);
        BorderPane.setMargin(movementTable, new Insets(14, 0, 0, 0));
        return root;
    }

    private void moveStock(TextField qtyField, TextField noteField, boolean entry) {
        MedicineDTO selected = stockMedicineBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiHelpers.info("Se\u00E7im yok", "L\u00FCtfen \u00F6nce bir ila\u00E7 se\u00E7in.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException | NullPointerException ex) {
            UiHelpers.error("Ge\u00E7ersiz adet", "Adet alan\u0131na pozitif bir tam say\u0131 girin.");
            return;
        }
        try {
            if (entry) {
                stockMovementService.registerStockEntry(
                        selected.getId(), qty, noteField.getText());
                UiHelpers.info("Tamam", "Stok ba\u015Far\u0131yla eklendi.");
            } else {
                stockMovementService.removeStockEntry(selected.getId(), qty, noteField.getText());
                UiHelpers.info("Tamam", "Stok ba\u015Far\u0131yla d\u00FC\u015F\u00FCld\u00FC.");
            }
            refreshStockMedicines();
            refreshMovements();
        } catch (RuntimeException e) {
            UiHelpers.error("\u0130\u015Flem yap\u0131lamad\u0131", UiHelpers.friendlyError(e));
        }
    }

    private void refreshStockMedicines() {
        try {
            MedicineDTO keep = stockMedicineBox.getSelectionModel().getSelectedItem();
            List<MedicineDTO> all = medicineService.getAllMedicines();
            stockMedicineBox.setItems(FXCollections.observableArrayList(all));
            if (keep != null) {
                all.stream().filter(m -> m.getId() == keep.getId()).findFirst()
                        .ifPresent(stockMedicineBox.getSelectionModel()::select);
            }
        } catch (RuntimeException e) {
            UiHelpers.error("Liste al\u0131namad\u0131", UiHelpers.friendlyError(e));
        }
    }

    private void refreshMovements() {
        try {
            List<StockMovementDTO> movements;
            MedicineDTO selected = stockMedicineBox.getSelectionModel().getSelectedItem();
            if (onlySelectedBox.isSelected() && selected != null) {
                movements = stockMovementService.getMovementsByMedicineId(selected.getId());
            } else {
                movements = stockMovementService.getAllMovements();
            }
            movementTable.setItems(FXCollections.observableArrayList(movements));
        } catch (RuntimeException e) {
            UiHelpers.error("Liste al\u0131namad\u0131", UiHelpers.friendlyError(e));
        }
    }

    // ---------- SATIS GECMISI ----------

    private Node salesPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));

        saleTable = new TableView<>();
        saleTable.setPlaceholder(new Label("Sat\u0131\u015F kayd\u0131 yok"));
        saleTable.getColumns().add(idCol("Sat\u0131\u015F No", "id", 110));
        saleTable.getColumns().add(dateTimeCol("Tarih", SaleDTO::getSaleDate, 180));
        saleTable.getColumns().add(idCol("Toplam Adet", "totalQuantity", 140));
        saleTable.getColumns().add(moneyCol("Toplam Tutar", SaleDTO::getTotalPrice, 160));

        saleItemTable = new TableView<>();
        saleItemTable.setPlaceholder(new Label("Sat\u0131\u015F se\u00E7ilmedi"));
        saleItemTable.getColumns().add(textCol("\u0130la\u00E7", "medicineName", 240));
        saleItemTable.getColumns().add(idCol("Adet", "quantity", 100));
        saleItemTable.getColumns().add(moneyCol("Birim Fiyat", SaleItemDTO::getUnitPrice, 140));
        saleItemTable.getColumns().add(moneyCol("Toplam", SaleItemDTO::getTotalPrice, 140));

        saleTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sale) -> {
            if (sale != null) {
                saleItemTable.setItems(FXCollections.observableArrayList(sale.getItems()));
            } else {
                saleItemTable.getItems().clear();
            }
        });

        VBox lists = new VBox(10,
                new Label("Sat\u0131\u015Flar"), saleTable,
                new Label("Se\u00E7ili sat\u0131\u015F\u0131n kalemleri"), saleItemTable);
        VBox.setVgrow(saleTable, Priority.ALWAYS);

        Button refreshButton = new Button("Yenile");
        refreshButton.getStyleClass().add("normal-button");
        refreshButton.setOnAction(e -> refreshSales());
        HBox bottom = new HBox(refreshButton);
        bottom.setPadding(new Insets(12, 0, 0, 0));

        root.setCenter(lists);
        root.setBottom(bottom);
        return root;
    }

    private void refreshSales() {
        try {
            List<SaleDTO> sales = saleService.getAllSales();
            saleTable.setItems(FXCollections.observableArrayList(sales));
            saleItemTable.getItems().clear();
        } catch (RuntimeException e) {
            UiHelpers.error("Liste al\u0131namad\u0131", UiHelpers.friendlyError(e));
        }
    }

    // ---------- Tablo sutun yardimcilari ----------

    private static String directionText(StockMovementDTO movement) {
        if (movement == null || movement.getType() == null) {
            return "-";
        }
        return switch (movement.getType()) {
            case IN -> "Giri\u015F";
            case OUT -> "\u00C7\u0131k\u0131\u015F";
            case CANCEL -> "\u0130ptal";
        };
    }

    private <S> TableColumn<S, Integer> idCol(String title, String property, int width) {
        TableColumn<S, Integer> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMaxWidth(width);
        return col;
    }

    private <S> TableColumn<S, String> textCol(String title, String property, int width) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    private <S> TableColumn<S, String> moneyCol(String title,
                                                 java.util.function.Function<S, Number> getter,
                                                 int width) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> {
            Number value = getter.apply(cell.getValue());
            return new SimpleStringProperty(
                    UiHelpers.money(value == null ? 0 : value.doubleValue()));
        });
        col.setMaxWidth(width);
        return col;
    }

    private <S> TableColumn<S, String> dateTimeCol(String title,
                                                  java.util.function.Function<S, java.time.LocalDateTime> getter,
                                                  int width) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell ->
                new SimpleStringProperty(UiHelpers.dateTime(getter.apply(cell.getValue()))));
        col.setMaxWidth(width);
        return col;
    }
}
