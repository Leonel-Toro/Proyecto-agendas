package com.Calendario.AgendarReservas.Service;

import com.Calendario.AgendarReservas.DTO.ReservaClienteDTO;
import com.Calendario.AgendarReservas.Model.Cliente;
import com.Calendario.AgendarReservas.Model.EstadoReserva;
import com.Calendario.AgendarReservas.Model.MedioContacto;
import com.Calendario.AgendarReservas.Model.Reserva;
import com.Calendario.AgendarReservas.Repository.ClienteRepository;
import com.Calendario.AgendarReservas.Repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public ReservaClienteDTO agendarCliente(ReservaClienteDTO reservaClienteDTO){
        if (reservaClienteDTO == null) throw new IllegalArgumentException("Payload vacío.");

        if (reservaClienteDTO.getPrecio() == null || reservaClienteDTO.getPrecio() < 0)
            throw new IllegalArgumentException("El precio debe ser >= 0.");

        if (reservaClienteDTO.getEstado() == null || reservaClienteDTO.getEstado().equals(""))
            throw new IllegalArgumentException("El estado es obligatorio.");

        if (reservaClienteDTO.getFechaReserva() == null)
            throw new IllegalArgumentException("La fecha de reserva es obligatoria.");

        LocalDateTime inicio = reservaClienteDTO.getFechaReserva().toLocalDateTime();

        if (inicio.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("La fecha de inicio debe ser futura.");

        // Campos de cliente
        if (reservaClienteDTO.getNombreCliente() == null || reservaClienteDTO.getNombreCliente().equals(""))
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");

        /*if (reservaClienteDTO.getEmailCliente() == null || reservaClienteDTO.getEmailCliente().equals(""))
            throw new IllegalArgumentException("Email de cliente inválido.");
        */
        if (reservaClienteDTO.getMedioCliente() == null || reservaClienteDTO.getMedioCliente().equals(""))
            throw new IllegalArgumentException("Debe seleccionar el medio por el cual fue contactado.");

        if(reservaClienteDTO.getNombreProducto() == null || reservaClienteDTO.getNombreProducto().equals("")){
            throw new IllegalArgumentException("El nombre del producto no debe ser vacio.");
        }

        Cliente cliente = null;
        if (reservaClienteDTO.getEmailCliente() != null && !reservaClienteDTO.getEmailCliente().equals("")) {
            cliente = clienteRepository.findByEmail(reservaClienteDTO.getEmailCliente().trim());
        }
        MedioContacto medioSeleccionado = MedioContacto.findById(Integer.parseInt(reservaClienteDTO.getMedioCliente()));
        if (cliente == null) {
            // Crea cliente nuevo si no existe por email
            cliente = new Cliente();
            cliente.setNombre(reservaClienteDTO.getNombreCliente().trim());
            cliente.setEmail(reservaClienteDTO.getEmailCliente());
            cliente.setTelefono(reservaClienteDTO.getTelefonoCliente() != null ? reservaClienteDTO.getTelefonoCliente().trim() : null);
            cliente.setMedio(medioSeleccionado);
        }

        Reserva r = new Reserva();
        EstadoReserva estadoSeleccionado = EstadoReserva.findEstado(Integer.parseInt(reservaClienteDTO.getEstado()));
        r.setFechaReserva(reservaClienteDTO.getFechaReserva());
        r.setPrecio(reservaClienteDTO.getPrecio());
        r.setEstado(estadoSeleccionado);
        r.setLugarEncuentro(reservaClienteDTO.getLugarEncuentro());
        r.setNombreProducto(reservaClienteDTO.getNombreProducto());
        r.setMensajePersonalizado(reservaClienteDTO.getMensajePersonalizado());
        clienteRepository.save(cliente);
        r.setCliente(cliente);

        reservaRepository.save(r);

        // Retornar el DTO con la reserva creada
        ReservaClienteDTO respuesta = new ReservaClienteDTO();
        respuesta.setPrecio(r.getPrecio());
        respuesta.setEstado(r.getEstado().toString());
        respuesta.setFechaReserva(r.getFechaReserva());
        respuesta.setLugarEncuentro(r.getLugarEncuentro());
        respuesta.setNombreProducto(r.getNombreProducto());
        respuesta.setMensajePersonalizado(r.getMensajePersonalizado());
        respuesta.setNombreCliente(cliente.getNombre());
        respuesta.setEmailCliente(cliente.getEmail());
        respuesta.setTelefonoCliente(cliente.getTelefono());
        respuesta.setMedioCliente(cliente.getMedio().toString());

        return respuesta;
    }

    public List<ReservaClienteDTO> obtenerHistorial(){
        List<Reserva> listaReserva = reservaRepository.findAll();
        List<ReservaClienteDTO> listaRCDTO = listaReserva.stream().map(r -> {
            ReservaClienteDTO reservaClienteDTO = new ReservaClienteDTO();
            reservaClienteDTO.setPrecio(r.getPrecio());
            reservaClienteDTO.setEstado(r.getEstado().toString());
            reservaClienteDTO.setFechaReserva(r.getFechaReserva());
            reservaClienteDTO.setLugarEncuentro(r.getLugarEncuentro());
            Optional<Cliente> optionalCliente = clienteRepository.findById(r.getCliente().getIdCliente());
            Cliente cliente = new Cliente();

            if(optionalCliente.isPresent()){
                cliente = optionalCliente.get();
            }
            reservaClienteDTO.setNombreCliente(cliente.getNombre());
            reservaClienteDTO.setEmailCliente(cliente.getEmail());
            reservaClienteDTO.setMedioCliente(cliente.getMedio().toString());
            reservaClienteDTO.setTelefonoCliente(cliente.getTelefono());
            return reservaClienteDTO;
        }).toList();
        return listaRCDTO;
    }

    public ReservaClienteDTO obtenerDetalleReserva(Long id){
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if(reservaOpt.isEmpty()){
            return null;
        }

        Reserva r = reservaOpt.get();
        ReservaClienteDTO reservaClienteDTO = new ReservaClienteDTO();
        reservaClienteDTO.setPrecio(r.getPrecio());
        reservaClienteDTO.setEstado(r.getEstado().toString());
        reservaClienteDTO.setFechaReserva(r.getFechaReserva());
        reservaClienteDTO.setLugarEncuentro(r.getLugarEncuentro());
        reservaClienteDTO.setNombreProducto(r.getNombreProducto());
        reservaClienteDTO.setMensajePersonalizado(r.getMensajePersonalizado());

        Optional<Cliente> optionalCliente = clienteRepository.findById(r.getCliente().getIdCliente());
        if(optionalCliente.isPresent()){
            Cliente cliente = optionalCliente.get();
            reservaClienteDTO.setNombreCliente(cliente.getNombre());
            reservaClienteDTO.setEmailCliente(cliente.getEmail());
            reservaClienteDTO.setMedioCliente(cliente.getMedio().toString());
            reservaClienteDTO.setTelefonoCliente(cliente.getTelefono());
        }

        return reservaClienteDTO;
    }
}
