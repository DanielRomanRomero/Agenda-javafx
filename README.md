# Agenda — Productivity Desktop App

Agenda semanal de productividad desarrollada con JavaFX y Java 25. Incluye calendario mensual, gestión de tareas/notas y recordatorios del sistema.

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue)
![SQLite](https://img.shields.io/badge/SQLite-3.49-green?logo=sqlite)
![Maven](https://img.shields.io/badge/Maven-3.x-red?logo=apachemaven)

---

## Características

- **Vista semanal** — visualiza la semana actual con las tareas colocadas en su franja horaria (07:00–23:00). Navega entre semanas o vuelve al día actual.
- **Calendario mensual** — vista completa en cuadrícula; los días con tareas muestran un indicador visual. Clic en cualquier día para ir a esa semana.
- **Gestión de tareas y notas** — creación rápida desde una franja horaria o el botón `+`. Cada entrada tiene título, descripción opcional, hora de inicio/fin y tipo (tarea o nota). Edición con doble clic; borrado con confirmación.
- **Recordatorios** — notificaciones configurables (5, 10, 15, 30 minutos o 1 hora antes de cada tarea), gestionadas a nivel de sistema operativo.
- **Base de datos SQLite local** — todos los datos se guardan en local, sin necesidad de cuenta.

---

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 25 |
| JavaFX | 25.0.2 |
| SQLite | 3.49 (vía `org.xerial:sqlite-jdbc`) |
| Build tool | Maven 3 |
| Claude Code

---

## Estructura del proyecto
agenda/
├── pom.xml
└── src/main/
├── java/com/agenda/
│   ├── App.java
│   ├── controllers/
│   ├── models/
│   ├── services/
│   └── utils/
└── resources/com/agenda/
├── views/
├── styles/
└── database/

---

## Ejecución rápida

> **Nota sobre rutas locales:** El script `iniciar-agenda.bat` incluye rutas hardcodeadas a una instalación concreta de Java y Maven (`C:\Dev\JDKs\jdk-25`, `C:\Program Files\Apache NetBeans\...`). Si usas otra instalación, edita esas rutas antes de ejecutarlo, o lanza la app directamente con Maven estándar:
> ```bash
> mvn clean javafx:run
> ```
> siempre que `JAVA_HOME` apunte a un JDK 25 y Maven esté en el PATH.

---

## Autor

Daniel Román Romero