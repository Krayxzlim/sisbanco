package com.banco.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaccion {
    private TipoOperacion tipo;
    private double monto;
    private String usuario;
    private int cajeroId;
    private LocalDateTime fecha;

    public Transaccion(TipoOperacion tipo, double monto, String usuario, int cajeroId) {
        this.tipo = tipo;
        this.monto = monto;
        this.usuario = usuario;
        this.cajeroId = cajeroId;
        this.fecha = LocalDateTime.now();
    }

    public int getCajeroId() {
        return cajeroId;
    }

    public String getUsuario() {
    return usuario;
    }

    @Override
    public String toString() {
        if (this.tipo == TipoOperacion.CONEXION) {
            return "Conectado al cajero " + this.getCajeroId() + " (usuario: " + this.getUsuario() + ")";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "[" + fecha.format(formatter) + "] " + tipo + " - $" + monto +
            " - Usuario: " + usuario + " - Cajero: " + cajeroId;
    }
}
