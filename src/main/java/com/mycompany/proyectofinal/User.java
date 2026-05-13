package com.mycompany.proyectofinal;

public class User {

    private String nombre;
    private String contrasena;

    public User(String nombre, String contrasena) {
        this.nombre = nombre;
        this.contrasena = contrasena;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

}
