package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.ReservaDTO;
import com.calendario.agendarreservas.event.ReservaEstadoEvent;
import com.calendario.agendarreservas.mapper.ReservaMapper;
import com.calendario.agendarreservas.model.EstadoReserva;
import com.calendario.agendarreservas.model.Modalidad;
import com.calendario.agendarreservas.model.Reserva;
import com.calendario.agendarreservas.model.User;
import com.calendario.agendarreservas.repository.ReservaRepository;
import com.calendario.agendarreservas.repository.UserRepository;
import com.calendario.agendarreservas.util.SecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplEventTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContextHelper securityContextHelper;
    @Mock
    private ReservaMapper reservaMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        User paciente = new User("juanp", "paciente@test.com", "password123");
        User psicologo = new User("dra.ana", "psico@test.com", "password123");

        reserva = new Reserva();
        reserva.setIdReserva(7L);
        reserva.setPaciente(paciente);
        reserva.setPsicologo(psicologo);
        reserva.setModalidad(Modalidad.PRESENCIAL);
        reserva.setDuracionMinutos(60);
        reserva.setFechaReserva(Timestamp.valueOf(LocalDateTime.of(2026, 7, 1, 10, 0)));
        reserva.setFechaTermino(Timestamp.valueOf(LocalDateTime.of(2026, 7, 1, 11, 0)));
        reserva.setPrecio(30000L);
        reserva.setAbonado(0L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
    }

    @Test
    void completarReservaPublicaEventoCompletada() {
        when(reservaRepository.findById(7L)).thenReturn(Optional.of(reserva));

        reservaService.completarReserva(7L);

        ArgumentCaptor<ReservaEstadoEvent> captor = ArgumentCaptor.forClass(ReservaEstadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().nuevoEstado()).isEqualTo(EstadoReserva.COMPLETADA);
        assertThat(captor.getValue().reservaId()).isEqualTo(7L);
        assertThat(captor.getValue().esCreacion()).isFalse();
    }

    @Test
    void cancelarReservaAdminPublicaEventoCancelada() {
        when(reservaRepository.findById(7L)).thenReturn(Optional.of(reserva));

        reservaService.cancelarReservaAdmin(7L);

        ArgumentCaptor<ReservaEstadoEvent> captor = ArgumentCaptor.forClass(ReservaEstadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().nuevoEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void editarReservaAdminSinCambioDeEstadoNoPublicaEvento() {
        when(reservaRepository.findById(7L)).thenReturn(Optional.of(reserva));
        sinSolapamientos();

        ReservaDTO dto = new ReservaDTO();
        dto.setMotivoConsulta("Actualizado");
        // mismo estado actual -> no debe notificar
        dto.setEstado("CONFIRMADA");

        reservaService.editarReservaAdmin(7L, dto);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void editarReservaAdminConCambioDeEstadoPublicaEvento() {
        when(reservaRepository.findById(7L)).thenReturn(Optional.of(reserva));
        sinSolapamientos();

        ReservaDTO dto = new ReservaDTO();
        dto.setEstado("CANCELADA");

        reservaService.editarReservaAdmin(7L, dto);

        ArgumentCaptor<ReservaEstadoEvent> captor = ArgumentCaptor.forClass(ReservaEstadoEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().nuevoEstado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(captor.getValue().esCreacion()).isFalse();
    }

    private void sinSolapamientos() {
        lenient().when(reservaRepository.findSolapamientosPsicologo(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(reservaRepository.findSolapamientosPaciente(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
    }
}
