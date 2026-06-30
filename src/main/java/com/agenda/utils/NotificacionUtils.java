package com.agenda.utils;

import javafx.application.Platform;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;

public class NotificacionUtils {

    private static TrayIcon trayIcon;
    private static Runnable onSalir;

    private NotificacionUtils() {}

    public static boolean isSoportado() {
        return SystemTray.isSupported();
    }

    public static void inicializar(Runnable accionSalir) {
        if (!isSoportado()) return;
        onSalir = accionSalir;

        // La app sigue viva en bandeja aunque se cierre la ventana principal
        Platform.setImplicitExit(false);

        try {
            URL iconUrl = NotificacionUtils.class.getResource("/com/agenda/styles/icon.png");
            Image imagen;
            if (iconUrl != null) {
                imagen = Toolkit.getDefaultToolkit().createImage(iconUrl);
            } else {
                // Icono genérico si no se encuentra el archivo
                imagen = Toolkit.getDefaultToolkit().createImage(new byte[0]);
            }

            trayIcon = new TrayIcon(imagen, "Agenda");
            trayIcon.setImageAutoSize(true);

            PopupMenu menu = new PopupMenu();
            MenuItem itemSalir = new MenuItem("Salir de Agenda");
            itemSalir.addActionListener(e -> {
                remover();
                if (onSalir != null) onSalir.run();
                Platform.exit();
            });
            menu.add(itemSalir);
            trayIcon.setPopupMenu(menu);

            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            // Bandeja no disponible en este sistema
        }
    }

    public static void mostrarNotificacion(String titulo, String mensaje) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.INFO);
        }
    }

    public static void remover() {
        if (trayIcon != null && isSoportado()) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }
}
