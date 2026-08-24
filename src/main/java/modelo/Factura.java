/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Asus
 */
public class Factura {
     private int idFactura;
    private LocalDate fecha;
    private Cliente cliente;
    private List<DetalleFactura> listaArticulos;

    public Factura() {
    }

    public Factura(int idFactura, LocalDate fecha, Cliente cliente, List<DetalleFactura> listaArticulos) {
        this.idFactura = idFactura;
        this.fecha = fecha;
        this.cliente = cliente;
        this.listaArticulos = listaArticulos;
    }
    
}
