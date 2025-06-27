package com.banco.negocio;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.banco.modelo.Cajero;
import com.banco.modelo.Cliente;
import com.banco.modelo.CuentaBancaria;
import com.banco.modelo.Empleado;
import com.banco.modelo.TipoOperacion;
import com.banco.modelo.Transaccion;
import com.google.gson.reflect.TypeToken;

public class Banco {
    //atributos y json para listas de entidades
    private List<Cliente> clientes;
    private List<Empleado> empleados;
    private List<Cajero> cajeros;

    private static final String ARCH_CLIENTES = "clientes.json";
    private static final String ARCH_EMPLEADOS = "empleados.json";
    private static final String ARCH_CAJEROS = "cajeros.json";

    public Banco() {
        clientes = new ArrayList<>();
        empleados = new ArrayList<>();
        cajeros = new ArrayList<>();
        for (int i = 1; i <= 4; i++) cajeros.add(new Cajero(i));
    }

    public void inicializar() {
        //carga y o crea json
        Type tipoClientes = new TypeToken<List<Cliente>>() {}.getType();
        clientes = GestorArchivos.cargarLista(ARCH_CLIENTES, tipoClientes);

        Type tipoEmpleados = new TypeToken<List<Empleado>>() {}.getType();
        empleados = GestorArchivos.cargarLista(ARCH_EMPLEADOS, tipoEmpleados);

        Type tipoCajeros = new TypeToken<List<Cajero>>() {}.getType();
        List<Cajero> cajerosGuardados = GestorArchivos.cargarLista(ARCH_CAJEROS, tipoCajeros);

        if (cajerosGuardados.isEmpty()) {
            cajeros.clear();
            for (int i = 1; i <= 4; i++) cajeros.add(new Cajero(i));
        } else {
            cajeros = cajerosGuardados;
        }
    }
//autenticaciones
    public Optional<Cliente> autenticarCliente(String usuario, String contrasena) {
        return clientes.stream()
                .filter(c -> c.getUsuario().equals(usuario) && c.getContrasena().equals(contrasena))
                .findFirst();
    }

    public Optional<Empleado> autenticarEmpleado(String usuario, String contrasena) {
        return empleados.stream()
                .filter(e -> e.getUsuario().equals(usuario) && e.getContrasena().equals(contrasena))
                .findFirst();
    }
//modificar pass, podria agregar alguna validacion externa o con el usuario empleado quizas
    public boolean recuperarContrasena(String usuario, String pin, String nuevaContrasena) {
        for (Cliente c : clientes) {
            if (c.getUsuario().equals(usuario) && c.getPin().equals(pin)) {
                c.setContrasena(nuevaContrasena);
                return true;
            }
        }
        return false;
    }
//registros
    public void registrarCliente(String nombre, String apellido, String usuario, String contrasena, String pin) {
        boolean existe = clientes.stream().anyMatch(c -> c.getUsuario().equalsIgnoreCase(usuario));
        if (existe) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }
        clientes.add(new Cliente(nombre, apellido, usuario, contrasena, pin));
    }

    public void registrarEmpleado(String nombre, String apellido, String usuario, String contrasena) {
    boolean existe = empleados.stream().anyMatch(e -> e.getUsuario().equalsIgnoreCase(usuario)) ||
                     clientes.stream().anyMatch(c -> c.getUsuario().equalsIgnoreCase(usuario));
    if (existe) {
        throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
    }
    empleados.add(new Empleado(nombre, apellido, usuario, contrasena));
    }


//guardar datos
    public void guardarTodo() {
        GestorArchivos.guardarLista(ARCH_CLIENTES, clientes);
        GestorArchivos.guardarLista(ARCH_EMPLEADOS, empleados);
        GestorArchivos.guardarLista(ARCH_CAJEROS, cajeros);
    }

    public List<Cajero> getCajeros() {
        return cajeros;
    }

    public Cajero obtenerCajero(int id) {
        return cajeros.get(id - 1);
    }

    public boolean registrarInicioSesion(Cliente cliente, int cajeroId) {
        Cajero cajero = obtenerCajero(cajeroId);
        if (!cajero.estaHabilitado()) return false;
        cliente.getCuentaActual().getHistorial().add(new Transaccion(TipoOperacion.CONEXION, 0, cliente.getUsuario(), cajeroId));
        return true;
    }
//operaciones cliente
    public boolean depositar(Cliente cliente, int cajeroId, double monto) {
        Cajero cajero = obtenerCajero(cajeroId);
        cliente.getCuentaActual().depositar(monto, cliente.getUsuario(), cajeroId);
        cajero.agregarDinero(monto);
        cajero.registrarTransaccion(new Transaccion(TipoOperacion.DEPOSITO, monto, cliente.getUsuario(), cajeroId));
        guardarTodo();
        return true;
    }

    public boolean retirar(Cliente cliente, int cajeroId, double monto) {
        Cajero cajero = obtenerCajero(cajeroId);
        if (!cajero.extraerDinero(monto)) return false;
        if (!cliente.getCuentaActual().retirar(monto, cliente.getUsuario(), cajeroId)) return false;
        cajero.registrarTransaccion(new Transaccion(TipoOperacion.RETIRO, monto, cliente.getUsuario(), cajeroId));
        guardarTodo();
        return true;
    }

    public boolean transferir(Cliente remitente, String cuentaDestino, double monto, String pin) {
        if (!remitente.getPin().equals(pin)) return false;

        CuentaBancaria cuentaDest = null;
        Cliente receptor = null;

        for (Cliente c : clientes) {
            for (CuentaBancaria cuenta : c.getCuentas()) {
                if (cuenta.getNumeroCuenta().equals(cuentaDestino)) {
                    receptor = c;
                    cuentaDest = cuenta;
                    break;
                }
            }
            if (receptor != null) break;
        }

        if (receptor == null || cuentaDest == null) return false;

        int cajeroId = remitente.getCuentaActual().getHistorial().getLast().getCajeroId();
        Cajero cajero = obtenerCajero(cajeroId);

        boolean ok = remitente.getCuentaActual().transferir(monto, cuentaDest, remitente.getUsuario(), cajeroId);

        if (ok) {
            cajero.registrarTransaccion(new Transaccion(TipoOperacion.TRANSFERENCIA, monto, remitente.getUsuario(), cajeroId));
            guardarTodo();
        }
        return ok;
    }


    public List<Transaccion> verHistorial(Cliente cliente) {
        return cliente.getCuentaActual().getHistorial();
    }

    public double verSaldo(Cliente cliente) {
        return cliente.getCuentaActual().getSaldo();
    }

    public String verEstadoCajeros() {
        StringBuilder sb = new StringBuilder();
        for (Cajero c : cajeros) {
            sb.append("Cajero ").append(c.getId())
              .append(" | Habilitado: ").append(c.estaHabilitado())
              .append(" | Dinero: $").append(c.getDineroDisponible())
              .append("\nOperaciones:\n");
            for (Transaccion t : c.getTransacciones()) {
                sb.append(t.toString()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void modificarCajero(int id, int accion, double monto) {
        Cajero cajero = obtenerCajero(id);
        switch (accion) {
            case 0 -> cajero.setHabilitado(!cajero.estaHabilitado());
            case 1 -> cajero.agregarDinero(monto);
            case 2 -> cajero.extraerDinero(monto);
        }
        guardarTodo();
    }
//cuenta cliente
    public void seleccionarCuenta(Cliente cliente, CuentaBancaria cuentaSeleccionada) {
    cliente.setCuentaActual(cuentaSeleccionada);
    }

    public List<CuentaBancaria> obtenerCuentasDeCliente(Cliente cliente) {
        return cliente.getCuentas();
    }

    public CuentaBancaria crearNuevaCuentaParaCliente(Cliente cliente) {
        CuentaBancaria nueva = new CuentaBancaria(false);
        cliente.getCuentas().add(nueva);
        guardarTodo();
        return nueva;
    }
//visualizacion de cuentas para clientes
    public String verCuentasClientes() {
        StringBuilder sb = new StringBuilder();
        for (Cliente c : clientes) {
            sb.append("Cliente: ").append(c.getNombreCompleto()).append("\n");
            for (CuentaBancaria cuenta : c.getCuentas()) {
                sb.append(" - Cuenta: ").append(cuenta.getNumeroCuenta())
                .append(" | Saldo: $").append(cuenta.getSaldo())
                .append(" | Estado: ").append(cuenta.isHabilitada() ? "Habilitada" : "Deshabilitada").append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
//mostrar de que usuario es la cuenta
    public Cliente buscarClientePorCuenta(CuentaBancaria cuentaBuscada) {
        for (Cliente cliente : clientes) {
            for (CuentaBancaria cuenta : cliente.getCuentas()) {
                if (cuenta.equals(cuentaBuscada)) {
                    return cliente;
                }
            }
        }
        return null;
    }
    //empleado, activacion cuenta
    public boolean cambiarEstadoCuenta(Empleado empleado, Cliente cliente, String numeroCuenta, boolean habilitar) {
        for (CuentaBancaria cuenta : cliente.getCuentas()) {
            if (cuenta.getNumeroCuenta().equals(numeroCuenta)) {             
                cuenta.cambiarEstado(habilitar, empleado.getUsuario());
                guardarTodo();
                return true;
            }
        }
        return false; 
    }

    public List<CuentaBancaria> obtenerCuentasPendientes() {
    List<CuentaBancaria> pendientes = new ArrayList<>();
    for (Cliente cliente : clientes) {
        for (CuentaBancaria cuenta : cliente.getCuentas()) {
            if (!cuenta.isHabilitada()) {
                pendientes.add(cuenta);
            }
        }
    }
    return pendientes;
    }
    //sim dia
    public void simularInteresDiario() {
    for (Cliente cliente : clientes) {
        cliente.getCuentaInversion().actualizarPorInteresDiario();
    }
    guardarTodo();
    }
    //deposito en cuenta inve
    public void invertir(Cliente cliente, double monto) {
    if (!cliente.getCuentaActual().retirar(monto, cliente.getUsuario(), -1)) {
        throw new IllegalArgumentException("Fondos insuficientes en cuenta principal.");
    }
    cliente.getCuentaInversion().depositar(monto);
    guardarTodo();
    }


}