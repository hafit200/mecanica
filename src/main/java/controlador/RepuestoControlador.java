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

public class RepuestoControlador {

    private VistaFacturaMecanica vfm;
    private ArrayList<String[]> listaRepuestos;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public RepuestoControlador() {
    }

    public RepuestoControlador(VistaFacturaMecanica vfm) {
        this.vfm = vfm;

        // Listener opcional para capturar el repuesto seleccionado y auto-completar el precio
        this.vfm.getCmbRepuesto().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vfm.getCmbRepuesto().getSelectedIndex();
                if (index >= 0 && listaRepuestos != null && !listaRepuestos.isEmpty()) {
                    String[] seleccionado = listaRepuestos.get(index);
                    // Coloca el precio unitario en el campo txtPrecio de la vista
                    vfm.getTxtPrecio().setText(seleccionado[2]);
                }
            }
        });
    }

    // Método que llama al Stored Procedure pasando el ID de la marca
    public ArrayList<String[]> obtenerRepuestosPorMarca(int idMarca) {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "{CALL sp_obtener_repuestos_por_marca(?)}";

        try {
            CallableStatement ejecutar = conectado.prepareCall(sentenciaSQL);
            ejecutar.setInt(1, idMarca);

            ResultSet res = ejecutar.executeQuery();

            while (res.next()) {
                String[] repuesto = new String[4];
                repuesto[0] = String.valueOf(res.getInt("ID_repuestos")); // Nombre corregido
                repuesto[1] = res.getString("nombre");
                repuesto[2] = String.valueOf(res.getDouble("precio"));       // Nombre corregido
                repuesto[3] = String.valueOf(res.getInt("stock"));

                lregistros.add(repuesto);
            }

            res.close();
            ejecutar.close();
        } catch (SQLException e) {
            System.out.println("Error al ejecutar el SP de repuestos: " + e.getMessage());
        }

        return lregistros;
    }

    // Carga los repuestos obtenidos en el JComboBox de repuestos
    public void cargarRepuestosPorMarca(int idMarca) {
        this.listaRepuestos = obtenerRepuestosPorMarca(idMarca);
        vfm.getCmbRepuesto().removeAllItems();

        if (this.listaRepuestos != null && !this.listaRepuestos.isEmpty()) {
            for (String[] repuesto : this.listaRepuestos) {
                vfm.getCmbRepuesto().addItem(repuesto[1]); // Muestra el Nombre del repuesto
            }
        }
    }
}
