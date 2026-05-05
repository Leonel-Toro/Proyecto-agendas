package com.calendario.agendarreservas.repository;

import com.calendario.agendarreservas.model.NotasSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotasSesionRepository extends JpaRepository<NotasSesion, Long> {

    List<NotasSesion> findByHistorialPacienteIdHistorialOrderByFechaCreacionAsc(Long idHistorial);
}
