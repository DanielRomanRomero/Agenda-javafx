package com.agenda.controllers;

import com.agenda.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;

public class MainController {

    @FXML private HBox toolbar;
    @FXML private StackPane contentArea;
    @FXML private Button btnToggleTema;
    @FXML private Button btnVistaSemana;
    @FXML private Button btnVistaCalendario;

    private SemanaController semanaController;
    private CalendarioController calendarioController;
    private Parent semanaView;
    private Parent calendarioView;

    @FXML
    public void initialize() {
        cargarVistas();
        mostrarSemana();
        actualizarIconoTema();
    }

    private void cargarVistas() {
        try {
            FXMLLoader loaderSemana = new FXMLLoader(
                    getClass().getResource("/com/agenda/views/semana.fxml"));
            semanaView = loaderSemana.load();
            semanaController = loaderSemana.getController();
            semanaController.setMainController(this);

            FXMLLoader loaderCal = new FXMLLoader(
                    getClass().getResource("/com/agenda/views/calendario.fxml"));
            calendarioView = loaderCal.load();
            calendarioController = loaderCal.getController();
            calendarioController.setMainController(this);

        } catch (IOException e) {
            System.err.println("Error al cargar vistas: " + e.getMessage());
        }
    }

    @FXML
    public void mostrarSemana() {
        contentArea.getChildren().setAll(semanaView);
        StackPane.setAlignment(semanaView, javafx.geometry.Pos.TOP_LEFT);
        marcarBotonActivo(btnVistaSemana);
    }

    @FXML
    public void mostrarCalendario() {
        contentArea.getChildren().setAll(calendarioView);
        marcarBotonActivo(btnVistaCalendario);
    }

    public void mostrarSemanaEnFecha(LocalDate fecha) {
        mostrarSemana();
        if (semanaController != null) {
            semanaController.navegarAFecha(fecha);
        }
    }

    @FXML
    public void nuevaTarea() {
        // Abre formulario de nueva tarea sin fecha/hora predefinida via SemanaController
        if (semanaController != null) {
            mostrarSemana();
            // El semanaController expone el método vía el rootPane; usamos la fecha actual
            LocalDate hoy = LocalDate.now();
            LocalDate lunes = com.agenda.utils.DateUtils.obtenerLunesDe(hoy);
            // Navegar a la semana actual si no estamos en ella, luego abrir formulario
            semanaController.navegarAFecha(hoy);
            // Pequeño delay para que el grid cargue antes de abrir el overlay
            javafx.application.Platform.runLater(() -> abrirFormularioNuevaTarea(hoy));
        }
    }

    private void abrirFormularioNuevaTarea(LocalDate fecha) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agenda/views/tarea-form.fxml"));
            Parent formRoot = loader.load();
            TareaFormController ctrl = loader.getController();
            ctrl.setFechaHoraInicial(fecha, LocalDate.now().atStartOfDay().toLocalTime().withHour(9));
            ctrl.setOnGuardado(() -> {
                contentArea.getChildren().remove(formRoot);
                if (semanaController != null) semanaController.navegarAFecha(fecha);
            });
            ctrl.setOnCancelado(() -> contentArea.getChildren().remove(formRoot));
            contentArea.getChildren().add(formRoot);
        } catch (IOException e) {
            System.err.println("Error al abrir formulario de nueva tarea: " + e.getMessage());
        }
    }

    @FXML
    public void toggleTema() {
        ThemeManager.toggleTheme();
        actualizarIconoTema();
    }

    private void actualizarIconoTema() {
        String tema = ThemeManager.getTemaActual();
        btnToggleTema.setText("oscuro".equals(tema) ? "☀" : "🌙");
    }

    private void marcarBotonActivo(Button activo) {
        btnVistaSemana.getStyleClass().remove("btn-nav-activo");
        btnVistaCalendario.getStyleClass().remove("btn-nav-activo");
        activo.getStyleClass().add("btn-nav-activo");
    }

    /** Expone el StackPane raíz para que los controladores hijos puedan añadir overlays */
    public StackPane getRootPane() {
        return contentArea;
    }
}
