package com.banco.modelo;

import java.util.ArrayList;
import java.util.List;

public class CuentaBancaria {
    private static int contador = 1000;
    private String numeroCuenta;
    private double saldo;
    private boolean habilitada;
    private List<Transaccion> historial;

    public CuentaBancaria() {
        this(true);
    }

    public CuentaBancaria(boolean habilitada) {
        this.numeroCuenta = String.valueOf(contador++);
        this.saldo = 0;
        this.habilitada = habilitada;
        this.historial = new ArrayList<>();
    }

    public String getNumeroCuenta() { return numeroCuenta; }
    public double getSaldo() { return saldo; }
    public boolean isHabilitada() { return habilitada; }
    public void setHabilitada(boolean habilitada) { this.habilitada = habilitada; }
    public List<Transaccion> getHistorial() { return historial; }

    public void depositar(double monto, String usuario, int cajeroId) {
        saldo += monto;
        historial.add(new Transaccion(TipoOperacion.DEPOSITO, monto, usuario, cajeroId));
    }

    public boolean retirar(double monto, String usuario, int cajeroId) {
        if (saldo >= monto) {
            saldo -= monto;
            historial.add(new Transaccion(TipoOperacion.RETIRO, monto, usuario, cajeroId));
            return true;
        }
        return false;
    }

    public boolean transferir(double monto, CuentaBancaria destino, String usuario, int cajeroId) {
        if (saldo >= monto) {
            saldo -= monto;
            destino.recibirTransferencia(monto, usuario);
            historial.add(new Transaccion(TipoOperacion.TRANSFERENCIA, monto, usuario, cajeroId));
            return true;
        }
        return false;
    }

    private void recibirTransferencia(double monto, String remitente) {
        saldo += monto;
        historial.add(new Transaccion(TipoOperacion.DEPOSITO, monto, remitente, -1));
    }

    public void cambiarEstado(boolean habilitar, String usuarioEmpleado) {
    this.habilitada = habilitar;
    TipoOperacion op = habilitar ? TipoOperacion.ACTIVACION : TipoOperacion.DESACTIVACION;
    historial.add(new Transaccion(op, 0, usuarioEmpleado, -1));
}
}
