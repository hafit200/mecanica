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
import javax.swing.JOptionPane;
import modelo.Mecanico;
import vista.VistaMecanico;

public class MecanicoControlador {
    private Mecanico m;
    private VistaMecanico vm;
    private ArrayList<String[]> listaMecanicos;
    private int idOrdenActual;
    private double totalRepuestos;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public MecanicoControlador() {
    }

    public MecanicoControlador(VistaMecanico vm) {
        this.vm = vm;
        this.listaMecanicos = obtenerMecanicos();

        cargarMecanicos(this.listaMecanicos);
        initListeners();
    }

    public MecanicoControlador(VistaMecanico vm, int idOrdenActual, double totalRepuestos) {
        this.vm = vm;
        this.idOrdenActual = idOrdenActual;
        this.totalRepuestos = totalRepuestos;
        this.listaMecanicos = obtenerMecanicos();

        cargarMecanicos(this.listaMecanicos);
        initListeners();
    }

    private void initListeners() {
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

        this.vm.getSpnHorasTrabajo().addChangeListener(e -> calcularSubtotalManoDeObra());
        this.vm.getCmbNivelTrabajo().addActionListener(e -> calcularSubtotalManoDeObra());

        this.vm.getBtnGuardarMecanico().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarAsignacionMecanico();
            }
        });
    }

    private void guardarAsignacionMecanico() {
        int index = vm.getCmbMecanico().getSelectedIndex();
        int horas = (int) vm.getSpnHorasTrabajo().getValue();
        String nivel = (String) vm.getCmbNivelTrabajo().getSelectedItem();
        String subtotalStr = vm.getTxtSubtotalMecanico().getText().replace(",", ".");

        if (index < 0 || listaMecanicos == null || listaMecanicos.isEmpty()) {
            JOptionPane.showMessageDialog(vm, "Debe seleccionar un mecánico válido.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (horas <= 0) {
            JOptionPane.showMessageDialog(vm, "Las horas de trabajo deben ser mayores a cero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] mecanicoSeleccionado = listaMecanicos.get(index);
        int idMecanico = Integer.parseInt(mecanicoSeleccionado[0]);
        double subtotalManoObra = Double.parseDouble(subtotalStr);

        // 1. Guardar detalle del mecánico mediante SP
        boolean guardadoExitoso = guardarDetalleMecanico(this.idOrdenActual, idMecanico, horas, nivel, subtotalManoObra);

        if (guardadoExitoso) {
            // 2. Calcular Total General
            double totalGeneral = this.totalRepuestos + subtotalManoObra;

            // 3. Actualizar el campo 'total' en orden_de_trabajo MEDIANTE STORED PROCEDURE
            actualizarTotalOrdenSP(this.idOrdenActual, totalGeneral);

            // 4. Mostrar JOptionPane con el desglose final
            String mensajeFinal = String.format(
                " ORDEN N° %06d FINALIZADA CON ÉXITO \n" +
                "-------------------------------------------\n" +
                " Mecánico Asignado: %s\n" +
                " Subtotal Repuestos: $%.2f\n" +
                " Mano de Obra:       $%.2f\n" +
                "-------------------------------------------\n" +
                " TOTAL GENERAL:      $%.2f",
                this.idOrdenActual, mecanicoSeleccionado[1], this.totalRepuestos, subtotalManoObra, totalGeneral
            );

            JOptionPane.showMessageDialog(
                vm, 
                mensajeFinal, 
                "Resumen General de la Orden", 
                JOptionPane.INFORMATION_MESSAGE
            );

            vm.dispose(); // Cierra el modal del mecánico
        } else {
            JOptionPane.showMessageDialog(vm, "Error al asociar el mecánico a la orden.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ejecuta el Stored Procedure para actualizar el valor total de la orden de trabajo
    public boolean actualizarTotalOrdenSP(int idOrden, double totalGeneral) {
        String sql = "{CALL sp_actualizar_total_orden(?, ?)}";

        try (CallableStatement cs = conectado.prepareCall(sql)) {
            cs.setInt(1, idOrden);
            cs.setDouble(2, totalGeneral);

            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al ejecutar sp_actualizar_total_orden: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarDetalleMecanico(int idOrden, int idMecanico, int horas, String nivel, double subtotal) {
        String sql = "{CALL sp_guardar_detalle_mecanico(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = conectado.prepareCall(sql)) {
            cs.setInt(1, idOrden);
            cs.setInt(2, idMecanico);
            cs.setInt(3, horas);
            cs.setString(4, nivel);
            cs.setDouble(5, subtotal);

            cs.execute();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al ejecutar sp_guardar_detalle_mecanico: " + e.getMessage());
            return false;
        }
    }

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

    public void cargarMecanicos(ArrayList<String[]> lM) {
        vm.getCmbMecanico().removeAllItems();

        if (lM != null && !lM.isEmpty()) {
            for (String[] mecanico : lM) {
                vm.getCmbMecanico().addItem(mecanico[1]);
            }
        }
    }
}