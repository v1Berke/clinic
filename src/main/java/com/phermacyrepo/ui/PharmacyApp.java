package com.phermacyrepo.ui;

import com.phermacyrepo.db.DatabaseManager;
import com.phermacyrepo.db.dao.MedicineDAO;
import com.phermacyrepo.db.dao.SaleDAO;
import com.phermacyrepo.db.dao.SaleItemDAO;
import com.phermacyrepo.db.dao.StockMovementDAO;
import com.phermacyrepo.service.MedicineService;
import com.phermacyrepo.service.ReportService;
import com.phermacyrepo.service.SaleService;
import com.phermacyrepo.service.StockMovementService;
import com.phermacyrepo.service.impl.MedicineServiceImpl;
import com.phermacyrepo.service.impl.ReportServiceImpl;
import com.phermacyrepo.service.impl.SaleServiceImpl;
import com.phermacyrepo.service.impl.StockMovementServiceImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Uygulama girisi. Katman akisi:
 * JavaFX -> controller (View siniflari) -> Service -> DAO -> Database.
 * Controller'lar veritabanina dogrudan erismez.
 *
 * Calistirma: mvn javafx:run
 */
public class PharmacyApp extends Application {

    private DatabaseManager dbManager;
    private SaleView saleView;
    private ManagementView managementView;
    private ReportView reportView;
    private StackPane content;

    @Override
    public void start(Stage stage) {
        if (!initBackend()) {
            Platform.exit();
            return;
        }

        saleView = new SaleView(medicineService(), saleService());
        managementView = new ManagementView(medicineService(), saleService(), stockMovementService());
        reportView = new ReportView(reportService());

        Node saleNode = saleView.view();
        Node managementNode = managementView.view();
        Node reportNode = reportView.view();

        content = new StackPane(saleNode, managementNode, reportNode);
        managementNode.setVisible(false);
        reportNode.setVisible(false);

        ToggleGroup modes = new ToggleGroup();
        ToggleButton salesButton = new ToggleButton("SATI\u015E");
        ToggleButton managementButton = new ToggleButton("Y\u00D6NET\u0130M");
        ToggleButton reportButton = new ToggleButton("RAPOR");
        salesButton.getStyleClass().add("nav-button");
        managementButton.getStyleClass().add("nav-button");
        reportButton.getStyleClass().add("nav-button");
        salesButton.setToggleGroup(modes);
        managementButton.setToggleGroup(modes);
        reportButton.setToggleGroup(modes);
        salesButton.setSelected(true);

        modes.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                // Klavye ile secili mod duserse her zaman SATIS'a don.
                modes.selectToggle(salesButton);
                return;
            }
            saleNode.setVisible(selected == salesButton);
            managementNode.setVisible(selected == managementButton);
            reportNode.setVisible(selected == reportButton);
            if (selected == salesButton) {
                saleView.onShown();
            } else if (selected == managementButton) {
                managementView.onShown();
            } else {
                reportView.onShown();
            }
        });

        Label title = new Label("MedVault");
        title.getStyleClass().add("nav-title");

        javafx.scene.image.ImageView logoView =
                new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(
                                getClass().getResourceAsStream("/ui/logo.png")));
        logoView.setFitWidth(44);
        logoView.setFitHeight(44);
        logoView.setPreserveRatio(true);

        HBox nav = new HBox(12, logoView, title, salesButton, managementButton, reportButton);
        nav.getStyleClass().add("nav-bar");
        nav.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();
        root.setTop(nav);
        root.setCenter(content);

        Scene scene = new Scene(root, 1100, 760);
        scene.getStylesheets().add(
                getClass().getResource("/ui/styles.css").toExternalForm());

        // Klavye kisa yollari: Alt+1 satis, Alt+2 yonetim, Alt+3 rapor.
        scene.getAccelerators().put(
                javafx.scene.input.KeyCombination.valueOf("Alt+1"),
                () -> modes.selectToggle(salesButton));
        scene.getAccelerators().put(
                javafx.scene.input.KeyCombination.valueOf("Alt+2"),
                () -> modes.selectToggle(managementButton));
        scene.getAccelerators().put(
                javafx.scene.input.KeyCombination.valueOf("Alt+3"),
                () -> modes.selectToggle(reportButton));

        stage.setTitle("MedVault Sat\u0131\u015F");
        stage.getIcons().add(new javafx.scene.image.Image(
                getClass().getResourceAsStream("/ui/logo.png")));
        stage.setScene(scene);
        stage.show();

        saleView.onShown();
    }

    @Override
    public void stop() {
        if (dbManager != null) {
            dbManager.shutdown();
        }
    }

    private boolean initBackend() {
        try {
            dbManager = DatabaseManager.getInstance();
            dbManager.initialize();
            return true;
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Veritaban\u0131 hatas\u0131");
            alert.setHeaderText(null);
            alert.setContentText("Veritaban\u0131 a\u00E7\u0131lamad\u0131:\n" + e.getMessage());
            alert.showAndWait();
            return false;
        }
    }

    // Servisler tek noktada kurulur, View'lara hazir verilir.
    private MedicineService medicineService;
    private SaleService saleService;
    private StockMovementService stockMovementService;
    private ReportService reportService;
    private boolean wired;

    private void wire() {
        if (wired) {
            return;
        }
        MedicineDAO medicineDAO = new MedicineDAO(dbManager);
        SaleDAO saleDAO = new SaleDAO(dbManager);
        SaleItemDAO saleItemDAO = new SaleItemDAO(dbManager);
        StockMovementDAO stockMovementDAO = new StockMovementDAO(dbManager);
        medicineService = new MedicineServiceImpl(dbManager, medicineDAO, saleDAO, saleItemDAO);
        saleService = new SaleServiceImpl(dbManager, saleDAO, saleItemDAO,
                medicineDAO, stockMovementDAO);
        stockMovementService = new StockMovementServiceImpl(dbManager,
                medicineDAO, stockMovementDAO);
        reportService = new ReportServiceImpl(saleDAO, saleItemDAO,
                medicineDAO, stockMovementDAO);
        wired = true;
    }

    private MedicineService medicineService() { wire(); return medicineService; }
    private SaleService saleService() { wire(); return saleService; }
    private StockMovementService stockMovementService() { wire(); return stockMovementService; }
    private ReportService reportService() { wire(); return reportService; }

    public static void main(String[] args) {
        launch(args);
    }
}
