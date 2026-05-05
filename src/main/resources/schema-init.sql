-- =====================================================================
-- AgendarReservas — Sistema Psicólogo-Paciente
-- Esquema PostgreSQL 14+
-- =====================================================================
-- Ejecución completa (base de datos nueva):
--   psql -U <user> -d <dbname> -f schema-init.sql
--
-- Si la base de datos ya tiene las tablas viejas (reserva con cliente):
--   ver sección "MIGRACIÓN DESDE ESQUEMA ANTERIOR" al final del archivo.
-- =====================================================================

SET timezone = 'America/Santiago';

-- =====================================================================
-- 0. DROP (orden inverso para respetar FKs)
-- =====================================================================
DROP TABLE IF EXISTS notas_sesion       CASCADE;
DROP TABLE IF EXISTS historial_paciente CASCADE;
DROP TABLE IF EXISTS refresh_tokens     CASCADE;
DROP TABLE IF EXISTS reserva            CASCADE;
DROP TABLE IF EXISTS user_roles         CASCADE;
DROP TABLE IF EXISTS users              CASCADE;

-- =====================================================================
-- 1. USERS
-- =====================================================================
CREATE TABLE users (
    id                 BIGSERIAL     PRIMARY KEY,

    -- Credenciales
    username           VARCHAR(50)   NOT NULL UNIQUE,
    email              VARCHAR(100)  NOT NULL UNIQUE,
    password           VARCHAR(120)  NOT NULL,
    enabled            BOOLEAN       NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN       NOT NULL DEFAULT TRUE,

    -- Perfil del paciente (NULL para usuarios admin)
    nombre             VARCHAR(100),
    apellidos          VARCHAR(150),
    telefono           VARCHAR(20),
    fecha_nacimiento   DATE,
    edad               INTEGER,
    rut                VARCHAR(12)   UNIQUE,
    genero             VARCHAR(25)
        CONSTRAINT chk_users_genero
            CHECK (genero IN ('MASCULINO', 'FEMENINO', 'PREFIERO_NO_DECIRLO')),
    paciente_anterior  BOOLEAN,
    estudiante         BOOLEAN,

    -- Auditoría
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ,
    last_login         TIMESTAMPTZ
);

-- =====================================================================
-- 2. USER_ROLES
-- =====================================================================
CREATE TABLE user_roles (
    user_id BIGINT      NOT NULL,
    role    VARCHAR(20) NOT NULL
        CONSTRAINT chk_user_roles_role
            CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER')),
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================================
-- 3. RESERVA
-- =====================================================================
CREATE TABLE reserva (
    id_reserva         BIGSERIAL    PRIMARY KEY,

    -- Participantes
    id_paciente        BIGINT       NOT NULL,
    id_psicologo       BIGINT       NOT NULL,

    -- Detalles de la sesión
    fecha_reserva      TIMESTAMP    NOT NULL,
    fecha_termino      TIMESTAMP,
    duracion_minutos   INTEGER      NOT NULL DEFAULT 60
        CONSTRAINT chk_reserva_duracion
            CHECK (duracion_minutos >= 30
               AND duracion_minutos <= 360
               AND duracion_minutos % 30 = 0),
    motivo_consulta    VARCHAR(500),
    modalidad          VARCHAR(20)  NOT NULL
        CONSTRAINT chk_reserva_modalidad
            CHECK (modalidad IN ('PRESENCIAL', 'VIRTUAL')),

    -- Financiero
    precio             BIGINT       NOT NULL DEFAULT 0
        CONSTRAINT chk_reserva_precio   CHECK (precio >= 0),
    abonado            BIGINT                DEFAULT 0
        CONSTRAINT chk_reserva_abonado  CHECK (abonado >= 0),
    CONSTRAINT chk_reserva_abonado_precio
        CHECK (abonado IS NULL OR abonado <= precio),

    -- Estado
    estado             VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
        CONSTRAINT chk_reserva_estado
            CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA')),

    -- Auditoría
    fecha_creacion     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMPTZ,

    CONSTRAINT fk_reserva_paciente
        FOREIGN KEY (id_paciente)  REFERENCES users(id),
    CONSTRAINT fk_reserva_psicologo
        FOREIGN KEY (id_psicologo) REFERENCES users(id)
);

-- =====================================================================
-- 4. HISTORIAL_PACIENTE  (un registro por sesión/reserva)
-- =====================================================================
CREATE TABLE historial_paciente (
    id_historial        BIGSERIAL    PRIMARY KEY,

    -- Sesión vinculada (1:1 con reserva)
    id_reserva          BIGINT       NOT NULL UNIQUE,

    -- Participantes (desnormalizados para acceso directo sin JOIN)
    id_paciente         BIGINT       NOT NULL,
    id_psicologo        BIGINT       NOT NULL,

    -- Datos clínicos
    motivo_consulta     VARCHAR(500),
    tipo_sesion         VARCHAR(20)
        CONSTRAINT chk_historial_tipo_sesion
            CHECK (tipo_sesion IN ('INICIAL', 'SEGUIMIENTO', 'CRISIS', 'EVALUACION', 'CIERRE')),
    crisis              BOOLEAN      NOT NULL DEFAULT FALSE,
    alta                BOOLEAN      NOT NULL DEFAULT FALSE,
    posible_abandono    BOOLEAN      NOT NULL DEFAULT FALSE,
    notas_generales     TEXT,

    -- Auditoría
    fecha_creacion      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,

    CONSTRAINT fk_historial_reserva
        FOREIGN KEY (id_reserva)   REFERENCES reserva(id_reserva)  ON DELETE CASCADE,
    CONSTRAINT fk_historial_paciente
        FOREIGN KEY (id_paciente)  REFERENCES users(id),
    CONSTRAINT fk_historial_psicologo
        FOREIGN KEY (id_psicologo) REFERENCES users(id)
);

-- =====================================================================
-- 5. NOTAS_SESION
-- =====================================================================
CREATE TABLE notas_sesion (
    id_nota        BIGSERIAL   PRIMARY KEY,
    id_historial   BIGINT      NOT NULL,
    nota           TEXT        NOT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notas_historial
        FOREIGN KEY (id_historial) REFERENCES historial_paciente(id_historial) ON DELETE CASCADE
);

-- =====================================================================
-- 6. REFRESH_TOKENS
-- =====================================================================
CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    user_agent  VARCHAR(500),
    ip_address  VARCHAR(45),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================================
-- 7. ÍNDICES
-- =====================================================================

-- users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_rut      ON users(rut) WHERE rut IS NOT NULL;

-- reserva
CREATE INDEX IF NOT EXISTS idx_reserva_paciente   ON reserva(id_paciente);
CREATE INDEX IF NOT EXISTS idx_reserva_psicologo  ON reserva(id_psicologo);
CREATE INDEX IF NOT EXISTS idx_reserva_estado     ON reserva(estado);
CREATE INDEX IF NOT EXISTS idx_reserva_fecha      ON reserva(fecha_reserva DESC);
CREATE INDEX IF NOT EXISTS idx_reserva_pac_estado ON reserva(id_paciente, estado);

-- historial_paciente
CREATE INDEX IF NOT EXISTS idx_historial_paciente  ON historial_paciente(id_paciente);
CREATE INDEX IF NOT EXISTS idx_historial_psicologo ON historial_paciente(id_psicologo);
CREATE INDEX IF NOT EXISTS idx_historial_fecha     ON historial_paciente(fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_historial_pac_fecha ON historial_paciente(id_paciente, fecha_creacion DESC);

-- notas_sesion
CREATE INDEX IF NOT EXISTS idx_notas_historial ON notas_sesion(id_historial);

-- refresh_tokens
CREATE INDEX IF NOT EXISTS idx_refresh_token   ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_user    ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_expiry  ON refresh_tokens(expiry_date);

-- =====================================================================
-- 8. USUARIO ADMIN INICIAL
-- =====================================================================
-- Contraseña: Admin123!  (BCrypt strength 12)
-- IMPORTANTE: Cambiar esta contraseña en producción.
-- Para generar un nuevo hash:
--   spring.security.user.password o usar el endpoint /api/auth/register
--   y luego UPDATE users SET roles = ROLE_ADMIN manualmente.

INSERT INTO users (
    username, email, password,
    enabled, account_non_locked,
    created_at, updated_at
) VALUES (
    'admin',
    'admin@agendarreservas.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3oL/A7vZKG',
    TRUE, TRUE,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER'  FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- =====================================================================
-- MIGRACIÓN DESDE ESQUEMA ANTERIOR
-- =====================================================================
-- Si ya tienes una base de datos con el esquema viejo (tabla reserva con
-- cliente, nombreProducto, lugarEncuentro, etc.), ejecuta este bloque
-- en lugar del DROP/CREATE anterior.
--
-- PASO 1: Agregar columnas nuevas a users
-- =====================================================================
--
-- ALTER TABLE users
--     ADD COLUMN IF NOT EXISTS nombre            VARCHAR(100),
--     ADD COLUMN IF NOT EXISTS apellidos         VARCHAR(150),
--     ADD COLUMN IF NOT EXISTS telefono          VARCHAR(20),
--     ADD COLUMN IF NOT EXISTS fecha_nacimiento  DATE,
--     ADD COLUMN IF NOT EXISTS edad              INTEGER,
--     ADD COLUMN IF NOT EXISTS rut               VARCHAR(12),
--     ADD COLUMN IF NOT EXISTS genero            VARCHAR(25),
--     ADD COLUMN IF NOT EXISTS paciente_anterior BOOLEAN,
--     ADD COLUMN IF NOT EXISTS estudiante        BOOLEAN;
--
-- ALTER TABLE users
--     ADD CONSTRAINT uq_users_rut UNIQUE (rut);
--
-- ALTER TABLE users
--     ADD CONSTRAINT chk_users_genero
--         CHECK (genero IN ('MASCULINO', 'FEMENINO', 'PREFIERO_NO_DECIRLO'));
--
-- =====================================================================
-- PASO 2: Recrear tabla reserva (datos históricos NO compatibles)
-- =====================================================================
--
-- DROP TABLE IF EXISTS reserva CASCADE;
--
-- CREATE TABLE reserva ( ... );  -- misma definición del bloque 3 arriba
--
-- =====================================================================
-- PASO 3: Crear tablas nuevas (historial_paciente, notas_sesion)
-- =====================================================================
--
-- Ejecutar los bloques 4 y 5 del script.
--
-- =====================================================================
-- NOTAS GENERALES
-- =====================================================================
-- 1. ddl-auto en desarrollo: update (Hibernate ajusta el esquema solo).
-- 2. ddl-auto en producción: validate (Hibernate solo valida, no modifica).
-- 3. Generar JWT_SECRET para producción: openssl rand -base64 64
-- 4. El campo fecha_reserva usa TIMESTAMP (sin zona) porque el backend
--    compara con LocalDateTime.now(). Asegurarse de que el servidor
--    PostgreSQL y la JVM usen la misma zona horaria.
-- =====================================================================
