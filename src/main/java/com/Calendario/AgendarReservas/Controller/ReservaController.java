package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.ReservaClienteDTO;
import com.calendario.agendarreservas.model.ResponseApi;
import com.calendario.agendarreservas.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping("/agendar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> agendarCliente(@RequestBody ReservaClienteDTO reservar) {
        ReservaClienteDTO resultado = reservaService.agendarCliente(reservar);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseApi<>(201, "Reserva creada exitosamente", resultado));
    }

    @GetMapping("/historial/detalle/{id}")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> obtenerDetalleReserva(@PathVariable Long id) {
        ReservaClienteDTO detalle = reservaService.obtenerDetalleReserva(id);
        return ResponseEntity.ok(new ResponseApi<>(200, "Detalle de reserva obtenido", detalle));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorial() {
        List<ReservaClienteDTO> historial = reservaService.obtenerHistorial();
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial obtenido", historial));
    }

    @PutMapping("/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> editarReserva(@RequestBody ReservaClienteDTO reservaClienteDTO) {
        ReservaClienteDTO resultado = reservaService.editarReserva(reservaClienteDTO);
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva editada exitosamente", resultado));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ResponseApi<Void>> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminarReserva(id);
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva eliminada exitosamente", null));
    }

    @GetMapping("/admin/historial/usuario/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorialPorUsuario(@PathVariable Long userId) {
        List<ReservaClienteDTO> historial = reservaService.obtenerHistorialPorUsuario(userId);
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial del usuario obtenido", historial));
    }

    @GetMapping("/admin/historial/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorialPorEmail(@PathVariable String email) {
        List<ReservaClienteDTO> historial = reservaService.obtenerHistorialPorEmail(email);
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial del usuario obtenido", historial));
    }

    @GetMapping("/admin/detalle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> obtenerDetalleReservaAdmin(@PathVariable Long id) {
        ReservaClienteDTO detalle = reservaService.obtenerDetalleReservaAdmin(id);
        return ResponseEntity.ok(new ResponseApi<>(200, "Detalle de reserva obtenido", detalle));
    }

    @PutMapping("/admin/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> editarReservaAdmin(@RequestBody ReservaClienteDTO reservaClienteDTO) {
        ReservaClienteDTO resultado = reservaService.editarReservaAdmin(reservaClienteDTO);
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva editada exitosamente", resultado));
    }
}
