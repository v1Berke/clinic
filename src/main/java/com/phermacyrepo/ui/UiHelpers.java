package com.phermacyrepo.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Ortak format ve pencere yardimcilari. */
public final class UiHelpers {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private UiHelpers() {}

    public static String money(double value) {
        return String.format("\u20BA%.2f", value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME);
    }

    public static String date(LocalDate value) {
        return value == null ? "-" : value.format(DATE);
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Evet/Hayir sorar, true = onaylandi. */
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType keep = new ButtonType("Vazge\u00E7");
        ButtonType ok = new ButtonType("Onayla");
        alert.getButtonTypes().setAll(keep, ok);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ok;
    }

    /**
     * Enum degeri domain'de Ingilizce kalir; kullaniciya gosterim
     * sadece UI katmaninda Turkcelestirilir.
     */
    public static String medicineTypeText(
            com.phermacyrepo.domain.enum_.MedicineType type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case TABLET -> "Tablet";
            case SYRUP -> "\u015Eurup";
            case INJECTION -> "Enjeksiyon";
            case OINTMENT -> "Merhem";
        };
    }

    /**
     * Servis katmanindan gelen teknik mesaji kullanicinin
     * anlayacagi dile cevirir.
     */
    public static String friendlyError(RuntimeException e) {
        String raw = e.getMessage() == null ? "" : e.getMessage();
        String lower = raw.toLowerCase();
        if (lower.contains("not found")) {
            return "Kay\u0131t bulunamad\u0131.\n" + raw;
        }
        if (lower.contains("insufficient stock")) {
            return "Yetersiz stok.\n" + raw;
        }
        if (lower.contains("already exists")) {
            return "Bu barkod zaten kay\u0131tl\u0131.\n" + raw;
        }
        return raw.isEmpty() ? "Beklenmeyen bir hata olu\u015Ftu." : raw;
    }
}
