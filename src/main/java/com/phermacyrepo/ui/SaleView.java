package com.phermacyrepo.ui;

import com.phermacyrepo.dto.MedicineDTO;
import com.phermacyrepo.dto.SaleDTO;
import com.phermacyrepo.service.MedicineService;
import com.phermacyrepo.service.SaleService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * MODE 1 - Satis ekrani. USB barkod okuyucu klavye gibi calisir:
 * barkod + Enter. Odak her islemden sonra barkod alanina doner.
 */
public class SaleView {

    private final MedicineService medicineService;
    private final SaleService saleService;
    private final CartModel cart = new CartModel();

    private List<MedicineDTO> catalog = new ArrayList<>();
    private TextField barcodeField;
    private Label totalLabel;
    private Label countLabel;
    private Label readyLabel;
    private Button completeButton;

    public SaleView(MedicineService medicineService, SaleService saleService) {
        this.medicineService = medicineService;
        this.saleService = saleService;
    }

    public Node view() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));

        Label title = new Label("Mevcut Sat\u0131\u015F");
        title.getStyleClass().add("section-title");

        readyLabel = new Label("Sat\u0131\u015Fa haz\u0131r \u2014 barkod okutarak ba\u015Flay\u0131n.");
        readyLabel.getStyleClass().add("ready-label");
        readyLabel.setVisible(true);

        barcodeField = new TextField();
        barcodeField.setPromptText("Barkodu okutun veya yaz\u0131p Enter'a bas\u0131n");
        barcodeField.getStyleClass().add("barcode-field");
        barcodeField.setOnAction(e -> handleScan());

        Button manualButton = new Button("Listeden Ekle");
        manualButton.getStyleClass().add("normal-button");
        manualButton.setOnAction(e -> openManualAddDialog());

        HBox topRow = new HBox(12, barcodeField, manualButton);
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(barcodeField, Priority.ALWAYS);

        VBox header = new VBox(8, title, readyLabel, topRow);

        TableView<CartModel.Row> table = buildCartTable();
        table.setItems(cart.getRows());
        table.setPlaceholder(new Label("Sepet bo\u015F"));

        totalLabel = new Label(UiHelpers.money(0));
        totalLabel.getStyleClass().add("total-label");
        countLabel = new Label("\u00DCr\u00FCn: 0");
        countLabel.getStyleClass().add("count-label");
        HBox totalRow = new HBox(16, countLabel, totalLabel);
        totalRow.setAlignment(Pos.CENTER_RIGHT);

        completeButton = new Button("SATI\u015EI TAMAMLA");
        completeButton.getStyleClass().add("primary-button");
        completeButton.setDisable(true);
        completeButton.setDefaultButton(true);
        completeButton.setOnAction(e -> completeSale());

        Button cancelButton = new Button("SATI\u015EI \u0130PTAL ET");
        cancelButton.getStyleClass().add("danger-button");
        cancelButton.setOnAction(e -> cancelSale());

        HBox buttons = new HBox(16, cancelButton, completeButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox bottom = new VBox(12, totalRow, buttons);

        root.setTop(header);
        root.setCenter(table);
        root.setBottom(bottom);
        BorderPane.setMargin(table, new Insets(14, 0, 14, 0));

        refreshTotals();
        return root;
    }

    /** Moda her geciste cagrilir: katalog tazelenir, odak barkoda verilir. */
    public void onShown() {
        reloadCatalog();
        focusBarcode();
    }

    private TableView<CartModel.Row> buildCartTable() {
        TableView<CartModel.Row> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<CartModel.Row, String> nameCol = new TableColumn<>("\u0130la\u00E7");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<CartModel.Row, Integer> qtyCol = new TableColumn<>("Adet");
        qtyCol.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());
        qtyCol.setMaxWidth(120);

        TableColumn<CartModel.Row, String> priceCol = new TableColumn<>("Birim Fiyat");
        priceCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        UiHelpers.money(cell.getValue().getUnitPrice())));
        priceCol.setMaxWidth(180);

        TableColumn<CartModel.Row, String> totalCol = new TableColumn<>("Toplam");
        totalCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        UiHelpers.money(cell.getValue().getLineTotal())));
        totalCol.setMaxWidth(180);

        TableColumn<CartModel.Row, Void> removeCol = new TableColumn<>("");
        removeCol.setMaxWidth(80);
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button remove = new Button("X");
            {
                remove.getStyleClass().add("remove-button");
                remove.setOnAction(e -> {
                    CartModel.Row row = getTableView().getItems().get(getIndex());
                    cart.remove(row);
                    refreshTotals();
                    focusBarcode();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : remove);
            }
        });

        table.getColumns().add(nameCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(totalCol);
        table.getColumns().add(removeCol);
        return table;
    }

    private void handleScan() {
        String code = barcodeField.getText() == null ? "" : barcodeField.getText().trim();
        if (code.isEmpty()) {
            focusBarcode();
            return;
        }
        MedicineDTO medicine = findByBarcode(code);
        if (medicine == null) {
            UiHelpers.error("\u0130la\u00E7 bulunamad\u0131",
                    "Okutulan barkod sistemdeki hi\u00E7bir ila\u00E7la e\u015Fle\u015Fmedi.\nBarkod: " + code);
            barcodeField.selectAll();
            focusBarcode();
            return;
        }
        String problem = sellProblem(medicine, cart.quantityOf(medicine.getId()) + 1);
        if (problem != null) {
            UiHelpers.error("Sat\u0131\u015F yap\u0131lamaz", problem);
            barcodeField.selectAll();
            focusBarcode();
            return;
        }
        cart.add(medicine);
        barcodeField.clear();
        refreshTotals();
        focusBarcode();
    }

    /** Istenen adet icin satis engeli varsa aciklama doner, yoksa null. */
    private String sellProblem(MedicineDTO medicine, int wantedQty) {
        if (medicine.getStock() < wantedQty) {
            return "'" + medicine.getName() + "' i\u00E7in yetersiz stok.\nStokta: "
                    + medicine.getStock();
        }
        return null;
    }

    private void completeSale() {
        if (cart.isEmpty()) {
            return;
        }
        try {
            SaleDTO sale = saleService.processSale(cart.toSaleCreateDTO());
            cart.clear();
            refreshTotals();
            reloadCatalog();
            UiHelpers.info("Sat\u0131\u015F tamamland\u0131",
                    "Sat\u0131\u015F ba\u015Far\u0131yla tamamland\u0131.\nToplam: "
                            + UiHelpers.money(sale.getTotalPrice()));
        } catch (RuntimeException e) {
            // Sepet korunur, kullanici duzeltip tekrar deneyebilir.
            UiHelpers.error("Sat\u0131\u015F tamamlanamad\u0131", UiHelpers.friendlyError(e));
        }
        focusBarcode();
    }

    private void cancelSale() {
        if (cart.isEmpty()) {
            focusBarcode();
            return;
        }
        boolean ok = UiHelpers.confirm("Mevcut sat\u0131\u015F iptal edilsin mi?",
                "Sepetteki t\u00FCm \u00FCr\u00FCnler sat\u0131\u015Ftan \u00E7\u0131kar\u0131lacak.");
        if (ok) {
            cart.clear();
            refreshTotals();
        }
        focusBarcode();
    }

    private void openManualAddDialog() {
        Dialog<MedicineDTO> dialog = new Dialog<>();
        dialog.setTitle("\u0130la\u00E7 Se\u00E7");
        dialog.setHeaderText("Sepete eklenecek ilac\u0131 se\u00E7in");

        TextField filter = new TextField();
        filter.setPromptText("Ada veya barkoda g\u00F6re ara...");
        ListView<MedicineDTO> list = new ListView<>();
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MedicineDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + "  |  " + item.getBarcode()
                            + "  |  " + UiHelpers.money(item.getSalePrice())
                            + "  |  Stok: " + item.getStock());
                }
            }
        });
        Runnable applyFilter = () -> {
            String q = filter.getText() == null ? "" : filter.getText().toLowerCase().trim();
            list.setItems(FXCollections.observableArrayList(catalog.stream()
                    .filter(m -> m.getName().toLowerCase().contains(q)
                            || m.getBarcode().toLowerCase().contains(q))
                    .toList()));
        };
        filter.textProperty().addListener((obs, old, val) -> applyFilter.run());
        applyFilter.run();

        VBox content = new VBox(10, filter, list);
        content.setPrefSize(520, 420);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.CANCEL,
                javafx.scene.control.ButtonType.OK);

        list.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                dialog.setResult(list.getSelectionModel().getSelectedItem());
                dialog.close();
            }
        });
        dialog.setResultConverter(btn ->
                btn == javafx.scene.control.ButtonType.OK
                        ? list.getSelectionModel().getSelectedItem() : null);

        dialog.showAndWait().ifPresent(medicine -> {
            String problem = sellProblem(medicine, cart.quantityOf(medicine.getId()) + 1);
            if (problem != null) {
                UiHelpers.error("Sat\u0131\u015F yap\u0131lamaz", problem);
            } else {
                cart.add(medicine);
                refreshTotals();
            }
        });
        focusBarcode();
    }

    private MedicineDTO findByBarcode(String barcode) {
        for (MedicineDTO medicine : catalog) {
            if (medicine.getBarcode().equalsIgnoreCase(barcode)) {
                return medicine;
            }
        }
        return null;
    }

    private void reloadCatalog() {
        try {
            catalog = medicineService.getAllMedicines();
        } catch (RuntimeException e) {
            UiHelpers.error("Liste al\u0131namad\u0131", UiHelpers.friendlyError(e));
        }
    }

    private void refreshTotals() {
        totalLabel.setText(UiHelpers.money(cart.getTotalPrice()));
        countLabel.setText("\u00DCr\u00FCn: " + cart.getTotalQuantity());
        readyLabel.setVisible(cart.isEmpty());
        completeButton.setDisable(cart.isEmpty());
    }

    private void focusBarcode() {
        Platform.runLater(() -> barcodeField.requestFocus());
    }
}
