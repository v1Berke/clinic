-- Pharmacy Management System Database Schema
-- SQLite Database

-- Enable foreign key support (also done in DatabaseManager)
PRAGMA foreign_keys = ON;

-- Medicine table
-- Stores medicine information with pricing and inventory
CREATE TABLE IF NOT EXISTS medicine (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    barcode TEXT NOT NULL UNIQUE,
    type TEXT NOT NULL,
    purchase_price REAL NOT NULL,
    sale_price REAL NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (purchase_price > 0),
    CHECK (sale_price > 0),
    CHECK (stock >= 0)
);

-- Stock Movement table
-- Tracks all inventory movements (purchases, sales, adjustments, losses)
CREATE TABLE IF NOT EXISTS stock_movement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    date TIMESTAMP NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE,
    CHECK (quantity > 0)
);

-- Sale table
-- Stores individual sales transactions
CREATE TABLE IF NOT EXISTS sale (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_date TIMESTAMP NOT NULL,
    total_price REAL NOT NULL,
    total_quantity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (total_price >= 0),
    CHECK (total_quantity > 0)
);

-- Sale Item table
-- Individual items within a sale
CREATE TABLE IF NOT EXISTS sale_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER NOT NULL,
    medicine_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price REAL NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE RESTRICT,
    CHECK (quantity > 0),
    CHECK (unit_price > 0)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_medicine_barcode ON medicine(barcode);
CREATE INDEX IF NOT EXISTS idx_stock_movement_medicine ON stock_movement(medicine_id);
CREATE INDEX IF NOT EXISTS idx_stock_movement_date ON stock_movement(date);
CREATE INDEX IF NOT EXISTS idx_sale_date ON sale(sale_date);
CREATE INDEX IF NOT EXISTS idx_sale_item_sale ON sale_item(sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_item_medicine ON sale_item(medicine_id);
