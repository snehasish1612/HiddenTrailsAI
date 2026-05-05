package com.hiddentrails.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBConnection — Singleton connection pool using Apache DBCP2.
 *
 * Reads credentials from src/main/resources/db.properties.
 * All DAOs call DBConnection.getConnection() and close() in a
 * try-with-resources block — connections return to the pool automatically.
 *
 * Usage:
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // use conn
 *   }
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final BasicDataSource DATA_SOURCE = new BasicDataSource();

    // Static initialiser — runs once when the class is loaded
    static {
        // Try multiple loading strategies for Eclipse WTP + Maven compatibility
        InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties");

        // Fallback 1: try with leading slash
        if (in == null) {
            in = DBConnection.class
                    .getResourceAsStream("/db.properties");
        }

        // Fallback 2: try Thread context classloader
        if (in == null) {
            in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("db.properties");
        }

        if (in == null) {
            throw new RuntimeException(
                "db.properties not found in classpath. " +
                "Place it in WEB-INF/classes/ or src/main/resources/");
        }

        try (InputStream stream = in) {
            Properties props = new Properties();
            props.load(stream);

            DATA_SOURCE.setDriverClassName("com.mysql.cj.jdbc.Driver");
            DATA_SOURCE.setUrl(props.getProperty("db.url"));
            DATA_SOURCE.setUsername(props.getProperty("db.username"));
            DATA_SOURCE.setPassword(props.getProperty("db.password"));

            // Pool sizing
            DATA_SOURCE.setInitialSize(
                Integer.parseInt(props.getProperty("db.pool.initial", "3")));
            DATA_SOURCE.setMaxTotal(
                Integer.parseInt(props.getProperty("db.pool.max", "10")));
            DATA_SOURCE.setMinIdle(
                Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));
            DATA_SOURCE.setMaxIdle(
                Integer.parseInt(props.getProperty("db.pool.maxIdle", "5")));

            // Connection validation
            DATA_SOURCE.setValidationQuery("SELECT 1");
            DATA_SOURCE.setTestOnBorrow(true);
            DATA_SOURCE.setTestWhileIdle(true);

            // Wait up to 10 s for a connection before throwing
            DATA_SOURCE.setMaxWaitMillis(10_000);

            LOGGER.info("DBConnection pool initialised: " + props.getProperty("db.url"));

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load db.properties", e);
            throw new RuntimeException("Cannot initialise DB pool", e);
        }
    }

    // Private constructor — no instances allowed
    private DBConnection() {}

    /**
     * Returns a connection from the pool.
     * Caller MUST close it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    /**
     * Call on application shutdown (e.g. in a ServletContextListener)
     * to gracefully drain the pool.
     */
    public static void shutdown() {
        try {
            DATA_SOURCE.close();
            LOGGER.info("DBConnection pool closed.");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing DB pool", e);
        }
    }
}