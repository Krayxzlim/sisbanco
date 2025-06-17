package com.banco.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cajero {
    private int id;
    private double dineroDisponible;
    private boolean habilitado;
    private List<Transaccion> transacciones;

    public Cajero(int id) {
        this.id = id;
        this.dineroDisponible = 0;
        this.habilitado = true;
        this.transacciones = new ArrayList<>();
    }

    public int getId() { return id; }
    public boolean estaHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
    public double getDineroDisponible() { return dineroDisponible; }
    public void agregarDinero(double monto) { dineroDisponible += monto; }

    public boolean extraerDinero(double monto) {
        if (dineroDisponible >= monto) {
            dineroDisponible -= monto;
            return true;
        }
        return false;
    }

    public void registrarTransaccion(Transaccion t) { transacciones.add(t); }
    public List<Transaccion> getTransacciones() {
        return Collections.unmodifiableList(transacciones);
    }  
}