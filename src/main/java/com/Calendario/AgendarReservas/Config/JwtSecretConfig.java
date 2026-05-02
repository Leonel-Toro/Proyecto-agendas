package com.calendario.agendarreservas.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtSecretConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretConfig.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            logger.error("JWT_SECRET no está configurado!");
            throw new IllegalStateException("JWT_SECRET es requerido. Configure la variable de entorno.");
        }
        logger.info("JWT_SECRET configurado correctamente");
    }

    public String getSecret() {
        return jwtSecret;
    }
}
