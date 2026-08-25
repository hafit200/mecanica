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

public class MarcaControlador {

    private VistaFacturaMecanica vfm;
    private ArrayList<String[]> listaMarcas;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public MarcaControlador() {
    }

    public MarcaControlador(VistaFacturaMecanica vfm, RepuestoControlador rc) {
        this.vfm = vfm;
        this.listaMarcas = obtenerMarcas();

        cargarMarcas(this.listaMarcas);

        // Evento al cambiar la marca seleccionada
        this.vfm.getCmbMarca().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vfm.getCmbMarca().getSelectedIndex();
                if (index >= 0 && listaMarcas != null && !listaMarcas.isEmpty()) {
                    String[] seleccionado = listaMarcas.get(index);
                    int idMarca = Integer.parseInt(seleccionado[0]);

                    // Llama al controlador de repuestos para actualizar el combo
                    rc.cargarRepuestosPorMarca(idMarca);
                }
            }
        });
    }

    // Obtener marcas desde la base de datos usando Stored Procedure
    public ArrayList<String[]> obtenerMarcas() {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "{CALL sp_obtener_marcas()}";

        try (CallableStatement ejecutar = conectado.prepareCall(sentenciaSQL);
             ResultSet res = ejecutar.executeQuery()) {

            while (res.next()) {
                String[] marca = new String[2];
                marca[0] = String.valueOf(res.getInt("ID_marca"));
                marca[1] = res.getString("nombre");

                lregistros.add(marca);
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar el SP de marcas: " + e.getMessage());
        }

        return lregistros;
    }

    // Cargar marcas en el JComboBox de la Vista
    public void cargarMarcas(ArrayList<String[]> lM) {
        vfm.getCmbMarca().removeAllItems();

        if (lM != null && !lM.isEmpty()) {
            for (String[] marca : lM) {
                vfm.getCmbMarca().addItem(marca[1]); // Agrega el nombre de la marca
            }
        }
    }
}
