package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.UserDTO;
import com.calendario.agendarreservas.model.ResponseApi;
import com.calendario.agendarreservas.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/psicologos")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseApi<List<UserDTO>>> obtenerPsicologos() {
        return ResponseEntity.ok(new ResponseApi<>(200, "Psicólogos obtenidos", userService.obtenerPsicologos()));
    }

    @GetMapping("/api/admin/pacientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<UserDTO>>> obtenerPacientes() {
        return ResponseEntity.ok(new ResponseApi<>(200, "Pacientes obtenidos", userService.obtenerPacientes()));
    }
}
