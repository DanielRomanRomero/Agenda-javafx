package com.agenda.services;

import com.agenda.models.Tarea;
import com.agenda.utils.NotificacionUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecordatorioService {

    private static RecordatorioService instance;
    private ScheduledExecutorService executor;
    private final Set<String> notificacionesEnviadas = new HashSet<>();

    private RecordatorioService() {}

    public static RecordatorioService getInstance() {
        if (instance == null) {
            instance = new RecordatorioService();
        }
        return instance;
    }

    public void start() {
        if (executor != null && !executor.isShutdown()) return;

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("agenda-recordatorios");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(this::checkRecordatorios, 5, 60, TimeUnit.SECONDS);
    }

    private void checkRecordatorios() {
        try {
            List<Tarea> pendientes = TareaService.getInstance().getRecordatoriosPendientes();
            for (Tarea t : pendientes) {
                String clave = t.getId() + "_" + t.getFecha() + "_" + t.getHoraInicio();
                if (!notificacionesEnviadas.contains(clave)) {
                    notificacionesEnviadas.add(clave);
                    enviarNotificacion(t);
                }
            }
        } catch (SQLException e) {
            // Error silencioso — la app sigue funcionando sin recordatorios
        }
    }

    private void enviarNotificacion(Tarea tarea) {
        String hora = tarea.getHoraInicio() != null
                ? tarea.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "";
        String mensaje = "A las " + hora + ": " + tarea.getTitulo();
        NotificacionUtils.mostrarNotificacion("Recordatorio — Agenda", mensaje);
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
