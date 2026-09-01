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

        // 1. Validación de campos requeridos
        if (nombre.isEmpty() || cedula.isEmpty()) {
            JOptionPane.showMessageDialog(
                    vistaCliente, 
                    "El nombre y la cédula son obligatorios.", 
                    "Campos incompletos", 
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Validación de Cédula Ecuatoriana usando esCedulaValida()
        if (!esCedulaValida(cedula)) {
            JOptionPane.showMessageDialog(
                    vistaCliente, 
                    "La cédula ingresada no es válida. Por favor, verifíquela.", 
                    "Cédula Inválida", 
                    JOptionPane.ERROR_MESSAGE
            );
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

    public static boolean esCedulaValida(String cedula) {
        // 1. Longitud exacta de 10 dígitos numéricos
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }

        // 2. Provincia válida (01 a 24, o 30 para ecuatorianos en el exterior)
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) {
            return false;
        }

        // 3. Tercer dígito menor a 6 (personas naturales)
        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito >= 6) {
            return false;
        }

        // 4. Algoritmo Módulo 10 (multiplicación por coeficientes 2 y 1)
        int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
        int suma = 0;

        for (int i = 0; i < 9; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));
            int producto = digito * coeficientes[i];

            if (producto >= 10) {
                producto -= 9;
            }

            suma += producto;
        }

        // 5. Cálculo del dígito verificador esperado
        int decenaSuperior = (int) Math.ceil((double) suma / 10) * 10;
        int digitoVerificadorEsperado = decenaSuperior - suma;

        if (digitoVerificadorEsperado == 10) {
            digitoVerificadorEsperado = 0;
        }

        // 6. Comparación con el décimo dígito
        int digitoVerificadorReal = Character.getNumericValue(cedula.charAt(9));

        return digitoVerificadorEsperado == digitoVerificadorReal;
    }
}
