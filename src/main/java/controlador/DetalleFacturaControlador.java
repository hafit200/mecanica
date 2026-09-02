/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                double precioUnitario = Double.parseDouble(precioText.replace(",", "."));
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

    // Agrega el item a la JTable (4 columnas estándar de la UI)
    private void agregarRepuestoATabla() {
        try {
            String repuesto = (String) vfm.getCmbRepuesto().getSelectedItem();
            String precioU = vfm.getTxtPrecio().getText().trim();
            int cantidad = (int) vfm.getSpnCantidad().getValue();
            String subtotal = vfm.getTxtSubtotal().getText().trim();

            if (repuesto == null || repuesto.isEmpty() || repuesto.toUpperCase().contains("SELEACCIONE")) {
                JOptionPane.showMessageDialog(vfm, "Seleccione un repuesto válido.");
                return;
            }

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(vfm, "La cantidad debe ser mayor a 0.");
                return;
            }

            DefaultTableModel modelo = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();

            // Insertar exactamente 4 columnas para no descuadrar la vista
            Object[] fila = new Object[]{
                repuesto, // Columna 0: Nombre
                precioU,  // Columna 1: Precio Unitario
                cantidad, // Columna 2: Cantidad
                subtotal  // Columna 3: Subtotal
            };
            modelo.addRow(fila);

            // Resetear Spinner y Subtotal de entrada
            vfm.getSpnCantidad().setValue(0);
            vfm.getTxtSubtotal().setText("0.00");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vfm, "Error al agregar el repuesto a la tabla: " + ex.getMessage());
        }
    }

    // --- MÉTODO PARA GUARDAR LOS DETALLES EN LA BDD ---
    public boolean guardarDetalles(int idOrdenGenerado, String detalleServicio) {
        DefaultTableModel modeloTabla = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();

        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(vfm, "Debe agregar al menos un repuesto a la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sqlDetalle = "{CALL sp_guardar_detalle_orden(?, ?, ?, ?, ?)}";

        try (CallableStatement csDetalle = conectado.prepareCall(sqlDetalle)) {
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                
                // Mapeo seguro utilizando los índices exactos (0 a 3)
                String nombreRepuesto = modeloTabla.getValueAt(i, 0).toString();
                int cantidad = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
                double subtotal = Double.parseDouble(modeloTabla.getValueAt(i, 3).toString().replace(",", "."));

                // Obtenemos el ID real correspondiente al nombre del repuesto
                int idRepuesto = obtenerIdPorNombreRepuesto(nombreRepuesto);

                csDetalle.setInt(1, idOrdenGenerado);
                csDetalle.setInt(2, idRepuesto);
                csDetalle.setInt(3, cantidad);
                csDetalle.setDouble(4, subtotal);
                csDetalle.setString(5, detalleServicio);

                csDetalle.addBatch();
            }

            csDetalle.executeBatch();
            return true;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(vfm, "Error al guardar el detalle en la BDD: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Búsqueda dinámica del ID del repuesto en la BDD
    private int obtenerIdPorNombreRepuesto(String nombreRepuesto) {
        int id = 1;
        String sql = "SELECT ID_repuestos FROM repuestos WHERE nombre = ? LIMIT 1";
        try (PreparedStatement ps = conectado.prepareStatement(sql)) {
            ps.setString(1, nombreRepuesto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("ID_repuestos");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar ID del repuesto: " + e.getMessage());
        }
        return id;
    }
}
