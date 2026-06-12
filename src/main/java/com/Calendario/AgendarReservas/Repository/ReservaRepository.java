package com.calendario.agendarreservas.repository;

import com.calendario.agendarreservas.model.EstadoReserva;
import com.calendario.agendarreservas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByPacienteIdOrderByFechaReservaDesc(Long pacienteId);

    Optional<Reserva> findByIdReservaAndPacienteId(Long idReserva, Long pacienteId);

    List<Reserva> findByPsicologoIdOrderByFechaReservaDesc(Long psicologoId);

    List<Reserva> findByPacienteIdAndEstadoOrderByFechaReservaDesc(Long pacienteId, EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.paciente.id = :pacienteId AND r.fechaReserva BETWEEN :desde AND :hasta ORDER BY r.fechaReserva DESC")
    List<Reserva> findByPacienteIdAndRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Timestamp desde,
            @Param("hasta") Timestamp hasta);

    @Query("SELECT r FROM Reserva r WHERE r.psicologo.id = :psicologoId " +
            "AND r.estado <> 'CANCELADA' " +
            "AND (:excludeId IS NULL OR r.idReserva <> :excludeId) " +
            "AND r.fechaReserva < :fin AND r.fechaTermino > :inicio")
    List<Reserva> findSolapamientosPsicologo(
            @Param("psicologoId") Long psicologoId,
            @Param("inicio") Timestamp inicio,
            @Param("fin") Timestamp fin,
            @Param("excludeId") Long excludeId);

    @Query("SELECT r FROM Reserva r WHERE r.paciente.id = :pacienteId " +
            "AND r.estado <> 'CANCELADA' " +
            "AND (:excludeId IS NULL OR r.idReserva <> :excludeId) " +
            "AND r.fechaReserva < :fin AND r.fechaTermino > :inicio")
    List<Reserva> findSolapamientosPaciente(
            @Param("pacienteId") Long pacienteId,
            @Param("inicio") Timestamp inicio,
            @Param("fin") Timestamp fin,
            @Param("excludeId") Long excludeId);

    @Query("SELECT r FROM Reserva r WHERE r.psicologo.id = :psicologoId " +
            "AND r.estado <> 'CANCELADA' " +
            "AND (:excludeId IS NULL OR r.idReserva <> :excludeId) " +
            "AND r.fechaReserva < :hasta AND r.fechaTermino > :desde " +
            "ORDER BY r.fechaReserva")
    List<Reserva> findOcupadosPsicologo(
            @Param("psicologoId") Long psicologoId,
            @Param("desde") Timestamp desde,
            @Param("hasta") Timestamp hasta,
            @Param("excludeId") Long excludeId);
}
