/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.time.LocalDate;
import modelo.Factura;
import vista.VistaFacturaMecanica;

/**
 *
 * @author Asus
 */
public class FacturaControlador {
    private Factura f = new Factura();
    private VistaFacturaMecanica vfm;

    public FacturaControlador() {
    }

    public FacturaControlador(VistaFacturaMecanica vfm) {
        this.vfm = vfm;
    }
    public FacturaControlador(Factura f, VistaFacturaMecanica vfm) {
        this.f = f;
        this.vfm = vfm;
    }
    
    public void cargarFecha() {
        LocalDate fecha = LocalDate.now();
        vfm.setTxtFecha(fecha);
}
    
}
