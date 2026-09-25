package com.phermacyrepo.ui;

/**
 * Exe/jar dagitimi icin giris noktasi. Dogrudan Application
 * alt sinifini baslatmak classpath modunda "JavaFX runtime
 * components are missing" hatasi verir, o yuzden ayri sinif.
 */
public class Launcher {
    public static void main(String[] args) {
        PharmacyApp.main(args);
    }
}
