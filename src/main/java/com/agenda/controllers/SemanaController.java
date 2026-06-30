package com.agenda.controllers;

import com.agenda.models.Tarea;
import com.agenda.services.ConfiguracionService;
import com.agenda.services.TareaService;
import com.agenda.utils.DateUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SemanaController {

    @FXML private Label lblSemana;
    @FXML private ScrollPane scrollSemana;
    @FXML private HBox gridSemana;
    @FXML private TextArea txtNotasRapidas;

    private LocalDate inicioSemana;
    private MainController mainController;

    // Popup de resumen activo (para cerrarlo si se abre otro)
    private Node popupActivo;

    private static final double ALTURA_SLOT = 60.0;
    private static final int HORA_INICIO = 7;
    private static final int HORA_FIN = 23;
    private static final int MAX_CARDS_POR_SLOT = 3;
    private static final String CLAVE_NOTAS = "notas_rapidas";

    @FXML
    public void initialize() {
        inicioSemana = DateUtils.obtenerLunesDe(LocalDate.now());
        cargarSemana();
        cargarNotasRapidas();
        configurarAutoguardadoNotas();
    }

    public void setMainController(MainController ctrl) {
        this.mainController = ctrl;
    }

    public void navegarAFecha(LocalDate fecha) {
        inicioSemana = DateUtils.obtenerLunesDe(fecha);
        cargarSemana();
    }

    @FXML
    private void navegarAnterior() {
        inicioSemana = inicioSemana.minusWeeks(1);
        cargarSemana();
    }

    @FXML
    private void navegarSiguiente() {
        inicioSemana = inicioSemana.plusWeeks(1);
        cargarSemana();
    }

    @FXML
    private void navegarHoy() {
        inicioSemana = DateUtils.obtenerLunesDe(LocalDate.now());
        cargarSemana();
    }

    // ─── Notas rápidas ────────────────────────────────────────────────────────

    private void cargarNotasRapidas() {
        String notas = ConfiguracionService.getInstance().getValor(CLAVE_NOTAS, "");
        txtNotasRapidas.setText(notas);
    }

    private void configurarAutoguardadoNotas() {
        // Guardar con un pequeño debounce: cada vez que el usuario deja de escribir
        txtNotasRapidas.textProperty().addListener((obs, anterior, nuevo) ->
                ConfiguracionService.getInstance().setValor(CLAVE_NOTAS, nuevo));
    }

    // ─── Construcción del grid ────────────────────────────────────────────────

    private void cargarSemana() {
        cargarSemana(false);
    }

    private void cargarSemanaPreservandoScroll() {
        cargarSemana(true);
    }

    private void cargarSemana(boolean preservarScroll) {
        double scrollActual = scrollSemana.getVvalue();

        cerrarPopupActivo();
        LocalDate finSemana = inicioSemana.plusDays(6);
        lblSemana.setText(DateUtils.formatearRangoSemana(inicioSemana, finSemana));

        gridSemana.getChildren().clear();
        gridSemana.getChildren().add(crearColumnaHoras());

        List<Tarea> tareas = List.of();
        try {
            tareas = TareaService.getInstance().getTareasPorSemana(inicioSemana, finSemana);
        } catch (SQLException e) {
            System.err.println("Error al cargar tareas de la semana: " + e.getMessage());
        }

        final List<Tarea> tareasFinales = tareas;
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            javafx.scene.layout.StackPane columna = crearColumna(dia, tareasFinales);
            HBox.setHgrow(columna, Priority.ALWAYS);
            gridSemana.getChildren().add(columna);
        }

        if (preservarScroll) {
            Platform.runLater(() -> scrollSemana.setVvalue(scrollActual));
        } else {
            Platform.runLater(() -> {
                int horaActual = LocalTime.now().getHour();
                int horaScroll = Math.max(HORA_INICIO, Math.min(horaActual - 1, HORA_FIN - 2));
                double totalAltura = (HORA_FIN - HORA_INICIO) * ALTURA_SLOT;
                double posScroll = ((horaScroll - HORA_INICIO) * ALTURA_SLOT) / totalAltura;
                scrollSemana.setVvalue(Math.min(posScroll, 1.0));
            });
        }
    }

    private VBox crearColumnaHoras() {
        VBox col = new VBox();
        col.setMinWidth(44);
        col.setPrefWidth(44);
        col.setMaxWidth(44);

        Label headerSpace = new Label("");
        headerSpace.setMinHeight(40);
        headerSpace.setPrefHeight(40);
        col.getChildren().add(headerSpace);

        for (int h = HORA_INICIO; h < HORA_FIN; h++) {
            Label lblHora = new Label(String.format("%02d:00", h));
            lblHora.getStyleClass().add("hora-label");
            lblHora.setMinHeight(ALTURA_SLOT);
            lblHora.setPrefHeight(ALTURA_SLOT);
            lblHora.setAlignment(Pos.TOP_RIGHT);
            col.getChildren().add(lblHora);
        }
        return col;
    }

    private javafx.scene.layout.StackPane crearColumna(LocalDate dia, List<Tarea> todasLasTareas) {
        // StackPane: slots de fondo + overlay de cards encima
        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane();
        wrapper.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        wrapper.setMinWidth(0);

        VBox columna = new VBox();
        columna.setMinWidth(0);
        if (dia.equals(LocalDate.now())) {
            columna.getStyleClass().add("dia-hoy");
        }

        Label header = new Label(DateUtils.formatearFechaEncabezado(dia));
        header.getStyleClass().add("dia-header");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        header.setMinHeight(40);
        header.setPrefHeight(40);
        columna.getChildren().add(header);

        for (int h = HORA_INICIO; h < HORA_FIN; h++) {
            final LocalDate diaFinal = dia;
            final LocalTime horaFinal = LocalTime.of(h, 0);

            Pane slot = new Pane();
            slot.getStyleClass().add("hora-slot");
            slot.setMinHeight(ALTURA_SLOT);
            slot.setPrefHeight(ALTURA_SLOT);
            slot.setMaxWidth(Double.MAX_VALUE);
            slot.setOnMouseClicked(e -> {
                cerrarPopupActivo();
                abrirFormularioNueva(diaFinal, horaFinal);
            });
            columna.getChildren().add(slot);
        }

        // Tareas sin hora de inicio (chips sobre el header)
        todasLasTareas.stream()
                .filter(t -> dia.equals(t.getFecha()) && t.getHoraInicio() == null)
                .forEach(t -> colocarTareaEnColumna(columna, t));

        wrapper.getChildren().add(columna);

        // Overlay para las cards con hora: cubre toda la columna con posición absoluta
        List<Tarea> tareasDelDia = todasLasTareas.stream()
                .filter(t -> dia.equals(t.getFecha()) && t.getHoraInicio() != null)
                .collect(Collectors.toList());

        if (!tareasDelDia.isEmpty()) {
            Pane overlay = new Pane();
            // setManaged(false): el overlay no participa en el layout del StackPane
            // (no infla su tamaño), pero sigue siendo visible y recibe eventos de ratón
            overlay.setManaged(false);
            overlay.setPickOnBounds(false);
            overlay.setBackground(null);

            // Agrupar por hora de inicio para distribuir cards en la misma hora horizontalmente
            for (int h = HORA_INICIO; h < HORA_FIN; h++) {
                final int hora = h;
                List<Tarea> tareasSlot = tareasDelDia.stream()
                        .filter(t -> t.getHoraInicio().getHour() == hora)
                        .collect(Collectors.toList());
                if (!tareasSlot.isEmpty()) {
                    colocarTareasEnOverlay(overlay, tareasSlot, h);
                }
            }

            // Dimensionar el overlay igual que la columna tras cada layout pass
            columna.layoutBoundsProperty().addListener((obs, ov, nv) -> {
                overlay.resizeRelocate(0, 0, nv.getWidth(), nv.getHeight());
            });

            wrapper.getChildren().add(overlay);
        }

        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    // ─── Múltiples tareas por slot ────────────────────────────────────────────

    private void colocarTareasEnOverlay(Pane overlay, List<Tarea> tareas, int hora) {
        int totalVisible = Math.min(tareas.size(), MAX_CARDS_POR_SLOT);
        int extras = tareas.size() - totalVisible;

        // La posición Y base de este slot en el overlay (header de 40px + slots previos)
        double baseY = 40 + (hora - HORA_INICIO) * ALTURA_SLOT;

        overlay.widthProperty().addListener((obs, ov, nv) -> {
            double anchoSlot = nv.doubleValue();
            double anchoCard = extras > 0
                    ? (anchoSlot - 4) / (totalVisible + 0.6)
                    : (anchoSlot - 4) / totalVisible;

            // Reposicionar todas las cards y el label "+N" de este slot
            for (Node child : overlay.getChildren()) {
                if (child.getUserData() instanceof int[] datos && datos[0] == hora) {
                    int col = datos[1];
                    if (child instanceof VBox card) {
                        card.setPrefWidth(anchoCard - 2);
                        card.setLayoutX(2 + col * (anchoCard + 1));
                    } else if (child instanceof Label masLabel) {
                        masLabel.setLayoutX(2 + totalVisible * (anchoCard + 1));
                        masLabel.setPrefWidth(anchoSlot - masLabel.getLayoutX() - 2);
                    }
                }
            }
        });

        for (int i = 0; i < totalVisible; i++) {
            Tarea t = tareas.get(i);
            double alturaCard = calcularAlturaCard(t);
            double minutosOffset = t.getHoraInicio().getMinute();
            double offsetY = baseY + (minutosOffset / 60.0) * ALTURA_SLOT;

            VBox card = crearTareaCard(t);
            card.setLayoutY(offsetY);
            card.setPrefHeight(Math.max(alturaCard - 4, 20));
            card.setMaxHeight(Math.max(alturaCard - 4, 20));
            card.setPrefWidth(Region.USE_COMPUTED_SIZE);
            card.setUserData(new int[]{hora, i});
            overlay.getChildren().add(card);
        }

        if (extras > 0) {
            List<Tarea> tareasExtra = new ArrayList<>(tareas.subList(totalVisible, tareas.size()));
            Label masLabel = new Label("+" + extras + " más");
            masLabel.getStyleClass().add("mas-tareas-label");
            masLabel.setLayoutY(baseY + 4);
            masLabel.setUserData(new int[]{hora, -1});
            masLabel.setOnMouseClicked(e -> {
                e.consume();
                mostrarPopupExtras(e, tareasExtra);
            });
            overlay.getChildren().add(masLabel);
        }
    }

    private void colocarTareaEnColumna(VBox columna, Tarea tarea) {
        Label chip = new Label("• " + tarea.getTitulo());
        chip.getStyleClass().add("tarea-sin-hora");
        chip.setOnMouseClicked(e -> {
            e.consume();
            mostrarPopupResumen(e, tarea);
        });
        columna.getChildren().add(1, chip); // después del header
    }

    private double calcularAlturaCard(Tarea tarea) {
        if (tarea.getHoraFin() == null) return ALTURA_SLOT;
        long min = java.time.Duration.between(tarea.getHoraInicio(), tarea.getHoraFin()).toMinutes();
        return min > 0 ? (min / 60.0) * ALTURA_SLOT : ALTURA_SLOT;
    }

    // ─── Card de tarea ────────────────────────────────────────────────────────

    private VBox crearTareaCard(Tarea tarea) {
        VBox card = new VBox(2);
        card.getStyleClass().add("tarea-card");
        card.getStyleClass().add("nota".equals(tarea.getTipo()) ? "tarea-card-nota" : "tarea-card-tarea");
        if (tarea.isCompletada()) card.getStyleClass().add("tarea-card-completada");
        card.setPadding(new Insets(4, 6, 4, 8));
        card.setClip(null); // el clip se aplica dinámicamente via maxHeight

        HBox fila = new HBox(4);
        fila.setAlignment(Pos.CENTER_LEFT);

        // Checkmark visual si está completada
        Label checkIcon = new Label(tarea.isCompletada() ? "✓" : "");
        checkIcon.getStyleClass().add("tarea-check-icon");

        Label lblTitulo = new Label(tarea.getTitulo());
        lblTitulo.getStyleClass().add("tarea-titulo-label");
        if (tarea.isCompletada()) lblTitulo.getStyleClass().add("tarea-titulo-completada");
        lblTitulo.setWrapText(false);
        HBox.setHgrow(lblTitulo, Priority.ALWAYS);

        fila.getChildren().addAll(checkIcon, lblTitulo);

        if (tarea.isRecordatorio()) {
            Label badge = new Label("🔔");
            badge.getStyleClass().add("recordatorio-badge");
            fila.getChildren().add(badge);
        }

        card.getChildren().add(fila);

        // Mostrar descripción si hay espacio (card de al menos ~46px de alto)
        String desc = tarea.getDescripcion();
        if (desc != null && !desc.isBlank()) {
            double alturaCard = calcularAlturaCard(tarea);
            if (alturaCard >= 46) {
                Label lblDesc = new Label(desc);
                lblDesc.getStyleClass().add("tarea-desc-label");
                lblDesc.setWrapText(true);
                lblDesc.setMaxHeight(32); // máximo 2 líneas de descripción
                VBox.setVgrow(lblDesc, Priority.NEVER);
                card.getChildren().add(lblDesc);
            }
        }

        // Clic simple → popup resumen; doble clic → formulario edición
        card.setOnMouseClicked(e -> {
            e.consume();
            if (e.getClickCount() == 2) {
                cerrarPopupActivo();
                abrirFormularioEdicion(tarea);
            } else {
                mostrarPopupResumen(e, tarea);
            }
        });

        return card;
    }

    // ─── Popup resumen ────────────────────────────────────────────────────────

    private void mostrarPopupResumen(MouseEvent evento, Tarea tarea) {
        cerrarPopupActivo();
        if (mainController == null) return;
        StackPane rootPane = mainController.getRootPane();

        VBox popup = new VBox(10);
        popup.getStyleClass().add("popup-resumen");
        popup.setPadding(new Insets(14, 16, 14, 16));
        popup.setMaxWidth(280);

        // Título
        Label lblTitulo = new Label(tarea.getTitulo());
        lblTitulo.getStyleClass().add("popup-titulo");
        lblTitulo.setWrapText(true);

        // Info secundaria
        VBox info = new VBox(4);
        if (tarea.getHoraInicio() != null) {
            String rango = tarea.getHoraInicio().toString()
                    + (tarea.getHoraFin() != null ? " – " + tarea.getHoraFin() : "");
            Label lblHora = new Label("🕐 " + rango);
            lblHora.getStyleClass().add("popup-info");
            info.getChildren().add(lblHora);
        }
        if (tarea.getDescripcion() != null && !tarea.getDescripcion().isBlank()) {
            Label lblDesc = new Label(tarea.getDescripcion());
            lblDesc.getStyleClass().add("popup-info");
            lblDesc.setWrapText(true);
            info.getChildren().add(lblDesc);
        }
        Label lblTipo = new Label(("nota".equals(tarea.getTipo()) ? "📝 Nota" : "✅ Tarea")
                + (tarea.isCompletada() ? " · Completada" : ""));
        lblTipo.getStyleClass().add("popup-info");
        info.getChildren().add(lblTipo);

        // Botones de acción
        HBox botones = new HBox(8);
        botones.setAlignment(Pos.CENTER_LEFT);

        Button btnCompletar = new Button(tarea.isCompletada() ? "Desmarcar" : "✓ Completar");
        btnCompletar.getStyleClass().add(tarea.isCompletada() ? "btn-ghost" : "btn-completar");
        btnCompletar.setOnAction(e -> {
            try {
                TareaService.getInstance().marcarCompletada(tarea.getId(), !tarea.isCompletada());
                cerrarPopupActivo();
                cargarSemanaPreservandoScroll();
            } catch (SQLException ex) {
                System.err.println("Error al marcar completada: " + ex.getMessage());
            }
        });

        Button btnEditar = new Button("✏ Editar");
        btnEditar.getStyleClass().add("btn-ghost");
        btnEditar.setOnAction(e -> {
            cerrarPopupActivo();
            abrirFormularioEdicion(tarea);
        });

        Button btnEliminar = new Button("🗑");
        btnEliminar.getStyleClass().add("btn-danger-small");
        btnEliminar.setOnAction(e -> mostrarConfirmEliminarEnPopup(popup, botones, tarea));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        botones.getChildren().addAll(btnCompletar, btnEditar, spacer, btnEliminar);

        popup.getChildren().addAll(lblTitulo, info, new Separator(), botones);

        // Overlay transparente para cerrar el popup al clicar fuera
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: transparent;");
        overlay.setOnMouseClicked(e -> cerrarPopupActivo());

        // Posicionar el popup cerca del cursor, ajustando si se sale de los bordes
        posicionarPopupEnCursor(popup, overlay, rootPane, evento.getSceneX(), evento.getSceneY());

        overlay.getChildren().add(popup);
        rootPane.getChildren().add(overlay);
        popupActivo = overlay;

        // Ajustar tras layout si el popup se sale por la derecha o abajo
        popup.layoutBoundsProperty().addListener((obs, ov, nv) ->
                posicionarPopupEnCursor(popup, overlay, rootPane, evento.getSceneX(), evento.getSceneY()));
    }

    private void posicionarPopupEnCursor(VBox popup, StackPane overlay,
                                         StackPane rootPane, double sceneX, double sceneY) {
        javafx.geometry.Point2D local = rootPane.sceneToLocal(sceneX, sceneY);
        double x = local.getX() + 12;
        double y = local.getY() + 12;

        double popupW = popup.prefWidth(-1);
        double popupH = popup.prefHeight(-1);
        double rootW = rootPane.getWidth();
        double rootH = rootPane.getHeight();

        if (popupW > 0 && x + popupW > rootW - 8) x = Math.max(8, rootW - popupW - 8);
        if (popupH > 0 && y + popupH > rootH - 8) y = Math.max(8, rootH - popupH - 8);

        StackPane.setAlignment(popup, Pos.TOP_LEFT);
        StackPane.setMargin(popup, new Insets(y, 0, 0, x));
    }

    private void mostrarConfirmEliminarEnPopup(VBox popup, HBox botonesActuales, Tarea tarea) {
        popup.getChildren().remove(botonesActuales);

        HBox confirmar = new HBox(8);
        confirmar.setAlignment(Pos.CENTER_LEFT);
        Label pregunta = new Label("¿Eliminar?");
        pregunta.getStyleClass().add("popup-info");
        Button btnSi = new Button("Eliminar");
        btnSi.getStyleClass().add("btn-danger");
        btnSi.setOnAction(e -> {
            try {
                TareaService.getInstance().eliminar(tarea.getId());
                cerrarPopupActivo();
                cargarSemanaPreservandoScroll();
            } catch (SQLException ex) {
                System.err.println("Error al eliminar: " + ex.getMessage());
            }
        });
        Button btnNo = new Button("Cancelar");
        btnNo.getStyleClass().add("btn-ghost");
        btnNo.setOnAction(e -> {
            popup.getChildren().remove(confirmar);
            popup.getChildren().add(botonesActuales);
        });
        confirmar.getChildren().addAll(pregunta, btnSi, btnNo);
        popup.getChildren().add(confirmar);
    }

    private void mostrarPopupExtras(MouseEvent evento, List<Tarea> extras) {
        cerrarPopupActivo();
        if (mainController == null) return;
        StackPane rootPane = mainController.getRootPane();

        VBox popup = new VBox(6);
        popup.getStyleClass().add("popup-resumen");
        popup.setPadding(new Insets(12, 14, 12, 14));
        popup.setMaxWidth(240);

        Label titulo = new Label("Más tareas");
        titulo.getStyleClass().add("popup-titulo");
        popup.getChildren().add(titulo);

        for (Tarea t : extras) {
            Label item = new Label("• " + t.getTitulo());
            item.getStyleClass().add("popup-info");
            item.setStyle("-fx-cursor: hand;");
            item.setOnMouseClicked(e -> {
                e.consume();
                cerrarPopupActivo();
                mostrarPopupResumen(e, t);
            });
            popup.getChildren().add(item);
        }

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: transparent;");
        overlay.setOnMouseClicked(e -> cerrarPopupActivo());

        posicionarPopupEnCursor(popup, overlay, rootPane, evento.getSceneX(), evento.getSceneY());
        popup.layoutBoundsProperty().addListener((obs, ov, nv) ->
                posicionarPopupEnCursor(popup, overlay, rootPane, evento.getSceneX(), evento.getSceneY()));

        overlay.getChildren().add(popup);
        rootPane.getChildren().add(overlay);
        popupActivo = overlay;
    }

    private void cerrarPopupActivo() {
        if (popupActivo != null && mainController != null) {
            mainController.getRootPane().getChildren().remove(popupActivo);
            popupActivo = null;
        }
    }

    // ─── Formularios ──────────────────────────────────────────────────────────

    private void abrirFormularioNueva(LocalDate fecha, LocalTime hora) {
        abrirFormulario(null, fecha, hora);
    }

    private void abrirFormularioEdicion(Tarea tarea) {
        abrirFormulario(tarea, null, null);
    }

    private void abrirFormulario(Tarea tarea, LocalDate fecha, LocalTime hora) {
        if (mainController == null) return;
        StackPane rootPane = mainController.getRootPane();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agenda/views/tarea-form.fxml"));
            Parent formRoot = loader.load();
            TareaFormController ctrl = loader.getController();

            if (tarea != null) {
                ctrl.setTarea(tarea);
            } else {
                ctrl.setFechaHoraInicial(fecha, hora);
            }

            ctrl.setOnGuardado(() -> {
                rootPane.getChildren().remove(formRoot);
                cargarSemanaPreservandoScroll();
            });
            ctrl.setOnCancelado(() -> rootPane.getChildren().remove(formRoot));

            rootPane.getChildren().add(formRoot);

        } catch (IOException e) {
            System.err.println("Error al abrir formulario: " + e.getMessage());
        }
    }
}
