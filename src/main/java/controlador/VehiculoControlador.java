/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

/**
 *
 * @author Asus
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import vista.VistaFacturaMecanica;

public class VehiculoControlador {
    private VistaFacturaMecanica vfm;
    private ArrayList<String[]> listaVehiculos;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public VehiculoControlador() {}

    public VehiculoControlador(VistaFacturaMecanica vfm) {
        this.vfm = vfm; // <-- IMPORTANTE: Asignar la vista

        // Listener para detectar cuando el usuario cambia de placa en el JComboBox
        this.vfm.getCmbPlaca().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vfm.getCmbPlaca().getSelectedIndex();
                // Solo actualiza si el índice es válido (evita errores al limpiar el combo)
                if (index >= 0) {
                    actualizarTipoVehiculo(index);
                }
            }
        });
    }

    // Consulta los vehículos desde la BDD mediante el Stored Procedure
    public ArrayList<String[]> obtenerVehiculosPorCliente(int idCliente) {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "{CALL sp_obtener_vehiculos_por_cliente(?)}";

        try {
            CallableStatement ejecutar = conectado.prepareCall(sentenciaSQL);
            ejecutar.setInt(1, idCliente);
            
            ResultSet res = ejecutar.executeQuery();

            while (res.next()) {
                String[] vehiculo = new String[5];
                vehiculo[0] = res.getString("Placa_vehiculo");
                vehiculo[1] = res.getString("modelo");
                vehiculo[2] = String.valueOf(res.getInt("ID_marca"));
                vehiculo[3] = String.valueOf(res.getInt("Numero_asientos"));
                vehiculo[4] = String.valueOf(res.getInt("ID_tipo_vehiculo"));

                lregistros.add(vehiculo);
            }

            res.close();
            ejecutar.close();
        } catch (SQLException e) {
            System.out.println("Error al ejecutar el SP de vehículos: " + e.getMessage());
        }

        return lregistros;
    }

    // Carga las placas en el JComboBox de la vista
    public void cargarVehiculosPorCliente(int idCliente) {
        this.listaVehiculos = obtenerVehiculosPorCliente(idCliente);
        vfm.getCmbPlaca().removeAllItems();

        if (this.listaVehiculos != null && !this.listaVehiculos.isEmpty()) {
            for (String[] vehiculo : this.listaVehiculos) {
                // Guardamos solo la placa para mantener limpia la vista
                vfm.getCmbPlaca().addItem(vehiculo[0]); 
            }
            // Selecciona automáticamente el primer vehículo cargado
            actualizarTipoVehiculo(0);
        }
    }

    // Método para activar el RadioButton según el tipo de vehículo (1 = Pesado, 2 = Particular)
    public void actualizarTipoVehiculo(int index) {
        if (index >= 0 && listaVehiculos != null && index < listaVehiculos.size()) {
            String[] vehiculo = listaVehiculos.get(index);
            int idTipo = Integer.parseInt(vehiculo[4]); // Posición 4: ID_tipo_vehiculo

            if (idTipo == 1) {
                vfm.getRbtPesado().setSelected(true);
            } else if (idTipo == 2) {
                vfm.getRbtParticular().setSelected(true);
            }
        }
    }
}
