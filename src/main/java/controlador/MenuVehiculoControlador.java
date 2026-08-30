/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import vista.VehiculoVista;

/**
 *
 * @author Asus
 */
public class MenuVehiculoControlador {

    private VehiculoVista vista;
    private int idCliente;
    private Connection conexion;
    // Lista auxiliar para mapear el ID real de la BDD con la posición del ComboBox
    private ArrayList<Integer> listaIdsMarcas = new ArrayList<>();

    public MenuVehiculoControlador(VehiculoVista vista, int idCliente, Connection conexion) {
        this.vista = vista;
        this.idCliente = idCliente;
        this.conexion = conexion;
        
        cargarMarcas();
        
        // Listener asignado al botón guardar del formulario
        this.vista.getjButton1().addActionListener(e -> guardarVehiculo());
    }

   private void guardarVehiculo() {
    String placa = vista.getTxtPlaca().getText().trim();
    String modelo = vista.getTxtModelo().getText().trim();
    int indexMarca = vista.getCmbMarca().getSelectedIndex();

    // Validaciones previas
    if (placa.isEmpty() || modelo.isEmpty()) {
        JOptionPane.showMessageDialog(vista, "Por favor complete todos los campos.");
        return;
    }

    if (indexMarca < 0 || listaIdsMarcas.isEmpty()) {
        JOptionPane.showMessageDialog(vista, "Por favor seleccione una marca válida.");
        return;
    }

    // Recupera el ID_marca real directamente del ArrayList
    int idMarcaReal = listaIdsMarcas.get(indexMarca);

    // Evalúa cbxTipo: 1 si está marcado (Pesado), 2 si está desmarcado (Particular)
    int idTipoVehiculo = vista.getCbxTipo().isSelected() ? 1 : 2;

    String sql = "{CALL sp_guardar_vehiculo(?, ?, ?, ?, ?, ?)}";

    try (CallableStatement cs = conexion.prepareCall(sql)) {
        cs.setString(1, placa);
        cs.setString(2, modelo);
        cs.setInt(3, idMarcaReal);
        cs.setInt(4, this.idCliente);     // ID del cliente asignado
        cs.setInt(5, 5);                  // Asientos por defecto
        cs.setInt(6, idTipoVehiculo);     // Retorna 1 (Pesado) o 2 (Particular)

        cs.executeUpdate();

        JOptionPane.showMessageDialog(vista, "¡Vehículo guardado exitosamente!");
        vista.dispose(); // Cierra el formulario modal tras finalizar

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                vista,
                "Error al guardar vehículo: " + e.getMessage(),
                "Error SQL",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void cargarMarcas() {
        String sql = "{CALL sp_obtener_marcas()}";

        try (CallableStatement cs = conexion.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            vista.getCmbMarca().removeAllItems();
            listaIdsMarcas.clear();

            while (rs.next()) {
                // Guarda el ID real de la BDD y la cadena del nombre
                listaIdsMarcas.add(rs.getInt("ID_marca"));
                vista.getCmbMarca().addItem(rs.getString("nombre"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Error al cargar las marcas: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
