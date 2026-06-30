package com.agenda.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

    private static final Locale LOCALE_ES = new Locale("es", "ES");
    private static final int HORA_INICIO_GRID = 7;

    private DateUtils() {}

    public static LocalDate obtenerLunesDe(LocalDate fecha) {
        return fecha.with(DayOfWeek.MONDAY);
    }

    /** "Lun 23" */
    public static String formatearFechaEncabezado(LocalDate fecha) {
        String dia = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES);
        String diaCapital = dia.substring(0, 1).toUpperCase(LOCALE_ES) + dia.substring(1);
        return diaCapital + " " + fecha.getDayOfMonth();
    }

    /** "23 Jun – 29 Jun 2025" */
    public static String formatearRangoSemana(LocalDate lunes, LocalDate domingo) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM", LOCALE_ES);
        return lunes.format(fmt) + " – " + domingo.format(fmt) + " " + lunes.getYear();
    }

    /** "Junio 2025" */
    public static String formatearMesAnio(YearMonth mes) {
        String nombre = mes.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);
        String capital = nombre.substring(0, 1).toUpperCase(LOCALE_ES) + nombre.substring(1);
        return capital + " " + mes.getYear();
    }

    /** Índice de fila en el grid (07:00 → 0, 08:00 → 1 … 22:00 → 15) */
    public static int calcularFilaHora(LocalTime hora) {
        return Math.max(0, hora.getHour() - HORA_INICIO_GRID);
    }

    /** Offset en píxeles dentro de una celda de alturaSlot px según los minutos */
    public static double calcularOffsetPixeles(LocalTime hora, double alturaSlot) {
        return (hora.getMinute() / 60.0) * alturaSlot;
    }

    public static int getHoraInicioGrid() {
        return HORA_INICIO_GRID;
    }
}
