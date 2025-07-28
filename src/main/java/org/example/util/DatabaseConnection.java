package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static volatile HikariDataSource dataSource;

    static {
        initializeDataSource();
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeDataSource));
    }

    private static void initializeDataSource() {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new IllegalStateException("database.properties file not found in classpath");
            }

            Properties prop = new Properties();
            prop.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.user"));
            config.setPassword(prop.getProperty("db.password"));

            // Оптимальные настройки пула соединений
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setAutoCommit(true);
            config.setPoolName("FamilyFinancePool");

            // Настройки для лучшей диагностики
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");

        } catch (IOException e) {
            logger.error("Failed to load database configuration", e);
            throw new RuntimeException("Database configuration error", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            logger.debug("Obtained database connection from pool");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw e;
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.debug("Database connection returned to pool");
                }
            } catch (SQLException e) {
                logger.warn("Failed to close database connection", e);
            }
        }
    }

    private static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shut down successfully");
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}