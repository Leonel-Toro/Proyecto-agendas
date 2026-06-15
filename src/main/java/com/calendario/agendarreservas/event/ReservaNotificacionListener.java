package com.calendario.agendarreservas.event;

import com.calendario.agendarreservas.model.Reserva;
import com.calendario.agendarreservas.repository.ReservaRepository;
import com.calendario.agendarreservas.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Escucha los cambios de estado de reserva y dispara las notificaciones por correo.
 * Se ejecuta de forma asincrona y solo despues de que la transaccion de negocio confirma,
 * de modo que un fallo de correo nunca afecta la reserva.
 */
@Component
@RequiredArgsConstructor
public class ReservaNotificacionListener {

    private static final Logger logger = LoggerFactory.getLogger(ReservaNotificacionListener.class);

    private final ReservaRepository reservaRepository;
    private final EmailService emailService;

    @Async("mailTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCambioEstado(ReservaEstadoEvent event) {
        Reserva reserva = reservaRepository.findByIdConPersonas(event.reservaId()).orElse(null);
        if (reserva == null) {
            logger.warn("No se encontro la reserva id={} para notificar el cambio de estado", event.reservaId());
            return;
        }
        emailService.enviarCambioEstado(reserva, event.esCreacion());
    }
}
