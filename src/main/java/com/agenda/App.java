package com.agenda;

import com.agenda.services.DatabaseService;
import com.agenda.services.RecordatorioService;
import com.agenda.utils.NotificacionUtils;
import com.agenda.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Inicializar base de datos (crea tablas si no existen)
        try {
            DatabaseService.getInstance().getConnection();
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agenda/views/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 760);
        ThemeManager.setScene(scene);
        ThemeManager.aplicarTema(ThemeManager.getTemaActual());

        // Icono de ventana y barra de tareas
        try (java.io.InputStream iconStream = getClass().getResourceAsStream("/com/agenda/styles/icon.png")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        primaryStage.setTitle("Agenda");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Bandeja del sistema para notificaciones
        NotificacionUtils.inicializar(() -> {
            RecordatorioService.getInstance().shutdown();
            DatabaseService.getInstance().close();
        });

        // Iniciar motor de recordatorios
        RecordatorioService.getInstance().start();
    }

    @Override
    public void stop() {
        RecordatorioService.getInstance().shutdown();
        NotificacionUtils.remover();
        DatabaseService.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
