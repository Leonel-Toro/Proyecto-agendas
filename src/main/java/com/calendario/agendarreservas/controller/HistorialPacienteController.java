package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.HistorialPacienteDTO;
import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.model.ResponseApi;
import com.calendario.agendarreservas.service.HistorialPacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class HistorialPacienteController {

    private final HistorialPacienteService historialPacienteService;

    // ===================== Paciente =====================

    @GetMapping("/api/historial")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<List<HistorialPacienteDTO>>> obtenerMiHistorialCompleto() {
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial obtenido",
                historialPacienteService.obtenerMiHistorialCompleto()));
    }

    @GetMapping("/api/historial/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<HistorialPacienteDTO>> obtenerMiHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial obtenido",
                historialPacienteService.obtenerMiHistorial(id)));
    }

    // ===================== Admin =====================

    @PostMapping("/api/admin/historial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<HistorialPacienteDTO>> crearHistorial(
            @Valid @RequestBody HistorialPacienteDTO dto) {
        HistorialPacienteDTO result = historialPacienteService.crearHistorial(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseApi<>(201, "Historial creado exitosamente", result));
    }

    @PutMapping("/api/admin/historial/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<HistorialPacienteDTO>> actualizarHistorial(
            @PathVariable Long id, @RequestBody HistorialPacienteDTO dto) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial actualizado",
                historialPacienteService.actualizarHistorial(id, dto)));
    }

    @GetMapping("/api/admin/historial/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<HistorialPacienteDTO>> obtenerHistorialAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial obtenido",
                historialPacienteService.obtenerHistorialAdmin(id)));
    }

    @GetMapping("/api/admin/historial/paciente/{pacienteId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<HistorialPacienteDTO>>> obtenerHistorialPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial del paciente obtenido",
                historialPacienteService.obtenerHistorialPorPaciente(pacienteId)));
    }

    @GetMapping("/api/admin/historial/paciente/{pacienteId}/rango")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<HistorialPacienteDTO>>> obtenerHistorialPorRango(
            @PathVariable Long pacienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Historial por rango obtenido",
                historialPacienteService.obtenerHistorialPorPacienteYRango(pacienteId, desde, hasta)));
    }
}
