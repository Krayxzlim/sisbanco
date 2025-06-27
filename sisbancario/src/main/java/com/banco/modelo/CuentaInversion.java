package com.banco.modelo;

import java.util.ArrayList;
import java.util.List;

public class CuentaInversion {
    private double saldo;
    private List<String> historial; 

    public CuentaInversion() {
        this.saldo = 0;
        this.historial = new ArrayList<>();        
    }

    public void depositar(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("Monto inválido");
        saldo += monto;
        historial.add("Depósito: +$" + monto + " | Nuevo saldo: $" + saldo);
    }

    public double getSaldo() {
        return saldo;
    }

    public List<String> getHistorial() {
        return historial;
    }

    public void actualizarPorInteresDiario() {
        if (saldo == 0) return;

        double tasa = (Math.random() * 0.1) - 0.05;
        double rendimiento = saldo * tasa;
        saldo += rendimiento;

        String reporte = String.format("Interés diario: %.2f%% | Saldo: $%.2f", tasa * 100, saldo);
        historial.add(reporte);
    }
}
