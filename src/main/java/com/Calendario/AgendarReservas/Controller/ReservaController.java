package com.Calendario.AgendarReservas.Controller;

import com.Calendario.AgendarReservas.DTO.ReservaClienteDTO;
import com.Calendario.AgendarReservas.Model.MedioContacto;
import com.Calendario.AgendarReservas.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas/")
@CrossOrigin(origins = "http://localhost:5173")
public class ReservaController {
    @Autowired
    private ReservaService reservaService;

    @PostMapping("/agendar")
    public ResponseEntity<?> agendarCliente(@RequestBody ReservaClienteDTO reservar){
        boolean exito = reservaService.agendarCliente(reservar);
        if(exito){
            return ResponseEntity.status(HttpStatus.OK).body("");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");

    }

    @GetMapping("/historial")
    public ResponseEntity<List<ReservaClienteDTO>> obtenerHistorial() {
        List<ReservaClienteDTO> historial = reservaService.obtenerHistorial();
        return ResponseEntity.ok(historial);
    }
}
