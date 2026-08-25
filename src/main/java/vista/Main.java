/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import controlador.ClienteControlador;
import controlador.DetalleFacturaControlador;
import controlador.FacturaControlador;
import controlador.MarcaControlador;
import controlador.MecanicoControlador;
import controlador.RepuestoControlador;
import controlador.VehiculoControlador;

/**
 *
 * @author Asus
 */
public class Main {

    public static void main(String[] args) {
        VistaFacturaMecanica vfm = new VistaFacturaMecanica();
        
        FacturaControlador fc = new FacturaControlador(vfm);
        
        // Controladores de vehículos y clientes vinculados
        VehiculoControlador vc = new VehiculoControlador(vfm);
        ClienteControlador cc = new ClienteControlador(vfm, vc);
        DetalleFacturaControlador dfc = new DetalleFacturaControlador(vfm);
        RepuestoControlador rc = new RepuestoControlador(vfm);
        MarcaControlador mac = new MarcaControlador(vfm, rc);

        fc.cargarFecha();

        vfm.setVisible(true);
        vfm.setLocationRelativeTo(null);
    }
}
