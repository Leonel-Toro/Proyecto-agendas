package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.UserDTO;
import com.calendario.agendarreservas.model.Role;
import com.calendario.agendarreservas.model.User;
import com.calendario.agendarreservas.repository.UserRepository;
import com.calendario.agendarreservas.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> obtenerPsicologos() {
        return userRepository.findByRole(Role.ROLE_ADMIN)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> obtenerPacientes() {
        return userRepository.findByRoleExcluding(Role.ROLE_USER, Role.ROLE_ADMIN)
                .stream().map(this::toDTO).toList();
    }

    private UserDTO toDTO(User u) {
        return new UserDTO(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getNombre(),
                u.getApellidos(),
                u.getRut(),
                u.getTelefono(),
                u.getFechaNacimiento(),
                u.getEdad(),
                u.getGenero() != null ? u.getGenero().name() : null,
                u.getPacienteAnterior(),
                u.getEstudiante(),
                u.isEnabled(),
                u.getCreatedAt(),
                u.getLastLogin()
        );
    }
}
