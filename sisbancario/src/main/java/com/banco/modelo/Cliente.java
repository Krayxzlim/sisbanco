package com.banco.modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {
    private String pin;
    private List<CuentaBancaria> cuentas;
    private CuentaBancaria cuentaActual;
    private CuentaInversion cuentaInversion;

    public Cliente(String nombre, String apellido, String usuario, String contrasena, String pin) {
        super(nombre, apellido, usuario, contrasena, TipoUsuario.CLIENTE);
        this.pin = pin;
        this.cuentas = new ArrayList<>();
        CuentaBancaria cuentaInicial = new CuentaBancaria();
        this.cuentas.add(cuentaInicial);
        this.cuentaActual = cuentaInicial;
        this.cuentaInversion = new CuentaInversion();
    }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public List<CuentaBancaria> getCuentas() {
        return cuentas;
    }

    public CuentaBancaria getCuentaActual() {
        return cuentaActual;
    }

    public CuentaInversion getCuentaInversion() {
    return cuentaInversion;
    }

    public void setCuentaActual(CuentaBancaria cuenta) {
        if (cuentas.contains(cuenta)) {
            this.cuentaActual = cuenta;
        } else {
            throw new IllegalArgumentException("La cuenta no pertenece al cliente.");
        }
    }
}
