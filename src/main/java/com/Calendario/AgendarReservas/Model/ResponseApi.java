package com.calendario.agendarreservas.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseApi<T> {
    private int code;
    private String mensaje;
    private T entidad;

    public ResponseApi(int code, String mensaje) {
        this.code = code;
        this.mensaje = mensaje;
    }
}
