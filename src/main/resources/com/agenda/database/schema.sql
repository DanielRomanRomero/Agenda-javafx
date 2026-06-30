CREATE TABLE IF NOT EXISTS tareas (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo        TEXT    NOT NULL,
    descripcion   TEXT,
    fecha         TEXT    NOT NULL,
    hora_inicio   TEXT,
    hora_fin      TEXT,
    tipo          TEXT    NOT NULL DEFAULT 'tarea',
    completada    INTEGER NOT NULL DEFAULT 0,
    recordatorio  INTEGER NOT NULL DEFAULT 0,
    minutos_antes INTEGER DEFAULT 15
);

CREATE TABLE IF NOT EXISTS configuracion (
    clave TEXT PRIMARY KEY,
    valor TEXT NOT NULL
);

INSERT OR IGNORE INTO configuracion (clave, valor) VALUES ('tema', 'oscuro');
