package com.agenda.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Tarea {

    private int id;
    private String titulo;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String tipo; // "tarea" | "nota"
    private boolean completada;
    private boolean recordatorio;
    private int minutosAntes; // 5, 10, 15, 30, 60

    public Tarea() {}

    public Tarea(String titulo, LocalDate fecha) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.tipo = "tarea";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public boolean isRecordatorio() { return recordatorio; }
    public void setRecordatorio(boolean recordatorio) { this.recordatorio = recordatorio; }

    public int getMinutosAntes() { return minutosAntes; }
    public void setMinutosAntes(int minutosAntes) { this.minutosAntes = minutosAntes; }
}
