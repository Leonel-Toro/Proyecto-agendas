package com.Calendario.AgendarReservas.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Configuración para gestión segura del JWT Secret.
 *
 * Prioridad:
 * 1. Variable de entorno JWT_SECRET (recomendado para producción)
 * 2. Archivo .jwt-secret en el directorio de la aplicación
 * 3. Generación automática (solo desarrollo, con advertencia)
 */
@Configuration
public class JwtSecretConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretConfig.class);
    private static final String SECRET_FILE_NAME = ".jwt-secret";
    private static final int SECRET_BYTES = 64; // 512 bits para HS512

    private final Environment environment;

    @Value("${app.jwt.secret:}")
    private String configuredSecret;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    public JwtSecretConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateJwtSecret() {
        boolean isProd = isProductionProfile();

        // Verificar si hay variable de entorno
        String envSecret = System.getenv("JWT_SECRET");

        if (envSecret != null && !envSecret.isBlank()) {
            // ✅ Usando variable de entorno (ideal)
            logger.info("✅ JWT_SECRET configurado via variable de entorno");
            validateSecretStrength(envSecret, isProd);
            return;
        }

        if (configuredSecret != null && !configuredSecret.isBlank() && !isDefaultSecret(configuredSecret)) {
            // ⚠️ Usando secret del archivo de propiedades
            logger.warn("⚠️ JWT_SECRET configurado en archivo de propiedades. " +
                       "En producción, use variables de entorno.");
            validateSecretStrength(configuredSecret, isProd);
            return;
        }

        // 🔴 No hay secret configurado
        if (isProd) {
            logger.error("🔴 CRÍTICO: No se ha configurado JWT_SECRET para producción!");
            logger.error("   Configure la variable de entorno JWT_SECRET con una clave segura de 64 bytes en Base64");
            logger.error("   Genere una con: openssl rand -base64 64");
            throw new IllegalStateException(
                "JWT_SECRET no configurado. En producción DEBE configurar esta variable de entorno."
            );
        } else {
            // En desarrollo, generar o cargar del archivo
            handleDevelopmentSecret();
        }
    }

    private void handleDevelopmentSecret() {
        Path secretPath = Paths.get(SECRET_FILE_NAME);

        if (Files.exists(secretPath)) {
            logger.info("📁 Usando JWT_SECRET del archivo local: {}", secretPath.toAbsolutePath());
        } else {
            // Generar nuevo secret
            String newSecret = generateSecureSecret();
            try {
                Files.writeString(secretPath, newSecret);
                logger.warn("🔑 Generado nuevo JWT_SECRET y guardado en: {}", secretPath.toAbsolutePath());
                logger.warn("   Este archivo está excluido de Git (.gitignore)");
                logger.warn("   Para producción, configure la variable de entorno JWT_SECRET");

                // Actualizar .gitignore si es necesario
                ensureGitIgnore();

            } catch (IOException e) {
                logger.warn("⚠️ No se pudo guardar JWT_SECRET en archivo: {}", e.getMessage());
                logger.warn("   El secret se generará en cada reinicio");
            }
        }
    }

    private void ensureGitIgnore() {
        Path gitignorePath = Paths.get(".gitignore");
        try {
            String content = "";
            if (Files.exists(gitignorePath)) {
                content = Files.readString(gitignorePath);
            }

            if (!content.contains(SECRET_FILE_NAME)) {
                String newContent = content + "\n# JWT Secret (auto-generated for development)\n" + SECRET_FILE_NAME + "\n";
                Files.writeString(gitignorePath, newContent);
                logger.info("📝 Agregado {} a .gitignore", SECRET_FILE_NAME);
            }
        } catch (IOException e) {
            logger.warn("No se pudo actualizar .gitignore: {}", e.getMessage());
        }
    }

    private String generateSecureSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }

    private void validateSecretStrength(String secret, boolean isProd) {
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);

            if (decoded.length < 32) {
                String msg = "JWT_SECRET muy débil: {} bytes. Mínimo recomendado: 64 bytes";
                if (isProd) {
                    logger.error("🔴 " + msg, decoded.length);
                    throw new IllegalStateException("JWT_SECRET demasiado débil para producción");
                } else {
                    logger.warn("⚠️ " + msg, decoded.length);
                }
            } else if (decoded.length < 64) {
                logger.warn("⚠️ JWT_SECRET tiene {} bytes. Recomendado: 64 bytes para HS512", decoded.length);
            } else {
                logger.info("✅ JWT_SECRET tiene fortaleza adecuada ({} bytes)", decoded.length);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ JWT_SECRET no está en formato Base64 válido");
        }
    }

    private boolean isDefaultSecret(String secret) {
        // Detectar secrets por defecto que no deben usarse en producción
        return secret.startsWith("dGhpc2lzYXZlcnk") || // El default del código
               secret.startsWith("bG9jYWxkZXZlbG9w") || // El de desarrollo
               secret.length() < 32;
    }

    private boolean isProductionProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod") ||
               "prod".equals(activeProfile) ||
               "production".equals(activeProfile);
    }

    /**
     * Método utilitario para obtener el secret (usado por JwtService)
     */
    public String getEffectiveSecret() {
        // Prioridad: Env > Config > File > Error
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            return envSecret;
        }

        if (configuredSecret != null && !configuredSecret.isBlank()) {
            return configuredSecret;
        }

        // Intentar leer del archivo
        Path secretPath = Paths.get(SECRET_FILE_NAME);
        if (Files.exists(secretPath)) {
            try {
                return Files.readString(secretPath).trim();
            } catch (IOException e) {
                logger.error("Error leyendo JWT_SECRET del archivo", e);
            }
        }

        throw new IllegalStateException("No se pudo obtener JWT_SECRET");
    }
}

