package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.model.ResponseApi;
import com.calendario.agendarreservas.service.NotasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notas")
public class NotasController {

    private final NotasService notasService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<NotasSesionDTO>> crearNota(@Valid @RequestBody NotasSesionDTO dto) {
        NotasSesionDTO result = notasService.crearNota(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseApi<>(201, "Nota creada exitosamente", result));
    }

    @GetMapping("/reserva/{idReserva}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<NotasSesionDTO>>> listarNotas(@PathVariable Long idReserva) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Notas obtenidas",
                notasService.listarNotasPorReserva(idReserva)));
    }

    @PatchMapping("/{idNota}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<NotasSesionDTO>> modificarNota(
            @PathVariable Long idNota, @Valid @RequestBody NotasSesionDTO dto) {
        return ResponseEntity.ok(new ResponseApi<>(200, "Nota actualizada",
                notasService.modificarNota(idNota, dto)));
    }

    @DeleteMapping("/{idNota}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<Void>> eliminarNota(@PathVariable Long idNota) {
        notasService.eliminarNota(idNota);
        return ResponseEntity.ok(new ResponseApi<>(200, "Nota eliminada"));
    }
}
