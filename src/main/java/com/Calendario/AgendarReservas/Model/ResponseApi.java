package com.Calendario.AgendarReservas.Model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseApi<T> {
    private int code;
    private String mensaje;
    private T entidad;

    public ResponseApi() {
    }

    public ResponseApi(int code, String mensaje) {
        this.code = code;
        this.mensaje = mensaje;
    }

    public ResponseApi(int code, String mensaje, T entidad) {
        this.code = code;
        this.mensaje = mensaje;
        this.entidad = entidad;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getEntidad() {
        return entidad;
    }

    public void setEntidad(T entidad) {
        this.entidad = entidad;
    }
}
