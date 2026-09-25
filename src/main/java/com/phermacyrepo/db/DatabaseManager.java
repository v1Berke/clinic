package com.phermacyrepo.db;

import com.phermacyrepo.domain.exceptions.DatabaseException;
import java.io.*;
import java.sql.*;

/**
 * DatabaseManager handles all SQLite database operations including connection management,
 * transaction handling, and schema initialization.
 */
public class DatabaseManager {
    
    private static final String DATABASE_NAME = "pharmacy.db";
    private static final String DATABASE_PATH = DATABASE_NAME;
    private static volatile DatabaseManager instance;
    private Connection connection;
    private boolean isTransactionActive = false;

    /**
     * Private constructor for singleton pattern
     */
    private DatabaseManager() {
    }

    /**
     * Get singleton instance of DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize database connection and schema
     * Should be called once at application startup
     */
    public void initialize() {
        try {
            openConnection();
            enableForeignKeys();
            initializeSchema();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            closeConnection();
            throw new DatabaseException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /**
     * Open connection to SQLite database
     */
    private void openConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            connection = DriverManager.getConnection(url);
            
            // Set basic configurations
            connection.setAutoCommit(true);
            
        } catch (SQLException e) {
            throw new DatabaseException("Failed to open database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (isTransactionActive) {
                    connection.rollback();
                }
                connection.close();
            } catch (SQLException e) {
                throw new DatabaseException("Failed to close database connection: " + e.getMessage(), e);
            } finally {
                connection = null;
                isTransactionActive = false;
            }
        }
    }

    /**
     * Enable foreign key constraints (required in SQLite)
     */
    private void enableForeignKeys() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new DatabaseException("Connection is not open");
            }
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to enable foreign keys: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize database schema by executing schema.sql
     */
    private void initializeSchema() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new DatabaseException("Connection is not open");
            }
            
            String schema = loadSchemaFromResource();
            executeSqlScript(schema);
            dropRemovedColumns();
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Failed to initialize schema: " + e.getMessage(), e);
        }
    }

    /**
     * Removes retired columns from older databases:
     * medicine.expiration_date, medicine.active and the medicine_batch table.
     * Safe to run on every start.
     */
    private void dropRemovedColumns() {
        try {
            ResultSet columns = executeQuery("PRAGMA table_info(medicine)");
            boolean hasExpiration = false;
            boolean hasActive = false;
            while (columns.next()) {
                String name = columns.getString("name");
                if ("expiration_date".equalsIgnoreCase(name)) {
                    hasExpiration = true;
                }
                if ("active".equalsIgnoreCase(name)) {
                    hasActive = true;
                }
            }
            columns.close();

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP INDEX IF EXISTS idx_medicine_active");
                stmt.execute("DROP INDEX IF EXISTS idx_medicine_batch_medicine");
                stmt.execute("DROP INDEX IF EXISTS idx_medicine_batch_expiration");
                if (hasExpiration) {
                    stmt.execute("ALTER TABLE medicine DROP COLUMN expiration_date");
                }
                if (hasActive) {
                    stmt.execute("ALTER TABLE medicine DROP COLUMN active");
                }
                stmt.execute("DROP TABLE IF EXISTS medicine_batch");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to migrate database: " + e.getMessage(), e);
        }
    }

    /**
     * Load schema.sql from resources
     */
    private String loadSchemaFromResource() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("schema.sql");
            
            if (inputStream == null) {
                throw new DatabaseException("schema.sql not found in resources");
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (IOException e) {
            throw new DatabaseException("Failed to load schema.sql: " + e.getMessage(), e);
        }
    }

    /**
     * Execute SQL script with multiple statements separated by semicolons
     */
    private void executeSqlScript(String script) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new DatabaseException("Connection is not open");
            }
            
            String[] statements = script.split(";");
            
            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute SQL script: " + e.getMessage(), e);
        }
    }

    /**
     * Begin a transaction
     */
    public void beginTransaction() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new DatabaseException("Connection is not open");
            }
            
            if (isTransactionActive) {
                throw new DatabaseException("Transaction already active");
            }
            
            connection.setAutoCommit(false);
            isTransactionActive = true;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to begin transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Commit current transaction
     */
    public void commit() {
        try {
            if (!isTransactionActive) {
                throw new DatabaseException("No active transaction to commit");
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            isTransactionActive = false;
        } catch (SQLException e) {
            try {
                rollback();
            } catch (Exception rollbackError) {
                // Ignore rollback error during commit failure
            }
            throw new DatabaseException("Failed to commit transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Rollback current transaction
     */
    public void rollback() {
        try {
            if (!isTransactionActive) {
                throw new DatabaseException("No active transaction to rollback");
            }
            
            connection.rollback();
            connection.setAutoCommit(true);
            isTransactionActive = false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to rollback transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Check if transaction is active
     */
    public boolean isTransactionActive() {
        return isTransactionActive;
    }

    /**
     * Get current database connection
     * Useful for executing queries directly if needed
     */
    public Connection getConnection() {
        if (connection == null || (isTransactionActive)) {
            try {
                if (connection != null && connection.isClosed()) {
                    openConnection();
                    enableForeignKeys();
                }
            } catch (SQLException e) {
                throw new DatabaseException("Connection is closed and cannot be reopened: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    /**
     * Execute a SELECT query and return ResultSet
     * NOTE: Caller is responsible for closing the ResultSet and Statement
     */
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement stmt = prepareStatement(sql, params);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    /**
     * Execute an INSERT/UPDATE/DELETE query and return number of affected rows
     */
    public int executeUpdate(String sql, Object... params) {
        try {
            PreparedStatement stmt = prepareStatement(sql, params);
            try {
                return stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute update: " + e.getMessage(), e);
        }
    }

    /**
     * Execute an INSERT query and return generated keys
     */
    public ResultSet executeInsertAndGetKeys(String sql, Object... params) {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql, 
                    Statement.RETURN_GENERATED_KEYS);
            setParameters(stmt, params);
            stmt.executeUpdate();
            return stmt.getGeneratedKeys();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute insert: " + e.getMessage(), e);
        }
    }

    /**
     * Prepare a statement with parameters
     */
    private PreparedStatement prepareStatement(String sql, Object... params) 
            throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt;
    }

    /**
     * Set parameters in a prepared statement
     */
    private void setParameters(PreparedStatement stmt, Object... params) 
            throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;
            
            if (param == null) {
                stmt.setNull(index, Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(index, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(index, (Long) param);
            } else if (param instanceof Double) {
                stmt.setDouble(index, (Double) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(index, (Boolean) param);
            } else if (param instanceof java.time.LocalDate) {
                stmt.setDate(index, Date.valueOf((java.time.LocalDate) param));
            } else if (param instanceof java.time.LocalDateTime) {
                stmt.setTimestamp(index, Timestamp.valueOf((java.time.LocalDateTime) param));
            } else {
                stmt.setObject(index, param);
            }
        }
    }

    /**
     * Check if database tables exist
     */
    public boolean tablesExist() {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet tables = metadata.getTables(null, null, "%", 
                    new String[]{"TABLE"});
            return tables.next();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check if tables exist: " + e.getMessage(), e);
        }
    }

    /**
     * Clear all data from database (useful for testing)
     */
    public void clearAllData() {
        try {
            beginTransaction();
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM sale_item");
                stmt.execute("DELETE FROM sale");
                stmt.execute("DELETE FROM stock_movement");
                stmt.execute("DELETE FROM medicine");
            }
            commit();
        } catch (SQLException e) {
            rollback();
            throw new DatabaseException("Failed to clear database: " + e.getMessage(), e);
        }
    }

    /**
     * Shutdown database gracefully
     */
    public void shutdown() {
        closeConnection();
    }
}
