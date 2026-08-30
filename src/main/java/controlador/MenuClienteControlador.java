/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import vista.ClienteVista;
import vista.VehiculoVista;

/**
 *
 * @author Asus
 */
public class MenuClienteControlador {

    private ClienteVista vistaCliente;
    private Connection conexion;

    // Constructor que recibe la vista y la conexión
    public MenuClienteControlador(ClienteVista vistaCliente, Connection conexion) {
        this.vistaCliente = vistaCliente;
        this.conexion = conexion;

        // Asignar el evento al botón GUARDAR de tu vista
        this.vistaCliente.getBtnGuardar().addActionListener(e -> guardarClienteYContinuarVehiculo());
    }

    private void guardarClienteYContinuarVehiculo() {
        // Obtener datos desde las variables de la vista 'vistaCliente'
        String nombre = vistaCliente.getTxtNombre().getText().trim();
        String cedula = vistaCliente.getTxtCedula().getText().trim();
        String email = vistaCliente.getTxtCorreo().getText().trim();
        String celular = vistaCliente.getTxtTelefono().getText().trim();
        String direccion = vistaCliente.getTxtDIreccion().getText().trim();

        if (nombre.isEmpty() || cedula.isEmpty()) {
            JOptionPane.showMessageDialog(vistaCliente, "El nombre y la cédula son obligatorios.");
            return;
        }

        String sql = "{CALL sp_guardar_cliente(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.setString(2, cedula);
            cs.setString(3, email);
            cs.setString(4, celular);
            cs.setString(5, direccion);

            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                if (rs != null && rs.next()) {
                    int nuevoIdCliente = rs.getInt("nuevo_id_cliente");

                    JOptionPane.showMessageDialog(
                            vistaCliente,
                            "Cliente guardado exitosamente. A continuación, ingrese su vehículo de inicio."
                    );

                    // 1. Cierra la ventana de cliente
                    vistaCliente.dispose();

                    // 2. Instancia la ventana VehiculoVista y le pasa el ID generado
                    VehiculoVista vehiculoVista = new VehiculoVista();
                    new MenuVehiculoControlador(vehiculoVista, nuevoIdCliente, conexion);
                    vehiculoVista.setVisible(true);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    vistaCliente,
                    "Error al guardar cliente: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
