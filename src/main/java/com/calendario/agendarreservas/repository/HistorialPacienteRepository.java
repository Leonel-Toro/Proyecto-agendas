package com.calendario.agendarreservas.repository;

import com.calendario.agendarreservas.model.EstadoReserva;
import com.calendario.agendarreservas.model.HistorialPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialPacienteRepository extends JpaRepository<HistorialPaciente, Long> {

    List<HistorialPaciente> findByPacienteIdOrderByFechaCreacionDesc(Long pacienteId);

    List<HistorialPaciente> findByPacienteIdAndPsicologoIdOrderByFechaCreacionDesc(Long pacienteId, Long psicologoId);

    @Query("SELECT h FROM HistorialPaciente h WHERE h.paciente.id = :pacienteId AND h.psicologo.id = :psicologoId AND h.fechaCreacion BETWEEN :desde AND :hasta ORDER BY h.fechaCreacion DESC")
    List<HistorialPaciente> findByPacienteIdAndPsicologoIdAndRango(
            @Param("pacienteId") Long pacienteId,
            @Param("psicologoId") Long psicologoId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    Optional<HistorialPaciente> findByReservaIdReserva(Long idReserva);

    Optional<HistorialPaciente> findByIdHistorialAndReservaEstado(Long idHistorial, EstadoReserva estado);

    boolean existsByReservaIdReserva(Long idReserva);

    @Query("SELECT h FROM HistorialPaciente h WHERE h.paciente.id = :pacienteId AND h.fechaCreacion BETWEEN :desde AND :hasta ORDER BY h.fechaCreacion DESC")
    List<HistorialPaciente> findByPacienteIdAndRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);
}
