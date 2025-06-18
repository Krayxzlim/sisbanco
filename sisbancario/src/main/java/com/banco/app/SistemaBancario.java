package com.banco.app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import com.banco.modelo.Cliente;
import com.banco.modelo.CuentaBancaria;
import com.banco.modelo.Empleado;
import com.banco.negocio.Banco;

public class SistemaBancario {
    public static void main(String[] args) {
        Banco banco = new Banco();
        banco.inicializar();
        //bucle inicial
        while (true) {
            String[] opciones = {"Iniciar sesión", "Registrarse", "Salir"};
            int opcion = JOptionPane.showOptionDialog(null, "Bienvenido al Banco",
                    "Banco", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opciones, opciones[0]);

            if (opcion == JOptionPane.CLOSED_OPTION) {
                banco.guardarTodo();
                JOptionPane.showMessageDialog(null, "Gracias por usar el banco. ¡Hasta luego!");
                System.exit(0);
            }

            try {
                switch (opcion) {
                    case 0 -> login(banco);
                    case 1 -> registrar(banco);
                    case 2 -> {
                        banco.guardarTodo();
                        JOptionPane.showMessageDialog(null, "Gracias por usar el banco. ¡Hasta luego!");
                        System.exit(0);
                    }
                }
            } catch (OperacionCanceladaException e) {
                JOptionPane.showMessageDialog(null, "Operación cancelada. Volviendo al menú principal.");               
            }
        }
    }
//flujo cliente empleado
    private static void login(Banco banco) {
        String usuario = pedirTexto("Usuario:");
        String contrasena = pedirTexto("Contraseña:");

        var clienteOpt = banco.autenticarCliente(usuario, contrasena);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            int id = -1;
            while (true) {
                StringBuilder msg = new StringBuilder("Seleccione un cajero:\n");
                for (var cajero : banco.getCajeros()) {
                    msg.append("Cajero ").append(cajero.getId())
                            .append(" - Estado: ").append(cajero.estaHabilitado() ? "Habilitado" : "Deshabilitado")
                            .append(" - Dinero: $").append(cajero.getDineroDisponible()).append("\n");
                }
                id = pedirEntero("\n" + msg, 1, banco.getCajeros().size());
                
                var cajero = banco.obtenerCajero(id);
                if (cajero == null) {
                    JOptionPane.showMessageDialog(null, "Cajero no encontrado. Intente de nuevo.");
                } else if (!cajero.estaHabilitado()) {
                    JOptionPane.showMessageDialog(null, "Elija un cajero habilitado.");
                } else {
                    break;
                }
            }

            banco.registrarInicioSesion(cliente, id);
            seleccionarCuenta(cliente, banco);
            menuCliente(banco, cliente);
            return;
        }
//inicio empleado
        var empleadoOpt = banco.autenticarEmpleado(usuario, contrasena);
        if (empleadoOpt.isPresent()) {
            menuEmpleado(banco, empleadoOpt.get());
            //cambio de contrasena con pin, solo clientes (empleado no tiene pin)
        } else {
            if (JOptionPane.showConfirmDialog(null, "¿Olvidaste tu contraseña?", "Recuperar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String u = pedirTexto("Ingrese usuario:");
                String pin = pedirTexto("Ingrese PIN:");
                String nueva = pedirTexto("Nueva contraseña:");
                if (banco.recuperarContrasena(u, pin, nueva)) {
                    JOptionPane.showMessageDialog(null, "Contraseña actualizada.");
                } else {
                    JOptionPane.showMessageDialog(null, "Datos incorrectos.");
                }
            }
        }
    }
//menu de cuenta
    private static void seleccionarCuenta(Cliente cliente, Banco banco) {
        List<CuentaBancaria> cuentas = cliente.getCuentas();
        JPanel panel = new JPanel(new BorderLayout());
        JPanel cuentasPanel = new JPanel(new FlowLayout());

        ButtonGroup grupo = new ButtonGroup();
        JRadioButton[] radios = new JRadioButton[cuentas.size()];

        for (int i = 0; i < cuentas.size(); i++) {
            CuentaBancaria c = cuentas.get(i);
            String texto;
            if (c.isHabilitada()) {
                texto = "Cuenta: " + c.getNumeroCuenta() + " - Saldo: $" + c.getSaldo();
            } else {
                texto = "Cuenta: " + c.getNumeroCuenta() + " - Pendiente habilitación";
            }

            JRadioButton radio = new JRadioButton(texto);
            radios[i] = radio;
            grupo.add(radio);
            cuentasPanel.add(radio);

            if (!c.isHabilitada()) {
                radio.setEnabled(false);
                radio.addActionListener(e ->
                    JOptionPane.showMessageDialog(null, "La cuenta aún no está habilitada por el banco.")
                );
            }
        }

        JButton confirmarBtn = new JButton("Confirmar");
        JButton nuevaBtn = new JButton("Solicitar cuenta nueva");

        confirmarBtn.addActionListener(e -> {
            for (int i = 0; i < radios.length; i++) {
                if (radios[i].isSelected()) {
                    cliente.setCuentaActual(cuentas.get(i));
                    Window w = SwingUtilities.getWindowAncestor(confirmarBtn);
                    if (w != null) w.dispose();
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "Seleccione una cuenta.");
        });

        nuevaBtn.addActionListener(e -> {
            banco.crearNuevaCuentaParaCliente(cliente);
            JOptionPane.showMessageDialog(null, "Solicitud de nueva cuenta enviada.");
        });

        JPanel botones = new JPanel();
        botones.add(confirmarBtn);
        botones.add(nuevaBtn);

        panel.add(new JLabel("Seleccione su cuenta"), BorderLayout.NORTH);
        panel.add(cuentasPanel, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        JDialog dialog = new JDialog();
        dialog.setTitle("Cuentas");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
//registrar con boton de empleado
    private static void registrar(Banco banco) {
        JCheckBox checkEmpleado = new JCheckBox("Registrar como empleado");
        int res = JOptionPane.showConfirmDialog(null, checkEmpleado, "Tipo de usuario", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        boolean esEmpleado = checkEmpleado.isSelected();

        String nombre = pedirTexto("Nombre:");
        String apellido = pedirTexto("Apellido:");
        String usuario = pedirTexto("Usuario:");
        String contrasena = pedirTexto("Contraseña:");

        try {
            if (esEmpleado) {
                banco.registrarEmpleado(nombre, apellido, usuario, contrasena);
            } else {
                String pin;
                while (true) {
                    pin = pedirTexto("PIN (4 dígitos):");
                    if (pin.matches("\\d{4}")) break;
                    JOptionPane.showMessageDialog(null, "PIN inválido. Debe tener exactamente 4 dígitos numéricos.");
                }
                banco.registrarCliente(nombre, apellido, usuario, contrasena, pin);
            }
            JOptionPane.showMessageDialog(null, "Usuario registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

//menu una vez que se selecciona cuenta
    private static void menuCliente(Banco banco, Cliente cliente) {
        while (true) {
            String[] opciones = {"Depositar", "Retirar", "Transferir", "Ver saldo", "Ver historial", "Salir"};
            int op = JOptionPane.showOptionDialog(null, "Menu Cliente", "Opciones",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

            int cajeroId = cliente.getCuentaActual().getHistorial().getLast().getCajeroId();

            switch (op) {
                case 0 -> {
                    double monto = pedirDouble("Monto a depositar:");
                    banco.depositar(cliente, cajeroId, monto);
                }
                case 1 -> {
                    double monto = pedirDouble("Monto a retirar:");
                    if (!banco.retirar(cliente, cajeroId, monto))
                        JOptionPane.showMessageDialog(null, "Fondos insuficientes o cajero sin dinero.");
                }
                case 2 -> {
                    String destino = pedirTexto("Cuenta destino:");
                    double monto = pedirDouble("Monto a transferir:");
                    String pin = pedirTexto("PIN:");
                    if (!banco.transferir(cliente, destino, monto, pin))
                        JOptionPane.showMessageDialog(null, "Transferencia fallida.");
                }
                case 3 -> JOptionPane.showMessageDialog(null, "Saldo: $" + banco.verSaldo(cliente));
                case 4 -> {
                    StringBuilder sb = new StringBuilder("Historial:\n");
                    banco.verHistorial(cliente).forEach(t -> sb.append(t).append("\n"));
                    JOptionPane.showMessageDialog(null, sb);
                }
                case 5 -> { return; }
            }
        }
    }
//menu si se ingresa con usuario empleado
    private static void menuEmpleado(Banco banco, Empleado emp) {
        while (true) {
            String[] opciones = {"Ver cajeros", "Modificar cajero", "Ver pedidos de habilitación", "Salir"};
            int op = JOptionPane.showOptionDialog(null, "Empleado: " + emp.getNombreCompleto(),
                    "Menu Empleado", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opciones, opciones[0]);

            switch (op) {
                case 0 -> JOptionPane.showMessageDialog(null, banco.verEstadoCajeros());
                case 1 -> {
                    if (banco.getCajeros().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No hay cajeros disponibles para modificar.");
                        break;
                    }
                    //stream para rangos de ID
                    int minId = banco.getCajeros().stream()
                                    .mapToInt(c -> c.getId())
                                    .min().orElse(1);
                    int maxId = banco.getCajeros().stream()
                                    .mapToInt(c -> c.getId())
                                    .max().orElse(1);

                    int id;
                    try {
                        while (true) {
                            id = pedirEntero("Ingrese ID del cajero (entre " + minId + " y " + maxId + "):", minId, maxId);
                            if (banco.obtenerCajero(id) == null) {
                                JOptionPane.showMessageDialog(null, "Cajero no encontrado. Intente de nuevo.");
                            } else {
                                break;
                            }
                        }
                    } catch (OperacionCanceladaException e) {
                        break;
                    }

                    String[] acciones = {"Habilitar/Deshabilitar", "Agregar dinero", "Quitar dinero"};
                    int accion = JOptionPane.showOptionDialog(null, "Acción:", "Cajero",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, acciones, acciones[0]);
                    double monto = 0;
                    if (accion == 1 || accion == 2) {
                        monto = pedirDouble("Monto:");
                    }
                    banco.modificarCajero(id, accion, monto);
                }
                //cuentas para habilitar si un usuario las pide
                case 2 -> {
                    List<CuentaBancaria> pendientes = banco.obtenerCuentasPendientes();
                    if (pendientes.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No hay pedidos de habilitación pendientes.");
                        break;
                    }               

                    StringBuilder msg = new StringBuilder("Cuentas pendientes de habilitación:\n");
                    for (int i = 0; i < pendientes.size(); i++) {
                        CuentaBancaria c = pendientes.get(i);
                        Cliente clienteCuenta = banco.buscarClientePorCuenta(c);
                        String nombreUsuario = clienteCuenta != null ? clienteCuenta.getUsuario() : "Desconocido";
                        msg.append(i + 1).append(". Cuenta: ").append(c.getNumeroCuenta())
                        .append(" | Usuario: ").append(nombreUsuario).append("\n");
                    }

                    try {
                        int seleccion = pedirEntero(msg.toString() + "\nSeleccione un número para habilitar", 1, pendientes.size());

                        CuentaBancaria cuentaSeleccionada = pendientes.get(seleccion - 1);
                        Cliente clienteCuenta = banco.buscarClientePorCuenta(cuentaSeleccionada);
                        if (clienteCuenta == null) {
                            JOptionPane.showMessageDialog(null, "Error: cliente no encontrado para esta cuenta.");
                            break;
                        }

                        boolean confirm = JOptionPane.showConfirmDialog(null,
                                "¿Desea habilitar la cuenta " + cuentaSeleccionada.getNumeroCuenta() + "?",
                                "Confirmar habilitación",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

                        if (confirm) {
                            if (banco.cambiarEstadoCuenta(emp, clienteCuenta, cuentaSeleccionada.getNumeroCuenta(), true)) {
                                JOptionPane.showMessageDialog(null, "Cuenta habilitada con éxito.");
                            } else {
                                JOptionPane.showMessageDialog(null, "Error al habilitar la cuenta.");
                            }
                        }
                    } catch (OperacionCanceladaException e) {
                        break;
                    }
                }
                case 3 -> {
                    return;
                }
                default -> JOptionPane.showMessageDialog(null, "Seleccione una opción válida.");
            }
        }
    }
//metodos de entradas 
    private static String pedirTexto(String mensaje) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, mensaje);
            if (input == null) {
                throw new OperacionCanceladaException();
            }
            if (input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Este campo no puede estar vacío.");
                continue;
            }
            return input.trim();
        }
    }

    private static int pedirEntero(String mensaje, int min, int max) {
        while (true) {
            try {
                int numero = Integer.parseInt(pedirTexto(mensaje));
                if (numero >= min && numero <= max) {
                    return numero;
                } else {
                    JOptionPane.showMessageDialog(null, "Ingrese un número entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Ingrese un número entero válido.");
            }
        }
    }

    private static double pedirDouble(String mensaje) {
        while (true) {
            try {
                double valor = Double.parseDouble(pedirTexto(mensaje));
                if (valor <= 0) throw new NumberFormatException();
                return valor;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Ingrese un monto numérico válido y mayor que cero.");
            }
        }
    }
//intento de manejo de excepcion
    public static class OperacionCanceladaException extends RuntimeException {
    public OperacionCanceladaException() {
        super("Operación cancelada por el usuario");
        }
    }
}