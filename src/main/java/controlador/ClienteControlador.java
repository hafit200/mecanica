/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Cliente;
import vista.VistaFacturaMecanica;

/**
 *
 * @author Asus
 */
public class ClienteControlador {
    private Cliente c;
    private VistaFacturaMecanica vfm;
    private ArrayList<String[]> listaClientes;
    
    ConexionBDD conectar = new ConexionBDD();
    //CLASE QUE ME PERMITA CONECTARME DIRECTAMENTE A MYSQL
    Connection conectado = (Connection) conectar.conectar();
    //CLASE QUE ME PERMITE EJECUTAR MI SENTENCIA SQL
    PreparedStatement ejecutar;
    //OBTENER RESULTADOS DE LA CONSULTA
    ResultSet resultado;

    public ClienteControlador() {
    }
    
    

    public ClienteControlador(Cliente c, VistaFacturaMecanica vfm) {
        this.c = c;
        this.vfm = vfm;
    }
    
    public ClienteControlador(VistaFacturaMecanica vfm) {
    this.vfm = vfm;
    this.listaClientes = obtenerClientes();
    
    // Carga los clientes
    CargarClientes(this.listaClientes);
    
    // ENLAZA EL EVENTO DIRECTAMENTE
    this.vfm.getCmbCliente().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int index = vfm.getCmbCliente().getSelectedIndex();
            System.out.println("-> Clic detectado en combo. Indice: " + index);
            
            if (index > 0 && listaClientes != null && !listaClientes.isEmpty()) {
                String[] seleccionado = listaClientes.get(index - 1);
                System.out.println("ID Cliente: " + seleccionado[0]);
                
                vfm.getTxtCedula().setText(seleccionado[2]);
                vfm.getTxtCorreo().setText(seleccionado[3]);
                vfm.getTxtTelefono().setText(seleccionado[4]);
                vfm.getTxtDireccion().setText(seleccionado[5]);
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
    
    //Obtener Clientes
    public ArrayList<String[]> obtenerClientes() {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "SELECT ID_cliente, Nombre, cedula, email, celular, direccion FROM clientes";

        try {
            PreparedStatement ejecutar = conectado.prepareStatement(sentenciaSQL);
            ResultSet res = ejecutar.executeQuery();

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

            res.close();
            ejecutar.close();
        } catch (SQLException e) {
            System.out.println("Error al obtener clientes: " + e.getMessage());
        }

        return lregistros;
    }

    //Cargar clientes
    public void CargarClientes(ArrayList<String[]> lC) {
    // 1. Limpiamos el combo usando el getter de la vista
    vfm.getCmbCliente().removeAllItems();
    
    // Opción por defecto
    

    // 2. Validamos que la lista no esté vacía
    if (lC != null && !lC.isEmpty()) {
        for (String[] cliente : lC) {
            // Usamos vfm.getCmbCliente() para agregar el item
            // cliente[1] es el Nombre, cliente[2] es la Cédula
            vfm.getCmbCliente().addItem(cliente[1]);
        }
    }
    }
}

