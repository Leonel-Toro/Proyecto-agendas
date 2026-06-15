package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.config.MailProperties;
import com.calendario.agendarreservas.model.EstadoReserva;
import com.calendario.agendarreservas.model.Modalidad;
import com.calendario.agendarreservas.model.Reserva;
import com.calendario.agendarreservas.model.User;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private SpringTemplateEngine templateEngine;
    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        User paciente = new User("juanp", "paciente@test.com", "password123");
        paciente.setNombre("Juan");
        paciente.setApellidos("Perez");
        paciente.setRut("11111111-1");

        User psicologo = new User("dra.ana", "psico@test.com", "password123");
        psicologo.setNombre("Ana");
        psicologo.setApellidos("Soto");
        psicologo.setTelefono("+56 9 1234 5678");

        reserva = new Reserva();
        reserva.setIdReserva(42L);
        reserva.setPaciente(paciente);
        reserva.setPsicologo(psicologo);
        reserva.setFechaReserva(Timestamp.valueOf(LocalDateTime.of(2026, 7, 1, 10, 0)));
        reserva.setModalidad(Modalidad.PRESENCIAL);
        reserva.setMotivoConsulta("Ansiedad");
        reserva.setPrecio(30000L);
        reserva.setAbonado(10000L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
    }

    @Test
    void noEnviaCuandoMailDeshabilitado() {
        when(mailProperties.isEnabled()).thenReturn(false);

        emailService.enviarCambioEstado(reserva, false);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviaAPacienteYPsicologoConTelefonoEnElModelo() {
        when(mailProperties.isEnabled()).thenReturn(true);
        when(mailProperties.getFrom()).thenReturn("noreply@test.com");
        when(mailProperties.getFromName()).thenReturn("AgendarReservas");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>cuerpo</html>");

        emailService.enviarCambioEstado(reserva, false);

        // Dos correos: paciente + psicologo
        verify(mailSender, times(2)).send(any(MimeMessage.class));

        // Ambas plantillas se renderizan con el telefono del psicologo en el modelo
        verify(templateEngine, times(2)).process(anyString(), contextCaptor.capture());
        contextCaptor.getAllValues().forEach(ctx ->
                assertThat(ctx.getVariable("psicologoTelefono")).isEqualTo("+56 9 1234 5678"));
    }

    @Test
    void usaPlantillasDistintasPorDestinatario() {
        when(mailProperties.isEnabled()).thenReturn(true);
        when(mailProperties.getFrom()).thenReturn("noreply@test.com");
        when(mailProperties.getFromName()).thenReturn("AgendarReservas");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>cuerpo</html>");

        emailService.enviarCambioEstado(reserva, false);

        ArgumentCaptor<String> plantillaCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateEngine, times(2)).process(plantillaCaptor.capture(), any(Context.class));
        assertThat(plantillaCaptor.getAllValues())
                .containsExactlyInAnyOrder("emails/reserva-paciente", "emails/reserva-psicologo");
    }
}
