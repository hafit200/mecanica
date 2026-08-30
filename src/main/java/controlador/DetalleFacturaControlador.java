/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import vista.VistaFacturaMecanica;

/**
 *
 * @author Asus
 */
public class DetalleFacturaControlador {

    private VistaFacturaMecanica vfm;
    private ConexionBDD conectar = new ConexionBDD();
    private Connection conectado = (Connection) conectar.conectar();

    public DetalleFacturaControlador() {}

    public DetalleFacturaControlador(VistaFacturaMecanica vfm) {
        this.vfm = vfm;
        initListeners();
    }

    private void initListeners() {
        // --- LISTENERS DE REPUESTOS Y MARCAS ---
        this.vfm.getCmbMarca().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularSubtotalRepuesto();
            }
        });

        this.vfm.getCmbRepuesto().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularSubtotalRepuesto();
            }
        });

        this.vfm.getSpnCantidad().addChangeListener(e -> calcularSubtotalRepuesto());

        // --- LISTENER DEL BOTÓN AGREGAR A LA TABLA ---
        this.vfm.getBtnAgregar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarRepuestoATabla();
            }
        });
    }

    // Calcula Subtotal Repuestos (Precio U. * Cantidad)
    public void calcularSubtotalRepuesto() {
        try {
            String precioText = vfm.getTxtPrecio().getText().trim();
            
            if (!precioText.isEmpty()) {
                double precioUnitario = Double.parseDouble(precioText);
                int cantidad = (int) vfm.getSpnCantidad().getValue();

                double subtotalRepuesto = precioUnitario * cantidad;
                vfm.getTxtSubtotal().setText(String.format("%.2f", subtotalRepuesto));
            } else {
                vfm.getTxtSubtotal().setText("0.00");
            }
        } catch (Exception e) {
            vfm.getTxtSubtotal().setText("0.00");
        }
    }

    // Agrega el item a la JTable al hacer clic en btnAgregar
    private void agregarRepuestoATabla() {
        try {
            String repuesto = (String) vfm.getCmbRepuesto().getSelectedItem();
            String precioU = vfm.getTxtPrecio().getText().trim();
            int cantidad = (int) vfm.getSpnCantidad().getValue();
            String subtotal = vfm.getTxtSubtotal().getText().trim();

            if (repuesto == null || repuesto.isEmpty() || repuesto.contains("SELEACCIONE")) {
                JOptionPane.showMessageDialog(vfm, "Seleccione un repuesto válido.");
                return;
            }

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(vfm, "La cantidad debe ser mayor a 0.");
                return;
            }

            // Modelo de la tabla de la vista principal
            DefaultTableModel modelo = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();

            // Insertar fila
            Object[] fila = new Object[]{
                repuesto,
                precioU,
                cantidad,
                subtotal
            };
            modelo.addRow(fila);

            // Resetear Spinner y Subtotal de entrada
            vfm.getSpnCantidad().setValue(0);
            vfm.getTxtSubtotal().setText("0.00");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vfm, "Error al agregar el repuesto a la tabla: " + ex.getMessage());
        }
    }

    // --- MÉTODO PARA GUARDAR LOS DETALLES DE LA TABLA EN LA BDD ---
    public boolean guardarDetalles(int idOrdenGenerado, String detalleServicio) {
    DefaultTableModel modeloTabla = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();

    if (modeloTabla.getRowCount() == 0) {
        JOptionPane.showMessageDialog(vfm, "Debe agregar al menos un repuesto a la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    String sqlDetalle = "{CALL sp_guardar_detalle_orden(?, ?, ?, ?, ?)}";

    try (CallableStatement csDetalle = conectado.prepareCall(sqlDetalle)) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            int idRepuesto = 1; // Ajusta según obtengas el ID real del repuesto
            int cantidad = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
            double subtotal = Double.parseDouble(modeloTabla.getValueAt(i, 3).toString().replace(",", "."));

            csDetalle.setInt(1, idOrdenGenerado);
            csDetalle.setInt(2, idRepuesto);
            csDetalle.setInt(3, cantidad);
            csDetalle.setDouble(4, subtotal);
            csDetalle.setString(5, detalleServicio); // Envía el texto ingresado en el JTextArea

            csDetalle.addBatch();
        }

        csDetalle.executeBatch();
        return true;

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(vfm, "Error al guardar el detalle en la BDD: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        return false;
    }
}
}
