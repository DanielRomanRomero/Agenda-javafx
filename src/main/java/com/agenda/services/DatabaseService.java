package com.agenda.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseService {

    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {}

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Path dbDir = Paths.get(System.getProperty("user.home"), ".agenda");
            try {
                Files.createDirectories(dbDir);
            } catch (Exception e) {
                throw new SQLException("No se pudo crear el directorio de base de datos: " + dbDir, e);
            }
            String url = "jdbc:sqlite:" + dbDir.resolve("agenda.db").toString().replace("\\", "/");
            connection = DriverManager.getConnection(url);
            configurarConexion(connection);
            initialize(connection);
        }
        return connection;
    }

    private void configurarConexion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
        }
    }

    private void initialize(Connection conn) throws SQLException {
        try (InputStream is = getClass().getResourceAsStream("/com/agenda/database/schema.sql")) {
            if (is == null) {
                throw new SQLException("No se encontró schema.sql en los recursos");
            }
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            try (Statement stmt = conn.createStatement()) {
                // Ejecutar cada sentencia separada por punto y coma
                for (String sentencia : sql.split(";")) {
                    String s = sentencia.trim();
                    if (!s.isEmpty()) {
                        stmt.execute(s);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("Error al inicializar la base de datos", e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignorar error al cerrar
            }
            connection = null;
        }
    }
}
