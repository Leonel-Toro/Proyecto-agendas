package com.Calendario.AgendarReservas.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuración para JWT Secret.
 *
 * El JWT_SECRET DEBE configurarse manualmente:
 * - En desarrollo: en application-local.properties
 * - En producción: como variable de entorno en Railway
 *
 * Para generar un secret seguro:
 *   openssl rand -base64 64
 */
@Configuration
public class JwtSecretConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretConfig.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            logger.error("🔴 JWT_SECRET no está configurado!");
            logger.error("   En desarrollo: configure app.jwt.secret en application-local.properties");
            logger.error("   En producción: configure la variable de entorno JWT_SECRET en Railway");
            throw new IllegalStateException("JWT_SECRET es requerido. Configure la variable de entorno.");
        }

        logger.info("JWT_SECRET configurado correctamente");
    }

    public String getSecret() {
        return jwtSecret;
    }
}

