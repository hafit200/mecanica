/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import controlador.HistorialTrabajosControlador;

/**
 *
 * @author Asus
 */
public class Main2 {

    public static void main(String[] args) {
        // Ejecutar en el Event Dispatch Thread (EDT) de Swing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Instanciar la vista del menú del mecánico
                    MenuMecanico vistaMenu = new MenuMecanico();
                    
                    // 2. Instanciar el controlador vinculando la vista
                    HistorialTrabajosControlador controlador = new HistorialTrabajosControlador(vistaMenu);
                    
                    // 3. Hacer visible la ventana
                    vistaMenu.setVisible(true);
                    vistaMenu.setLocationRelativeTo(null); // Centrar en pantalla
                    
                } catch (Exception e) {
                    System.out.println("Error al iniciar la prueba: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
