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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import vista.VehiculoVista;
import vista.VistaFacturaMecanica;



public class VehiculoControlador {

    private VistaFacturaMecanica vfm;
    private ArrayList<String[]> listaVehiculos;
    // Lista paralela para asociar el índice del JComboBox con el ID_cliente real
    private ArrayList<Integer> listaIdsClientes = new ArrayList<>();

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public VehiculoControlador() {}

    public VehiculoControlador(VistaFacturaMecanica vfm) {
        this.vfm = vfm;

        // Carga los IDs reales de los clientes mediante tu SP al instanciar el controlador
        cargarIdsClientes();

        // 1. Escuchador para detectar cambio de placa en el JComboBox
        this.vfm.getCmbPlaca().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vfm.getCmbPlaca().getSelectedIndex();
                if (index >= 0) {
                    actualizarTipoVehiculo(index);
                }
            }
        });

        // 2. Escuchador para el botón '+' al lado de PLACA
        this.vfm.getBtnAgregarVehiculo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirModalVehiculo();
            }
        });
    }

    // Ejecuta tu Stored Procedure para mapear los IDs reales de la base de datos
    private void cargarIdsClientes() {
        listaIdsClientes.clear();
        String sql = "{CALL sp_obtener_clientes()}";

        try (CallableStatement cs = conectado.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                // Lee la primera columna de la consulta (que es el ID_cliente)
                listaIdsClientes.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener lista de clientes mediante SP: " + e.getMessage());
        }
    }

    // Método para abrir la ventana de nuevo vehículo vinculada al cliente actual
    private void abrirModalVehiculo() {
        int indexCliente = vfm.getCmbCliente().getSelectedIndex();

        if (indexCliente < 0) {
            JOptionPane.showMessageDialog(
                vfm,
                "Por favor seleccione un cliente antes de añadir un vehículo.",
                "Cliente no seleccionado",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Si por alguna razón la lista de IDs está vacía, la recargamos mediante el SP
        if (listaIdsClientes.isEmpty()) {
            cargarIdsClientes();
        }

        // Obtiene el ID_cliente verdadero correspondiente al ítem seleccionado en el JComboBox
        int idClienteSeleccionado = listaIdsClientes.get(indexCliente);

        VehiculoVista vistaVehiculo = new VehiculoVista();
        new MenuVehiculoControlador(vistaVehiculo, idClienteSeleccionado, conectado);

        vistaVehiculo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cargarVehiculosPorCliente(idClienteSeleccionado);
            }
        });

        vistaVehiculo.setLocationRelativeTo(vfm);
        vistaVehiculo.setVisible(true);
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
                vfm.getCmbPlaca().addItem(vehiculo[0]); 
            }
            // Selecciona automáticamente la última placa agregada
            vfm.getCmbPlaca().setSelectedIndex(this.listaVehiculos.size() - 1);
        }
    }

    // Método para activar el RadioButton según el tipo de vehículo
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