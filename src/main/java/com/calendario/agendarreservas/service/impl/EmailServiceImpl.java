package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.config.MailProperties;
import com.calendario.agendarreservas.model.EstadoReserva;
import com.calendario.agendarreservas.model.Reserva;
import com.calendario.agendarreservas.model.User;
import com.calendario.agendarreservas.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String TELEFONO_FALLBACK = "No disponible";

    private static final String TEMPLATE_PACIENTE = "emails/reserva-paciente";
    private static final String TEMPLATE_PSICOLOGO = "emails/reserva-psicologo";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final MailProperties mailProperties;

    @Override
    public void enviarCambioEstado(Reserva reserva, boolean esCreacion) {
        Long reservaId = reserva.getIdReserva();
        if (!mailProperties.isEnabled()) {
            logger.debug("Notificaciones deshabilitadas (app.mail.enabled=false); se omite reserva id={}", reservaId);
            return;
        }

        logger.info("Preparando notificaciones reserva id={} estado={} creacion={}",
                reservaId, reserva.getEstado(), esCreacion);

        Map<String, Object> modelo = construirModelo(reserva, esCreacion);
        String asunto = construirAsunto(reserva.getEstado(), esCreacion);

        User paciente = reserva.getPaciente();
        User psicologo = reserva.getPsicologo();

        enviarA(paciente, "paciente", asunto, TEMPLATE_PACIENTE, modelo, reservaId);
        enviarA(psicologo, "psicologo", asunto, TEMPLATE_PSICOLOGO, modelo, reservaId);
    }

    /** Valida el destinatario y delega el envio; nunca registra el correo, solo el rol. */
    private void enviarA(User destinatario, String rol, String asunto, String plantilla,
                         Map<String, Object> modelo, Long reservaId) {
        if (destinatario == null || destinatario.getEmail() == null || destinatario.getEmail().isBlank()) {
            logger.warn("Reserva id={} sin email para rol={}; se omite el envio", reservaId, rol);
            return;
        }
        enviar(destinatario.getEmail(), rol, asunto, plantilla, modelo, reservaId, true);
    }

    private void enviar(String destinatario, String rol, String asunto, String plantilla,
                        Map<String, Object> modelo, Long reservaId, boolean conNombreRemitente) {
        try {
            Context context = new Context();
            context.setVariables(modelo);
            String html = templateEngine.process(plantilla, context);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            if (conNombreRemitente) {
                helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            } else {
                helper.setFrom(mailProperties.getFrom());
            }
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);

            mailSender.send(mensaje);
            logger.info("Correo enviado reserva id={} rol={} plantilla={}", reservaId, rol, plantilla);
        } catch (UnsupportedEncodingException e) {
            // from-name no codificable: reintentar sin nombre amigable
            logger.warn("Nombre de remitente no codificable; reintentando sin nombre. reserva id={} rol={}", reservaId, rol);
            enviar(destinatario, rol, asunto, plantilla, modelo, reservaId, false);
        } catch (Exception e) {
            logger.error("Fallo al enviar correo reserva id={} rol={} plantilla={}: {} -> {}",
                    reservaId, rol, plantilla, e.getClass().getSimpleName(), causaRaiz(e));
            // El stacktrace puede contener el correo del destinatario: solo en DEBUG.
            logger.debug("Detalle del fallo de envio reserva id={} rol={}", reservaId, rol, e);
        }
    }

    
    private String causaRaiz(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }

    private Map<String, Object> construirModelo(Reserva reserva, boolean esCreacion) {
        User paciente = reserva.getPaciente();
        User psicologo = reserva.getPsicologo();

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("pacienteNombre", nombreCompleto(paciente));
        modelo.put("psicologoNombre", nombreCompleto(psicologo));
        modelo.put("psicologoTelefono", telefonoOFallback(psicologo));
        modelo.put("fecha", reserva.getFechaReserva() != null
                ? FECHA_FORMAT.format(reserva.getFechaReserva().toLocalDateTime()) : "-");
        modelo.put("modalidad", reserva.getModalidad() != null ? reserva.getModalidad().getLabel() : "-");
        modelo.put("motivo", reserva.getMotivoConsulta() != null && !reserva.getMotivoConsulta().isBlank()
                ? reserva.getMotivoConsulta() : "Sin especificar");
        modelo.put("estadoLabel", reserva.getEstado() != null ? reserva.getEstado().getLabel() : "-");
        modelo.put("esCreacion", esCreacion);
        // Datos de gestion para la plantilla del psicologo
        modelo.put("precio", reserva.getPrecio() != null ? reserva.getPrecio() : 0L);
        modelo.put("abonado", reserva.getAbonado() != null ? reserva.getAbonado() : 0L);
        modelo.put("pacienteRut", paciente != null && paciente.getRut() != null ? paciente.getRut() : "-");
        return modelo;
    }

    private String construirAsunto(EstadoReserva estado, boolean esCreacion) {
        if (esCreacion) {
            return "Reserva registrada - Pendiente de confirmacion";
        }
        if (estado == null) {
            return "Actualizacion de tu reserva";
        }
        return switch (estado) {
            case CONFIRMADA -> "Reserva confirmada";
            case CANCELADA -> "Reserva cancelada";
            case COMPLETADA -> "Reserva completada";
            case PENDIENTE -> "Reserva pendiente de confirmacion";
        };
    }

    private String nombreCompleto(User user) {
        if (user == null) return "-";
        StringBuilder sb = new StringBuilder();
        if (user.getNombre() != null && !user.getNombre().isBlank()) sb.append(user.getNombre().trim());
        if (user.getApellidos() != null && !user.getApellidos().isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(user.getApellidos().trim());
        }
        return sb.length() > 0 ? sb.toString() : user.getUsername();
    }

    private String telefonoOFallback(User user) {
        if (user != null && user.getTelefono() != null && !user.getTelefono().isBlank()) {
            return user.getTelefono().trim();
        }
        return TELEFONO_FALLBACK;
    }
}
