/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Asus
 */
public abstract class Vehiculo {

    private int año;
    private String modelo;
    private String marca;
    private String placa;

    public Vehiculo() {
    }

    public Vehiculo(int año, String modelo, String marca, String placa) {
        this.año = año;
        this.modelo = modelo;
        this.marca = marca;
        this.placa = placa;
    }
 
    public int getAño() {
        return año;
    }

    public void setAño(int año) {
        this.año = año;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public abstract double CalcularRecargo();
}
