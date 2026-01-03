package com.Calendario.AgendarReservas.Repository;

import com.Calendario.AgendarReservas.Model.Reserva;
import com.Calendario.AgendarReservas.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Buscar todas las reservas de un usuario
    List<Reserva> findByUser(User user);

    // Buscar todas las reservas por userId
    List<Reserva> findByUserId(Long userId);

    // Buscar reserva por id y usuario (para verificar propiedad)
    Optional<Reserva> findByIdReservaAndUser(Long idReserva, User user);

    // Buscar reserva por id y userId
    Optional<Reserva> findByIdReservaAndUserId(Long idReserva, Long userId);

    // Buscar reservas ordenadas por fecha de creación descendente
    List<Reserva> findByUserOrderByFechaCreacionDesc(User user);

    // Buscar reservas por userId ordenadas por fecha de reserva
    List<Reserva> findByUserIdOrderByFechaReservaDesc(Long userId);

    // Contar reservas de un usuario
    long countByUserId(Long userId);

    // Buscar por estado y usuario
    @Query("SELECT r FROM Reserva r WHERE r.user.id = :userId AND r.estado = :estado")
    List<Reserva> findByUserIdAndEstado(@Param("userId") Long userId, @Param("estado") String estado);
}
