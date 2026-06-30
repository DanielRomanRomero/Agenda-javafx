package com.agenda.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracionService {

    private static ConfiguracionService instance;

    private ConfiguracionService() {}

    public static ConfiguracionService getInstance() {
        if (instance == null) {
            instance = new ConfiguracionService();
        }
        return instance;
    }

    private Connection getConn() throws SQLException {
        return DatabaseService.getInstance().getConnection();
    }

    public String getValor(String clave, String defaultValor) {
        try (PreparedStatement ps = getConn().prepareStatement("SELECT valor FROM configuracion WHERE clave=?")) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valor");
                }
            }
        } catch (SQLException e) {
            // Retornar default si falla
        }
        return defaultValor;
    }

    public void setValor(String clave, String valor) {
        try (PreparedStatement ps = getConn().prepareStatement(
                "INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)")) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Silencioso — la configuración no es crítica
        }
    }
}
