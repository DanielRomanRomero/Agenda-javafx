package com.agenda.services;

import com.agenda.models.Tarea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TareaService {

    private static TareaService instance;

    private TareaService() {}

    public static TareaService getInstance() {
        if (instance == null) {
            instance = new TareaService();
        }
        return instance;
    }

    private Connection getConn() throws SQLException {
        return DatabaseService.getInstance().getConnection();
    }

    public List<Tarea> getTareasPorSemana(LocalDate inicio, LocalDate fin) throws SQLException {
        String sql = "SELECT * FROM tareas WHERE fecha BETWEEN ? AND ? ORDER BY fecha, hora_inicio";
        List<Tarea> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fin.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Tarea> getTareasPorMes(int anio, int mes) throws SQLException {
        String sql = "SELECT * FROM tareas WHERE strftime('%Y', fecha) = ? AND strftime('%m', fecha) = ? ORDER BY fecha, hora_inicio";
        List<Tarea> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, String.valueOf(anio));
            ps.setString(2, String.format("%02d", mes));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Tarea> getRecordatoriosPendientes() throws SQLException {
        String sql = "SELECT * FROM tareas WHERE recordatorio = 1 AND completada = 0 AND fecha IS NOT NULL AND hora_inicio IS NOT NULL";
        List<Tarea> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            LocalDateTime ahora = LocalDateTime.now();
            while (rs.next()) {
                Tarea t = mapear(rs);
                if (t.getFecha() != null && t.getHoraInicio() != null) {
                    LocalDateTime momentoTarea = LocalDateTime.of(t.getFecha(), t.getHoraInicio());
                    LocalDateTime momentoAviso = momentoTarea.minusMinutes(t.getMinutosAntes());
                    // Dentro de la ventana de 60 segundos siguiente al momento de aviso
                    if (!momentoAviso.isBefore(ahora) && momentoAviso.isBefore(ahora.plusSeconds(60))) {
                        lista.add(t);
                    }
                }
            }
        }
        return lista;
    }

    public Tarea crear(Tarea tarea) throws SQLException {
        String sql = "INSERT INTO tareas (titulo, descripcion, fecha, hora_inicio, hora_fin, tipo, completada, recordatorio, minutos_antes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tarea.getTitulo());
            ps.setString(2, tarea.getDescripcion());
            ps.setString(3, tarea.getFecha() != null ? tarea.getFecha().toString() : null);
            ps.setString(4, tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : null);
            ps.setString(5, tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : null);
            ps.setString(6, tarea.getTipo());
            ps.setInt(7, tarea.isCompletada() ? 1 : 0);
            ps.setInt(8, tarea.isRecordatorio() ? 1 : 0);
            ps.setInt(9, tarea.getMinutosAntes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    tarea.setId(keys.getInt(1));
                }
            }
        }
        return tarea;
    }

    public void actualizar(Tarea tarea) throws SQLException {
        String sql = "UPDATE tareas SET titulo=?, descripcion=?, fecha=?, hora_inicio=?, hora_fin=?, tipo=?, completada=?, recordatorio=?, minutos_antes=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, tarea.getTitulo());
            ps.setString(2, tarea.getDescripcion());
            ps.setString(3, tarea.getFecha() != null ? tarea.getFecha().toString() : null);
            ps.setString(4, tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : null);
            ps.setString(5, tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : null);
            ps.setString(6, tarea.getTipo());
            ps.setInt(7, tarea.isCompletada() ? 1 : 0);
            ps.setInt(8, tarea.isRecordatorio() ? 1 : 0);
            ps.setInt(9, tarea.getMinutosAntes());
            ps.setInt(10, tarea.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM tareas WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void marcarCompletada(int id, boolean completada) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("UPDATE tareas SET completada=? WHERE id=?")) {
            ps.setInt(1, completada ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Tarea mapear(ResultSet rs) throws SQLException {
        Tarea t = new Tarea();
        t.setId(rs.getInt("id"));
        t.setTitulo(rs.getString("titulo"));
        t.setDescripcion(rs.getString("descripcion"));
        String fecha = rs.getString("fecha");
        if (fecha != null && !fecha.isEmpty()) t.setFecha(LocalDate.parse(fecha));
        String horaInicio = rs.getString("hora_inicio");
        if (horaInicio != null && !horaInicio.isEmpty()) t.setHoraInicio(LocalTime.parse(horaInicio));
        String horaFin = rs.getString("hora_fin");
        if (horaFin != null && !horaFin.isEmpty()) t.setHoraFin(LocalTime.parse(horaFin));
        t.setTipo(rs.getString("tipo"));
        t.setCompletada(rs.getInt("completada") == 1);
        t.setRecordatorio(rs.getInt("recordatorio") == 1);
        t.setMinutosAntes(rs.getInt("minutos_antes"));
        return t;
    }
}
