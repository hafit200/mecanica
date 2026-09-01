/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import controlador.ClienteControlador;
import controlador.DetalleFacturaControlador;
import controlador.FacturaControlador;
import controlador.LoginControlador;
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
        MenuLogin vl = new MenuLogin();
        LoginControlador lc = new LoginControlador(vl);
        vl.setLocationRelativeTo(null);
        vl.setVisible(true);
    }
}
