package com.agenda.controllers;

import com.agenda.models.Tarea;
import com.agenda.services.TareaService;
import com.agenda.utils.DateUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarioController {

    @FXML private Label lblMes;
    @FXML private GridPane gridCalendario;

    private YearMonth mesActual;
    private MainController mainController;

    private static final String[] DIAS_SEMANA = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    @FXML
    public void initialize() {
        mesActual = YearMonth.now();
        cargarMes();
    }

    public void setMainController(MainController ctrl) {
        this.mainController = ctrl;
    }

    public void navegarAMes(YearMonth mes) {
        mesActual = mes;
        cargarMes();
    }

    @FXML
    private void navegarMesAnterior() {
        mesActual = mesActual.minusMonths(1);
        cargarMes();
    }

    @FXML
    private void navegarMesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        cargarMes();
    }

    private void cargarMes() {
        lblMes.setText(DateUtils.formatearMesAnio(mesActual));
        gridCalendario.getChildren().clear();
        gridCalendario.getColumnConstraints().clear();

        // Columnas con peso igual
        for (int i = 0; i < 7; i++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            cc.setPercentWidth(100.0 / 7);
            gridCalendario.getColumnConstraints().add(cc);
        }

        // Encabezados de días
        for (int i = 0; i < 7; i++) {
            Label cab = new Label(DIAS_SEMANA[i]);
            cab.getStyleClass().add("cab-dia-semana");
            cab.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(cab, javafx.scene.layout.Priority.ALWAYS);
            gridCalendario.add(cab, i, 0);
        }

        // Obtener días con tareas en este mes
        Set<LocalDate> diasConTareas = obtenerDiasConTareas();

        // Primer día del mes y su posición en la semana (lunes=0)
        LocalDate primerDia = mesActual.atDay(1);
        int offsetInicio = primerDia.getDayOfWeek().getValue() - 1; // lunes=0

        int totalDias = mesActual.lengthOfMonth();
        int celda = offsetInicio;
        LocalDate hoy = LocalDate.now();

        for (int dia = 1; dia <= totalDias; dia++) {
            LocalDate fecha = mesActual.atDay(dia);
            int col = celda % 7;
            int fila = 1 + (celda / 7);

            VBox celdaDia = crearCeldaDia(fecha, hoy, diasConTareas);
            gridCalendario.add(celdaDia, col, fila);
            celda++;
        }
    }

    private VBox crearCeldaDia(LocalDate fecha, LocalDate hoy, Set<LocalDate> diasConTareas) {
        VBox box = new VBox(4);
        box.getStyleClass().add("dia-mes");

        if (fecha.equals(hoy)) {
            box.getStyleClass().add("dia-mes-hoy");
        }

        // Número del día
        StackPane numeroPane = new StackPane();
        Label numLabel = new Label(String.valueOf(fecha.getDayOfMonth()));
        numLabel.getStyleClass().add("dia-mes-numero");
        numeroPane.getChildren().add(numLabel);
        box.getChildren().add(numeroPane);

        // Punto indicador si hay tareas
        if (diasConTareas.contains(fecha)) {
            HBox puntos = new HBox(3);
            puntos.setAlignment(javafx.geometry.Pos.CENTER);
            Circle punto = new Circle(3);
            punto.getStyleClass().add("punto-tarea");
            puntos.getChildren().add(punto);
            box.getChildren().add(puntos);
        }

        // Click → navegar a esa semana en la vista principal
        box.setOnMouseClicked(e -> {
            if (mainController != null) {
                mainController.mostrarSemanaEnFecha(fecha);
            }
        });

        return box;
    }

    private Set<LocalDate> obtenerDiasConTareas() {
        Set<LocalDate> fechas = new HashSet<>();
        try {
            List<Tarea> tareas = TareaService.getInstance().getTareasPorMes(
                    mesActual.getYear(), mesActual.getMonthValue());
            for (Tarea t : tareas) {
                if (t.getFecha() != null) fechas.add(t.getFecha());
            }
        } catch (SQLException e) {
            // Sin tareas disponibles — grid sigue mostrándose sin dots
        }
        return fechas;
    }
}
