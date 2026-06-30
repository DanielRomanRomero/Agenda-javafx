package com.agenda.utils;

import com.agenda.services.ConfiguracionService;
import javafx.scene.Scene;

public class ThemeManager {

    private static Scene scene;

    private ThemeManager() {}

    public static void setScene(Scene s) {
        scene = s;
    }

    public static void aplicarTema(String tema) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String base = recurso("/com/agenda/styles/base.css");
        String theme = "claro".equals(tema)
                ? recurso("/com/agenda/styles/light-theme.css")
                : recurso("/com/agenda/styles/dark-theme.css");
        if (base != null) scene.getStylesheets().add(base);
        if (theme != null) scene.getStylesheets().add(theme);
    }

    public static void toggleTheme() {
        String actual = getTemaActual();
        String nuevo = "oscuro".equals(actual) ? "claro" : "oscuro";
        aplicarTema(nuevo);
        ConfiguracionService.getInstance().setValor("tema", nuevo);
    }

    public static String getTemaActual() {
        return ConfiguracionService.getInstance().getValor("tema", "oscuro");
    }

    private static String recurso(String path) {
        java.net.URL url = ThemeManager.class.getResource(path);
        if (url == null) {
            System.err.println("[ThemeManager] RECURSO NO ENCONTRADO: " + path);
            return null;
        }
        return url.toExternalForm();
    }
}
