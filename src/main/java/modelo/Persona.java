/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Asus
 */
public class Persona {
    private int id;
    private String nombre;
    private int cedula;
    private String correo;
    private double telefono;
    private String direccion;

    public Persona() {
    }

    public Persona(int id, String nombre, int cedula, String correo, double telefono, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
    }

   
    
}
