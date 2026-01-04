package com.Calendario.AgendarReservas.Controller;

import com.Calendario.AgendarReservas.DTO.ReservaClienteDTO;
import com.Calendario.AgendarReservas.Model.ResponseApi;
import com.Calendario.AgendarReservas.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    /**
     * Crear nueva reserva - Solo ADMIN
     * La reserva se asigna automáticamente al usuario autenticado
     */
    @PostMapping("/agendar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> agendarCliente(@RequestBody ReservaClienteDTO reservar) {
        try {
            ReservaClienteDTO resultado = reservaService.agendarCliente(reservar);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseApi<>(201, "Reserva creada exitosamente", resultado));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseApi<>(401, "Debe iniciar sesión para crear reservas"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseApi<>(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al crear la reserva: " + e.getMessage()));
        }
    }

    /**
     * Obtener detalle de una reserva del usuario autenticado
     */
    @GetMapping("/historial/detalle/{id}")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> obtenerDetalleReserva(@PathVariable Long id) {
        try {
            ReservaClienteDTO reservaDetalle = reservaService.obtenerDetalleReserva(id);
            if (reservaDetalle != null) {
                return ResponseEntity.ok(new ResponseApi<>(200, "Detalle de reserva obtenido", reservaDetalle));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseApi<>(404, "Reserva no encontrada o no tiene acceso"));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseApi<>(401, "Debe iniciar sesión"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al obtener el detalle: " + e.getMessage()));
        }
    }

    /**
     * Obtener historial de reservas - Solo ADMIN
     */
    @GetMapping("/historial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorial() {
        try {
            List<ReservaClienteDTO> historial = reservaService.obtenerHistorial();
            return ResponseEntity.ok(new ResponseApi<>(200, "Historial obtenido", historial));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseApi<>(401, "Debe iniciar sesión"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al obtener el historial: " + e.getMessage()));
        }
    }

    /**
     * Editar una reserva - Solo ADMIN
     */
    @PutMapping("/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> editarReserva(@RequestBody ReservaClienteDTO reservaClienteDTO) {
        try {
            ReservaClienteDTO resultado = reservaService.editarReserva(reservaClienteDTO);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseApi<>(200, "Reserva editada exitosamente", resultado));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseApi<>(401, "Debe iniciar sesión"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseApi<>(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al editar la reserva: " + e.getMessage()));
        }
    }

    /**
     * Eliminar una reserva del usuario autenticado
     */
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ResponseApi<Void>> eliminarReserva(@PathVariable Long id) {
        try {
            reservaService.eliminarReserva(id);
            return ResponseEntity.ok(new ResponseApi<>(200, "Reserva eliminada exitosamente", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseApi<>(401, "Debe iniciar sesión"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseApi<>(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al eliminar la reserva: " + e.getMessage()));
        }
    }

    // ==================== ENDPOINTS ADMIN ====================

    /**
     * Obtener historial de reservas de un usuario específico por ID (ADMIN)
     */
    @GetMapping("/admin/historial/usuario/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorialPorUsuario(@PathVariable Long userId) {
        try {
            List<ReservaClienteDTO> historial = reservaService.obtenerHistorialPorUsuario(userId);
            return ResponseEntity.ok(new ResponseApi<>(200, "Historial del usuario obtenido", historial));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseApi<>(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al obtener el historial: " + e.getMessage()));
        }
    }

    /**
     * Obtener historial de reservas de un usuario específico por email (ADMIN)
     */
    @GetMapping("/admin/historial/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorialPorEmail(@PathVariable String email) {
        try {
            List<ReservaClienteDTO> historial = reservaService.obtenerHistorialPorEmail(email);
            return ResponseEntity.ok(new ResponseApi<>(200, "Historial del usuario obtenido", historial));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseApi<>(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al obtener el historial: " + e.getMessage()));
        }
    }

    /**
     * Obtener detalle de cualquier reserva (ADMIN)
     */
    @GetMapping("/admin/detalle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> obtenerDetalleReservaAdmin(@PathVariable Long id) {
        try {
            ReservaClienteDTO reservaDetalle = reservaService.obtenerDetalleReservaAdmin(id);
            if (reservaDetalle != null) {
                return ResponseEntity.ok(new ResponseApi<>(200, "Detalle de reserva obtenido", reservaDetalle));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseApi<>(404, "Reserva no encontrada"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al obtener el detalle: " + e.getMessage()));
        }
    }

    /**
     * Editar cualquier reserva (ADMIN)
     */
    @PutMapping("/admin/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> editarReservaAdmin(@RequestBody ReservaClienteDTO reservaClienteDTO) {
        try {
            ReservaClienteDTO resultado = reservaService.editarReservaAdmin(reservaClienteDTO);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseApi<>(200, "Reserva editada exitosamente", resultado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseApi<>(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al editar la reserva: " + e.getMessage()));
        }
    }
}

