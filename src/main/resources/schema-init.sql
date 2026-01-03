-- =====================================================
-- SQL Script de Inicialización para Sistema de Autenticación JWT
-- =====================================================
-- Este script crea las tablas necesarias y un usuario admin inicial
-- Ejecutar solo si hibernate.ddl-auto no está creando las tablas automáticamente

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    account_non_locked BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    last_login TIMESTAMP WITH TIME ZONE
);

-- Tabla de roles de usuario
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT false,
    user_agent VARCHAR(500),
    ip_address VARCHAR(45),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);

-- =====================================================
-- USUARIO ADMIN INICIAL
-- =====================================================
-- Contraseña: Admin123!
-- La contraseña está hasheada con BCrypt (strength 12)
-- IMPORTANTE: Cambiar esta contraseña después del primer login

INSERT INTO users (username, email, password, enabled, account_non_locked, created_at, updated_at)
VALUES (
    'admin',
    'admin@agendarreservas.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3oL/A7vZKG',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

-- Asignar rol ADMIN al usuario admin
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- También asignar rol USER
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- =====================================================
-- NOTAS IMPORTANTES:
-- =====================================================
-- 1. Este script es opcional si usas spring.jpa.hibernate.ddl-auto=update
-- 2. La contraseña del admin es: Admin123!
-- 3. CAMBIAR la contraseña del admin en producción inmediatamente
-- 4. Generar un nuevo JWT_SECRET para producción:
--    openssl rand -base64 64
-- =====================================================

