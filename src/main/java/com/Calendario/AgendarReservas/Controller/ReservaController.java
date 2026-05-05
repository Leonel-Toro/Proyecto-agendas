package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.ReservaDTO;
import com.calendario.agendarreservas.model.ResponseApi;
import com.calendario.agendarreservas.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    // ===================== Paciente =====================

    @PostMapping("/api/reservas")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<ReservaDTO>> crearReserva(@Valid @RequestBody ReservaDTO dto) {
        ReservaDTO result = reservaService.crearReserva(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseApi<>(201, "Reserva creada exitosamente", result));
    }

    @GetMapping("/api/reservas")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<List<ReservaDTO>>> obtenerMisReservas() {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reservas obtenidas", reservaService.obtenerMisReservas()));
    }

    @GetMapping("/api/reservas/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<ReservaDTO>> obtenerMiReserva(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva obtenida", reservaService.obtenerMiReserva(id)));
    }

    @PatchMapping("/api/reservas/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<ReservaDTO>> editarReserva(
            @PathVariable Long id, @RequestBody ReservaDTO dto) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva actualizada", reservaService.editarReserva(id, dto)));
    }

    @DeleteMapping("/api/reservas/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<Void>> cancelarReserva(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva cancelada", null));
    }

    // ===================== Admin =====================

    @PostMapping("/api/admin/reservas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaDTO>> crearReservaAdmin(@Valid @RequestBody ReservaDTO dto) {
        ReservaDTO result = reservaService.crearReservaAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseApi<>(201, "Reserva creada exitosamente", result));
    }

    @GetMapping("/api/admin/reservas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaDTO>>> obtenerTodasReservas() {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reservas obtenidas", reservaService.obtenerTodasReservas()));
    }

    @GetMapping("/api/admin/reservas/paciente/{pacienteId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ReservaDTO>>> obtenerReservasPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reservas del paciente obtenidas",
                reservaService.obtenerReservasPorPaciente(pacienteId)));
    }

    @GetMapping("/api/admin/reservas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaDTO>> obtenerReservaAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva obtenida", reservaService.obtenerReservaAdmin(id)));
    }

    @PutMapping("/api/admin/reservas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaDTO>> editarReservaAdmin(
            @PathVariable Long id, @RequestBody ReservaDTO dto) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva actualizada", reservaService.editarReservaAdmin(id, dto)));
    }

    @DeleteMapping("/api/admin/reservas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<Void>> cancelarReservaAdmin(@PathVariable Long id) {
        reservaService.cancelarReservaAdmin(id);
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva cancelada", null));
    }

    @PatchMapping("/api/admin/reservas/{id}/completar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ReservaDTO>> completarReserva(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Reserva completada", reservaService.completarReserva(id)));
    }
}
