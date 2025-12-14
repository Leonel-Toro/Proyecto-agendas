package com.Calendario.AgendarReservas.Controller;

import com.Calendario.AgendarReservas.DTO.ReservaClienteDTO;
import com.Calendario.AgendarReservas.Model.ResponseApi;
import com.Calendario.AgendarReservas.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas/")
public class ReservaController {
    @Autowired
    private ReservaService reservaService;

    @PostMapping("/agendar")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> agendarCliente(@RequestBody ReservaClienteDTO reservar){
        try {
            ReservaClienteDTO resultado = reservaService.agendarCliente(reservar);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseApi<>(201, "Reserva creada exitosamente", resultado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseApi<>(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al crear la reserva: " + e.getMessage()));
        }

    }
    @GetMapping("/historial/detalle/{id}")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> obtenerDetalleReserva(@PathVariable Long id) {
        try {
            ReservaClienteDTO reservaDetalle = reservaService.obtenerDetalleReserva(id);
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

    @GetMapping("/historial")
    public ResponseEntity<ResponseApi<List<ReservaClienteDTO>>> obtenerHistorial() {
        try {
            List<ReservaClienteDTO> historial = reservaService.obtenerHistorial();
            return ResponseEntity.ok(new ResponseApi<>(200, "Historial obtenido", historial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseApi<>(500, "Error al obtener el historial: " + e.getMessage()));
        }
    }

    @PutMapping("/editar")
    public ResponseEntity<ResponseApi<ReservaClienteDTO>> editarReserva(@RequestBody ReservaClienteDTO reservaClienteDTO){
        try {
            ReservaClienteDTO resultado = reservaService.editarReserva(reservaClienteDTO);
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
