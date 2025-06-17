package com.banco.modelo;

public class Empleado extends Usuario {
    public Empleado(String nombre, String apellido, String usuario, String contrasena) {
        super(nombre, apellido, usuario, contrasena, TipoUsuario.EMPLEADO);
    }
}