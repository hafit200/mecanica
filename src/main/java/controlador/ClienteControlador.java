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
import modelo.Cliente;
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

        this.vfm.getCmbCliente().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vfm.getCmbCliente().getSelectedIndex();
                if (index >= 0 && listaClientes != null && !listaClientes.isEmpty()) {
                    String[] seleccionado = listaClientes.get(index);

                    // Mapeo de campos a la vista
                    vfm.getTxtCedula().setText(seleccionado[2]);    // Posición 2: cedula
                    vfm.getTxtTelefono().setText(seleccionado[4]);  // Posición 4: celular
                    vfm.getTxtDireccion().setText(seleccionado[5]); // Posición 5: direccion
                    vfm.getTxtCorreo().setText(seleccionado[3]);    // Posición 3: email

                    // Cargar placas del cliente seleccionado
                    int idCliente = Integer.parseInt(seleccionado[0]); // Posición 0: ID_cliente
                    vc.cargarVehiculosPorCliente(idCliente);
                }
            }
        });
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
