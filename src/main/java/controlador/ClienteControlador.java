/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import modelo.Cliente;
import vista.ClienteVista;
import vista.VehiculoVista;
import vista.VistaFacturaMecanica;

public class ClienteControlador {

    private Cliente c;
    private VistaFacturaMecanica vfm;
    private ArrayList<String[]> listaClientes;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public ClienteControlador() {}

    public ClienteControlador(Cliente c, VistaFacturaMecanica vfm) {
        this.c = c;
        this.vfm = vfm;
    }

    public ClienteControlador(VistaFacturaMecanica vfm, VehiculoControlador vc) {
        this.vfm = vfm;
        this.listaClientes = obtenerClientes();

        CargarClientes(this.listaClientes);

        // 1. Cargar datos del cliente seleccionado en el ComboBox
        this.vfm.getCmbCliente().addActionListener(e -> {
            int index = vfm.getCmbCliente().getSelectedIndex();
            if (index >= 0 && listaClientes != null && !listaClientes.isEmpty()) {
                String[] seleccionado = listaClientes.get(index);

                // Mapeo de campos a la vista
                vfm.getTxtCedula().setText(seleccionado[2]);    // cedula
                vfm.getTxtTelefono().setText(seleccionado[4]);  // celular
                vfm.getTxtDireccion().setText(seleccionado[5]); // direccion
                vfm.getTxtCorreo().setText(seleccionado[3]);    // email

                // Cargar placas del cliente seleccionado
                int idCliente = Integer.parseInt(seleccionado[0]); // ID_cliente
                vc.cargarVehiculosPorCliente(idCliente);
            }
        });

        // 2. Evento para abrir la ventana de registro de cliente nuevo
        this.vfm.getBtnAgregarCliente().addActionListener(e -> {
            ClienteVista vistaCliente = new ClienteVista();
            new MenuClienteControlador(vistaCliente, conectado);
            
            // Refrescar el ComboBox automáticamente cuando se cierra la ventana de ClienteVista
            vistaCliente.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    actualizarListaClientes();
                }
            });

            vistaCliente.setLocationRelativeTo(vfm);
            vistaCliente.setVisible(true);
        });
    }

    // Método para refrescar el combo cuando se guarda un nuevo cliente
    public void actualizarListaClientes() {
        this.listaClientes = obtenerClientes();
        CargarClientes(this.listaClientes);
        if (!this.listaClientes.isEmpty()) {
            vfm.getCmbCliente().setSelectedIndex(this.listaClientes.size() - 1); // Selecciona el último cliente registrado
        }
    }

    private void limpiarCampos() {
        vfm.getTxtCedula().setText("");
        vfm.getTxtCorreo().setText("");
        vfm.getTxtTelefono().setText("");
        vfm.getTxtDireccion().setText("");
    }

    // Obtener Clientes usando Stored Procedure
    public ArrayList<String[]> obtenerClientes() {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "{CALL sp_obtener_clientes()}";

        try (CallableStatement ejecutar = conectado.prepareCall(sentenciaSQL);
             ResultSet res = ejecutar.executeQuery()) {

            while (res.next()) {
                String[] listaCliente = new String[6];
                listaCliente[0] = String.valueOf(res.getInt("ID_cliente"));
                listaCliente[1] = res.getString("Nombre");
                listaCliente[2] = res.getString("cedula");
                listaCliente[3] = res.getString("email");
                listaCliente[4] = res.getString("celular");
                listaCliente[5] = res.getString("direccion");

                lregistros.add(listaCliente);
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar el SP de clientes: " + e.getMessage());
        }

        return lregistros;
    }

    // Cargar clientes en el JComboBox
    public void CargarClientes(ArrayList<String[]> lC) {
        vfm.getCmbCliente().removeAllItems();

        if (lC != null && !lC.isEmpty()) {
            for (String[] cliente : lC) {
                vfm.getCmbCliente().addItem(cliente[1]); // Muestra el Nombre del cliente
            }
        }
    }
}