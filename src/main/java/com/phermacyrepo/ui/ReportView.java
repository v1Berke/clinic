package com.phermacyrepo.ui;

import com.phermacyrepo.dto.SalesReportDTO;
import com.phermacyrepo.dto.StockReportDTO;
import com.phermacyrepo.service.ReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * MODE 3 - Gunluk rapor ekrani. Bilgisayar saatine gore bugunun,
 * tarih seciciyle istenen gunun raporunu gosterir.
 * Iki bolum: ilac satisi + stok degisimi.
 */
public class ReportView {

    private final ReportService reportService;

    private DatePicker datePicker;
    private HBox cardsRow;
    private TableView<SalesReportDTO.Line> salesTable;
    private TableView<StockReportDTO.Line> stockTable;

    public ReportView(ReportService reportService) {
        this.reportService = reportService;
    }

    public Node view() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));

        Label title = new Label("G\u00FCnl\u00FCk Rapor");
        title.getStyleClass().add("section-title");

        Button prevButton = new Button("\u25C0");
        prevButton.getStyleClass().add("normal-button");
        prevButton.setOnAction(e -> shiftDay(-1));

        Button nextButton = new Button("\u25B6");
        nextButton.getStyleClass().add("normal-button");
        nextButton.setOnAction(e -> shiftDay(1));

        Button todayButton = new Button("Bug\u00FCn");
        todayButton.getStyleClass().add("normal-button");
        todayButton.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            refresh();
        });

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(170);
        datePicker.setOnAction(e -> refresh());

        HBox dateRow = new HBox(10, prevButton, datePicker, nextButton, todayButton);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(10, title, dateRow);

        cardsRow = new HBox(12);

        salesTable = buildSalesTable();
        stockTable = buildStockTable();

        Label salesTitle = new Label("\u0130la\u00E7 Sat\u0131\u015F\u0131");
        salesTitle.getStyleClass().add("subsection-title");
        Label stockTitle = new Label("Stok De\u011Fi\u015Fimi");
        stockTitle.getStyleClass().add("subsection-title");

        VBox salesBox = new VBox(8, salesTitle, salesTable);
        VBox stockBox = new VBox(8, stockTitle, stockTable);
        HBox.setHgrow(salesBox, Priority.ALWAYS);
        HBox.setHgrow(stockBox, Priority.ALWAYS);
        VBox.setVgrow(salesTable, Priority.ALWAYS);
        VBox.setVgrow(stockTable, Priority.ALWAYS);

        HBox tables = new HBox(16, salesBox, stockBox);
        VBox.setVgrow(tables, Priority.ALWAYS);

        VBox center = new VBox(14, cardsRow, tables);
        VBox.setVgrow(tables, Priority.ALWAYS);

        root.setTop(header);
        root.setCenter(center);
        BorderPane.setMargin(center, new Insets(14, 0, 0, 0));

        refresh();
        return root;
    }

    /** Moda her geciste bugune donup tazeler. */
    public void onShown() {
        datePicker.setValue(LocalDate.now());
        refresh();
    }

    private void shiftDay(int days) {
        LocalDate current = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        LocalDate next = current.plusDays(days);
        if (next.isAfter(LocalDate.now())) {
            return;
        }
        datePicker.setValue(next);
        refresh();
    }

    private void refresh() {
        LocalDate date = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        try {
            SalesReportDTO sales = reportService.getDailySalesReport(date);
            StockReportDTO stock = reportService.getDailyStockReport(date);

            cardsRow.getChildren().setAll(
                    card("Sat\u0131\u015F Say\u0131s\u0131", String.valueOf(sales.getSaleCount()), false),
                    card("Sat\u0131lan \u00DCr\u00FCn", String.valueOf(sales.getTotalQuantity()), false),
                    card("Ciro", UiHelpers.money(sales.getTotalRevenue()), false),
                    card("K\u00E2r", UiHelpers.money(sales.getTotalProfit()), true),
                    card("Stok Giren / \u00C7\u0131kan",
                            stock.getTotalIn() + " / " + stock.getTotalOut(), false));

            salesTable.setItems(FXCollections.observableArrayList(sales.getLines()));
            stockTable.setItems(FXCollections.observableArrayList(stock.getLines()));
        } catch (RuntimeException e) {
            UiHelpers.error("Rapor al\u0131namad\u0131", UiHelpers.friendlyError(e));
        }
    }

    private VBox card(String title, String value, boolean highlight) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("report-card-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("report-card-value");
        if (highlight) {
            valueLabel.getStyleClass().add("profit-positive");
        }
        VBox card = new VBox(4, titleLabel, valueLabel);
        card.getStyleClass().add("report-card");
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private TableView<SalesReportDTO.Line> buildSalesTable() {
        TableView<SalesReportDTO.Line> table = new TableView<>();
        table.setPlaceholder(new Label("Bu g\u00FCnde sat\u0131\u015F yok"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<SalesReportDTO.Line, String> nameCol = new TableColumn<>("\u0130la\u00E7");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));

        TableColumn<SalesReportDTO.Line, Integer> qtyCol = new TableColumn<>("Adet");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(90);

        TableColumn<SalesReportDTO.Line, String> revenueCol = new TableColumn<>("Ciro");
        revenueCol.setCellValueFactory(c ->
                new SimpleStringProperty(UiHelpers.money(c.getValue().getRevenue())));
        revenueCol.setMaxWidth(130);

        TableColumn<SalesReportDTO.Line, String> profitCol = new TableColumn<>("K\u00E2r");
        profitCol.setCellValueFactory(c ->
                new SimpleStringProperty(UiHelpers.money(c.getValue().getProfit())));
        profitCol.setMaxWidth(130);

        table.getColumns().add(nameCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(revenueCol);
        table.getColumns().add(profitCol);
        return table;
    }

    private TableView<StockReportDTO.Line> buildStockTable() {
        TableView<StockReportDTO.Line> table = new TableView<>();
        table.setPlaceholder(new Label("Bu g\u00FCnde stok hareketi yok"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<StockReportDTO.Line, String> nameCol = new TableColumn<>("\u0130la\u00E7");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));

        TableColumn<StockReportDTO.Line, Integer> inCol = new TableColumn<>("Giren");
        inCol.setCellValueFactory(new PropertyValueFactory<>("inQuantity"));
        inCol.setMaxWidth(90);

        TableColumn<StockReportDTO.Line, Integer> outCol = new TableColumn<>("\u00C7\u0131kan");
        outCol.setCellValueFactory(new PropertyValueFactory<>("outQuantity"));
        outCol.setMaxWidth(90);

        TableColumn<StockReportDTO.Line, Integer> netCol = new TableColumn<>("Net");
        netCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(
                        c.getValue().getNetQuantity()).asObject());
        netCol.setMaxWidth(90);

        table.getColumns().add(nameCol);
        table.getColumns().add(inCol);
        table.getColumns().add(outCol);
        table.getColumns().add(netCol);
        return table;
    }
}
