package com.agenda.controllers;

import com.agenda.models.Tarea;
import com.agenda.services.TareaService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;

public class TareaFormController {

    @FXML private StackPane overlayPane;
    @FXML private Label lblTituloForm;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<String> cmbHoraInicio;
    @FXML private ComboBox<String> cmbHoraFin;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private CheckBox chkRecordatorio;
    @FXML private ComboBox<String> cmbMinutosAntes;
    @FXML private Label lblError;
    @FXML private VBox confirmEliminarPane;
    @FXML private javafx.scene.control.Button btnEliminar;

    private Tarea tareaExistente;
    private Runnable onGuardado;
    private Runnable onCancelado;

    @FXML
    public void initialize() {
        // Horas en incrementos de 30 minutos, de 07:00 a 23:00
        for (int h = 7; h <= 23; h++) {
            cmbHoraInicio.getItems().add(String.format("%02d:00", h));
            if (h < 23) cmbHoraInicio.getItems().add(String.format("%02d:30", h));
        }
        for (int h = 7; h <= 23; h++) {
            cmbHoraFin.getItems().add(String.format("%02d:00", h));
            if (h < 23) cmbHoraFin.getItems().add(String.format("%02d:30", h));
        }

        cmbTipo.getItems().addAll("tarea", "nota");
        cmbTipo.setValue("tarea");

        cmbMinutosAntes.getItems().addAll("5 min antes", "10 min antes", "15 min antes", "30 min antes", "1 hora antes");
        cmbMinutosAntes.setValue("15 min antes");

        dateFecha.setValue(LocalDate.now());
        cmbHoraInicio.setValue("09:00");
        cmbHoraFin.setValue("10:00");
    }

    public void setTarea(Tarea tarea) {
        this.tareaExistente = tarea;
        lblTituloForm.setText("Editar tarea");
        txtTitulo.setText(tarea.getTitulo());
        txtDescripcion.setText(tarea.getDescripcion() != null ? tarea.getDescripcion() : "");
        dateFecha.setValue(tarea.getFecha());
        if (tarea.getHoraInicio() != null) {
            cmbHoraInicio.setValue(tarea.getHoraInicio().toString());
        }
        if (tarea.getHoraFin() != null) {
            cmbHoraFin.setValue(tarea.getHoraFin().toString());
        }
        cmbTipo.setValue(tarea.getTipo());
        chkRecordatorio.setSelected(tarea.isRecordatorio());
        cmbMinutosAntes.setDisable(!tarea.isRecordatorio());
        cmbMinutosAntes.setValue(minutosATexto(tarea.getMinutosAntes()));

        btnEliminar.setVisible(true);
        btnEliminar.setManaged(true);
    }

    public void setFechaHoraInicial(LocalDate fecha, LocalTime hora) {
        dateFecha.setValue(fecha);
        if (hora != null) {
            cmbHoraInicio.setValue(String.format("%02d:00", hora.getHour()));
            int horaFin = Math.min(hora.getHour() + 1, 23);
            cmbHoraFin.setValue(String.format("%02d:00", horaFin));
        }
    }

    public void setOnGuardado(Runnable callback) { this.onGuardado = callback; }
    public void setOnCancelado(Runnable callback) { this.onCancelado = callback; }

    @FXML
    private void onRecordatorioToggle() {
        cmbMinutosAntes.setDisable(!chkRecordatorio.isSelected());
    }

    @FXML
    private void guardar() {
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            mostrarError("El título es obligatorio.");
            return;
        }
        ocultarError();

        Tarea t = tareaExistente != null ? tareaExistente : new Tarea();
        t.setTitulo(titulo);
        t.setDescripcion(txtDescripcion.getText().trim());
        t.setFecha(dateFecha.getValue());
        t.setHoraInicio(parsarHora(cmbHoraInicio.getValue()));
        t.setHoraFin(parsarHora(cmbHoraFin.getValue()));
        t.setTipo(cmbTipo.getValue() != null ? cmbTipo.getValue() : "tarea");
        t.setRecordatorio(chkRecordatorio.isSelected());
        t.setMinutosAntes(textoAMinutos(cmbMinutosAntes.getValue()));

        try {
            if (tareaExistente != null) {
                TareaService.getInstance().actualizar(t);
            } else {
                TareaService.getInstance().crear(t);
            }
            if (onGuardado != null) onGuardado.run();
        } catch (Exception e) {
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        if (onCancelado != null) onCancelado.run();
    }

    @FXML
    private void solicitarEliminar() {
        confirmEliminarPane.setVisible(true);
        confirmEliminarPane.setManaged(true);
    }

    @FXML
    private void confirmarEliminar() {
        if (tareaExistente == null) return;
        try {
            TareaService.getInstance().eliminar(tareaExistente.getId());
            if (onGuardado != null) onGuardado.run();
        } catch (Exception e) {
            mostrarError("Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void cancelarEliminar() {
        confirmEliminarPane.setVisible(false);
        confirmEliminarPane.setManaged(false);
    }

    @FXML
    private void clickOverlay(MouseEvent event) {
        // Clic en el fondo semitransparente cierra el formulario
        if (event.getTarget() == overlayPane) {
            cancelar();
        }
    }

    @FXML
    private void consumirClick(MouseEvent event) {
        // Evita que el clic en la card del formulario cierre el overlay
        event.consume();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private LocalTime parsarHora(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalTime.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }

    private String minutosATexto(int minutos) {
        return switch (minutos) {
            case 5 -> "5 min antes";
            case 10 -> "10 min antes";
            case 30 -> "30 min antes";
            case 60 -> "1 hora antes";
            default -> "15 min antes";
        };
    }

    private int textoAMinutos(String texto) {
        if (texto == null) return 15;
        return switch (texto) {
            case "5 min antes" -> 5;
            case "10 min antes" -> 10;
            case "30 min antes" -> 30;
            case "1 hora antes" -> 60;
            default -> 15;
        };
    }
}
