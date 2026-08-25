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
import modelo.Mecanico;
import vista.VistaFacturaMecanica;
import vista.VistaMecanico;

public class MecanicoControlador {
    private Mecanico m;
    private VistaMecanico vm;
    private ArrayList<String[]> listaMecanicos;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public MecanicoControlador() {
    }

    public MecanicoControlador(VistaMecanico vm) {
        this.vm = vm;
        this.listaMecanicos = obtenerMecanicos();

        // Cargar mecánicos en el ComboBox de la VistaMecanico
        cargarMecanicos(this.listaMecanicos);

        // Inicializar listeners del formulario del mecánico
        initListeners();
    }

    private void initListeners() {
        // Imprimir selección del mecánico por consola
        this.vm.getCmbMecanico().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = vm.getCmbMecanico().getSelectedIndex();
                if (index >= 0 && listaMecanicos != null && !listaMecanicos.isEmpty()) {
                    String[] seleccionado = listaMecanicos.get(index);
                    System.out.println("Mecánico seleccionado -> ID: " + seleccionado[0] + " | Nombre: " + seleccionado[1]);
                }
            }
        });

        // Listeners para calcular mano de obra en tiempo real
        this.vm.getSpnHorasTrabajo().addChangeListener(e -> calcularSubtotalManoDeObra());
        this.vm.getCmbNivelTrabajo().addActionListener(e -> calcularSubtotalManoDeObra());
    }

    // Calcula Subtotal Mano de Obra (Horas * Tarifa)
    public void calcularSubtotalManoDeObra() {
        try {
            int horas = (int) vm.getSpnHorasTrabajo().getValue();
            String nivel = (String) vm.getCmbNivelTrabajo().getSelectedItem();
            double tarifaHora = 0.0;

            if (nivel != null) {
                switch (nivel.toUpperCase()) {
                    case "NIVEL 1": tarifaHora = 10.0; break;
                    case "NIVEL 2": tarifaHora = 15.0; break;
                    case "NIVEL 3": tarifaHora = 20.0; break;
                    case "NIVEL 4": tarifaHora = 25.0; break;
                    default: tarifaHora = 0.0; break;
                }
            }

            double subtotalManoObra = horas * tarifaHora;
            vm.getTxtSubtotalMecanico().setText(String.format("%.2f", subtotalManoObra));

        } catch (Exception e) {
            vm.getTxtSubtotalMecanico().setText("0.00");
        }
    }

    // Obtener mecánicos desde la BDD mediante Stored Procedure
    public ArrayList<String[]> obtenerMecanicos() {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "{CALL sp_obtener_mecanicos()}";

        try (CallableStatement ejecutar = conectado.prepareCall(sentenciaSQL);
             ResultSet res = ejecutar.executeQuery()) {

            while (res.next()) {
                String[] mecanico = new String[4];
                mecanico[0] = String.valueOf(res.getInt("ID_mecanico"));
                mecanico[1] = res.getString("Nombre");
                mecanico[2] = res.getString("cedula");
                mecanico[3] = res.getString("celular");

                lregistros.add(mecanico);
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar el SP de mecánicos: " + e.getMessage());
        }

        return lregistros;
    }

    // Cargar ítems en el JComboBox de VistaMecanico
    public void cargarMecanicos(ArrayList<String[]> lM) {
        vm.getCmbMecanico().removeAllItems();

        if (lM != null && !lM.isEmpty()) {
            for (String[] mecanico : lM) {
                vm.getCmbMecanico().addItem(mecanico[1]);
            }
        }
    }
}
