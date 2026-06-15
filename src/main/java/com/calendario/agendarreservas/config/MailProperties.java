package com.calendario.agendarreservas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de la aplicacion para el envio de correos (prefijo {@code app.mail}).
 * Las credenciales SMTP en si viven bajo {@code spring.mail.*}.
 */
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    /** Activa o desactiva globalmente el envio de correos. */
    private boolean enabled = true;

    /** Direccion remitente (From). */
    private String from = "noreply@agendarreservas.com";

    /** Nombre visible del remitente. */
    private String fromName = "AgendarReservas";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
