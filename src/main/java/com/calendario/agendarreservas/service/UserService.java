package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.UserDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> obtenerPsicologos();

    List<UserDTO> obtenerPacientes();
}
