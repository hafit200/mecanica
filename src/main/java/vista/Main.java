/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import controlador.ClienteControlador;
import controlador.FacturaControlador;

/**
 *
 * @author Asus
 */
public class Main {

   public static void main(String[] args) {
    VistaFacturaMecanica vfm = new VistaFacturaMecanica();
    FacturaControlador fc = new FacturaControlador(vfm);
       ClienteControlador cc = new ClienteControlador(vfm);
    
    fc.cargarFecha();
    
    vfm.setVisible(true);
    vfm.setLocationRelativeTo(null);
    
}
}
