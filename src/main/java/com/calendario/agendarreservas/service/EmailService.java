package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.model.Reserva;

public interface EmailService {

    /**
     * Envia las notificaciones de cambio de estado de una reserva al paciente y al psicologo,
     * cada uno con su plantilla correspondiente. El envio es best-effort: los fallos se registran
     * y no se propagan.
     *
     * @param reserva    reserva con paciente y psicologo cargados
     * @param esCreacion {@code true} si la notificacion corresponde a la creacion de la reserva
     */
    void enviarCambioEstado(Reserva reserva, boolean esCreacion);
}
