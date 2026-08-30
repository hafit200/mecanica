/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.sql.Connection;
import javax.swing.JOptionPane;
import vista.ClienteVista;
import vista.VehiculoVista;
import vista.VistaFacturaMecanica;

/**
 *
 * @author Asus
 */
public class FacturaMecanicaControlador {

    private VistaFacturaMecanica vistaFactura;
    private Connection conexion;

    public FacturaMecanicaControlador(VistaFacturaMecanica vistaFactura, Connection conexion) {
        this.vistaFactura = vistaFactura;
        this.conexion = conexion;

        // Escuchadores de eventos para los botones de cliente y vehículo
        this.vistaFactura.getBtnAgregarCliente().addActionListener(e -> abrirRegistroCliente());
        this.vistaFactura.getBtnAgregarVehiculo().addActionListener(e -> abrirRegistroVehiculo());
    }

    // Flujo 1: Crear nuevo cliente -> Abre formulario de cliente y luego su primer vehículo
    private void abrirRegistroCliente() {
        ClienteVista vistaCliente = new ClienteVista();
        new MenuClienteControlador(vistaCliente, conexion);
        vistaCliente.setVisible(true);
    }

    // Flujo 2: Agregar vehículo a un cliente ya seleccionado en la factura
    private void abrirRegistroVehiculo() {
        int indexCliente = vistaFactura.getCmbCliente().getSelectedIndex();

        if (indexCliente < 0) {
            JOptionPane.showMessageDialog(
                vistaFactura, 
                "Por favor seleccione un cliente de la lista antes de añadir un vehículo.",
                "Cliente no seleccionado",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Asumiendo índice base 1 para los ID de clientes
        int idClienteSeleccionado = indexCliente + 1; 

        VehiculoVista vistaVehiculo = new VehiculoVista();
        new MenuVehiculoControlador(vistaVehiculo, idClienteSeleccionado, conexion);
        vistaVehiculo.setVisible(true);
    }
}
