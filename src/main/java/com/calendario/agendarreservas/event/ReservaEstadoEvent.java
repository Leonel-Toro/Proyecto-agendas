package com.calendario.agendarreservas.event;

import com.calendario.agendarreservas.model.EstadoReserva;

/**
 * Evento publicado cuando una reserva se crea o cambia de estado.
 * Se transporta solo el id (no la entidad) para evitar problemas de lazy-loading
 * fuera de la sesion de persistencia; el listener recarga la reserva tras el commit.
 *
 * @param reservaId   id de la reserva afectada
 * @param nuevoEstado estado resultante de la reserva
 * @param esCreacion  {@code true} si el evento corresponde a la creacion de la reserva
 */
public record ReservaEstadoEvent(Long reservaId, EstadoReserva nuevoEstado, boolean esCreacion) {
}
