# CLAUDE.md

## Proyecto
Agenda semanal de productividad con calendario mensual, gestión de tareas/notas y recordatorios.
Aplicación de escritorio construida con JavaFX y Java 25.

## Rol del agente
Desarrollador Java senior con 15 años de experiencia en aplicaciones de escritorio con JavaFX.

## Objetivo
Crear una aplicación de escritorio tipo agenda de productividad donde el usuario pueda:
- Ver su semana actual de un vistazo con las tareas organizadas por horas
- Navegar a un calendario mensual con un clic
- Añadir, editar y eliminar tareas y notas
- Configurar recordatorios con notificaciones del sistema

## Stack tecnológico
- Java 25
- JavaFX (con SceneBuilder-compatible FXML)
- SQLite como base de datos local (usar SQLite JDBC: org.xerial:sqlite-jdbc)
- Maven como gestor de dependencias y build

## Estructura de archivos
agenda/
├── pom.xml
├── CLAUDE.md
└── src/
└── main/
├── java/
│   └── com/agenda/
│       ├── App.java
│       ├── controllers/
│       ├── models/
│       ├── services/
│       └── utils/
└── resources/
└── com/agenda/
├── views/ (archivos FXML)
├── styles/ (archivos CSS)
└── database/ (scripts SQL)

## Funcionalidades

### Vista principal — Semana actual
- Al abrir la app se muestra la semana actual (lunes a domingo)
- Cada día tiene una columna con las horas del día (07:00 a 23:00)
- Las tareas/notas se muestran en el bloque horario correspondiente
- Navegación entre semanas con botones anterior/siguiente
- Botón para volver a "Hoy" en cualquier momento
- Botón para abrir el calendario mensual

### Vista calendario mensual
- Vista de mes completo en forma de cuadrícula
- Los días con tareas tienen un indicador visual
- Al hacer clic en un día, se navega a esa semana en la vista principal

### Gestión de tareas y notas
- Crear tarea/nota haciendo clic en un bloque horario o con botón "+"
- Formulario con: título (obligatorio), descripción (opcional), fecha, hora inicio, hora fin, tipo (tarea o nota)
- Editar haciendo doble clic sobre una tarea existente
- Eliminar con botón de borrado con confirmación visual
- Las tareas completadas se pueden marcar como hechas (tachado visual)

### Recordatorios y notificaciones
- Al crear/editar una tarea, opción de activar recordatorio
- Selección de anticipación: 5, 10, 15, 30 minutos o 1 hora antes
- Notificación del sistema operativo cuando llega el momento
- Indicador visual en la app de tareas con recordatorio pendiente

### Temas visuales
- Toggle en la interfaz para cambiar entre tema claro y oscuro
- La preferencia de tema se guarda y persiste entre sesiones

## Base de datos SQLite

### Tabla `tareas`
- id (INTEGER PRIMARY KEY AUTOINCREMENT)
- titulo (TEXT NOT NULL)
- descripcion (TEXT)
- fecha (TEXT — formato ISO: yyyy-MM-dd)
- hora_inicio (TEXT — formato HH:mm)
- hora_fin (TEXT — formato HH:mm)
- tipo (TEXT — 'tarea' o 'nota')
- completada (INTEGER — 0 o 1)
- recordatorio (INTEGER — 0 o 1)
- minutos_antes (INTEGER — 5, 10, 15, 30 o 60)
- tema (TEXT — guardado en tabla separada 'configuracion')

### Tabla `configuracion`
- clave (TEXT PRIMARY KEY)
- valor (TEXT)

## Diseño visual
- Estilo limpio y profesional, inspirado en apps de productividad modernas (Notion, Google Calendar)
- Tema oscuro: fondo #1E1E2E, acentos en #7C3AED (violeta)
- Tema claro: fondo #F8F9FA, acentos en #7C3AED (violeta)
- Tipografía: Inter o sistema por defecto
- Bordes redondeados, sombras suaves, transiciones de color

## Reglas de código
- Nunca usar var, siempre usar tipos explícitos
- No usar Alert de JavaFX para feedback, todo debe ser visual en la UI
- Código legible y bien comentado
- Separación clara entre controladores, modelos y servicios
- Si el agente duda, revise este CLAUDE.md antes de preguntar

## Estado actual
Proyecto vacío. No hay ningún archivo creado todavía.

## Próximo paso
Construir la aplicación completa según las especificaciones de este documento.