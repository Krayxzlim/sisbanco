package com.banco.modelo;

public abstract class Usuario {
    protected String nombre;
    protected String apellido;
    protected String usuario;
    protected String contrasena;
    protected TipoUsuario tipo;

    public Usuario(String nombre, String apellido, String usuario, String contrasena, TipoUsuario tipo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.tipo = tipo;
    }

    public String getUsuario() { return usuario; }
    public String getContrasena() { return contrasena; }
    public TipoUsuario getTipo() { return tipo; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public void setContrasena(String nueva) { this.contrasena = nueva; }
}
