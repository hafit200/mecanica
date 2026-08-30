/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import vista.MenuMecanico; // Cambia según la clase real de tu vista

public class HistorialTrabajosControlador {

    private MenuMecanico vista;
    private ArrayList<Integer> listaIdsClientes = new ArrayList<>();
    
    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public HistorialTrabajosControlador(MenuMecanico vista) {
        this.vista = vista;

        // 1. Cargar clientes al abrir el panel
        cargarClientes();

        // 2. Evento al seleccionar un cliente -> Carga sus placas
        this.vista.getCmbCliente().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vista.getCmbCliente().getSelectedIndex();
                if (index >= 0 && !listaIdsClientes.isEmpty()) {
                    int idCliente = listaIdsClientes.get(index);
                    cargarPlacasPorCliente(idCliente);
                }
            }
        });

        // 3. Evento al seleccionar una placa -> Llena la tabla
        this.vista.getCmbPlaca().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (vista.getCmbPlaca().getSelectedItem() != null) {
                    String placa = vista.getCmbPlaca().getSelectedItem().toString();
                    cargarHistorialTrabajos(placa);
                }
            }
        });
    }

    private void cargarClientes() {
        listaIdsClientes.clear();
        vista.getCmbCliente().removeAllItems();
        String sql = "{CALL sp_obtener_clientes()}";

        try (CallableStatement cs = conectado.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                listaIdsClientes.add(rs.getInt(1)); // ID_cliente
                vista.getCmbCliente().addItem(rs.getString(2) + " " + rs.getString(3)); // Nombre Apellido
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener clientes: " + e.getMessage());
        }
    }

    private void cargarPlacasPorCliente(int idCliente) {
        vista.getCmbPlaca().removeAllItems();
        String sql = "{CALL sp_obtener_vehiculos_por_cliente(?)}";

        try (CallableStatement cs = conectado.prepareCall(sql)) {
            cs.setInt(1, idCliente);
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                vista.getCmbPlaca().addItem(rs.getString("Placa_vehiculo"));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar placas: " + e.getMessage());
        }
    }

    private void cargarHistorialTrabajos(String placa) {
        // Modelo adaptado a tus 4 columnas exactas de la interfaz
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("N° ORDEN");
        modelo.addColumn("FECHA");
        modelo.addColumn("DETALLE");
        modelo.addColumn("MECANICO");

        // Llamada al nuevo Stored Procedure
        String sql = "{CALL sp_obtener_detalles_anteriores(?)}";

        try (CallableStatement cs = conectado.prepareCall(sql)) {
            cs.setString(1, placa);
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getInt("ID_orden");
                fila[1] = rs.getString("fecha");
                fila[2] = rs.getString("descripcion");
                fila[3] = rs.getString("mecanico");

                modelo.addRow(fila);
            }

            vista.getTblTrabajos().setModel(modelo); // Asigna los datos a la tabla de la vista

        } catch (SQLException e) {
            System.out.println("Error al obtener el historial por placa: " + e.getMessage());
        }
    }
}
