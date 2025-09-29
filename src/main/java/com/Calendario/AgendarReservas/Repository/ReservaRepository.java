package com.Calendario.AgendarReservas.Repository;

import com.Calendario.AgendarReservas.Model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva,Long> {

}
