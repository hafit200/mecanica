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
import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import modelo.Factura;
import vista.VistaFacturaMecanica;
import vista.VistaMecanico;

/**
 *
 * @author Asus
 */
public class FacturaControlador {

    private Factura f = new Factura();
    private VistaFacturaMecanica vfm;

    ConexionBDD conectar = new ConexionBDD();
    Connection conectado = (Connection) conectar.conectar();

    public FacturaControlador() {
    }

    public FacturaControlador(VistaFacturaMecanica vfm) {
        this.vfm = vfm;
        cargarFecha();
        cargarSiguienteNumeroOrden();
        initListeners();
        calcularYMostrarTotalRepuestos();
    }

    public FacturaControlador(Factura f, VistaFacturaMecanica vfm) {
        this.f = f;
        this.vfm = vfm;
        cargarFecha();
        cargarSiguienteNumeroOrden();
        initListeners();
        calcularYMostrarTotalRepuestos();
    }

    private void initListeners() {
        // Evento del botón para guardar la factura e ir al módulo del mecánico
        this.vfm.getBtnGuardar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarFacturaYAbrirMecanico();
            }
        });

        // EVENTO: Detecta el cambio en el ComboBox de la Placa para cargar Marca y Modelo
        this.vfm.getCmbPlaca().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (vfm.getCmbPlaca().getSelectedItem() != null) {
                    String placaSeleccionada = vfm.getCmbPlaca().getSelectedItem().toString().trim();
                    if (!placaSeleccionada.isEmpty() && !placaSeleccionada.toUpperCase().contains("SELEACCIONE")) {
                        cargarDetalleVehiculo(placaSeleccionada);
                    } else {
                        vfm.getTxtModelo().setText(""); // Limpia si selecciona la opción por defecto
                    }
                }
            }
        });

        // EVENTO: Escucha cambios en la JTable de repuestos para actualizar txtTotal automáticamente
        DefaultTableModel modeloTabla = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();
        modeloTabla.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                calcularYMostrarTotalRepuestos();
            }
        });
    }

    // Calcula el total de los repuestos sumando los valores de la JTable
    public double calcularYMostrarTotalRepuestos() {
        double totalRepuestos = 0.0;
        DefaultTableModel modelo = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();

        for (int i = 0; i < modelo.getRowCount(); i++) {
            // Se toma el valor del subtotal de la última columna
            Object valorSubtotal = modelo.getValueAt(i, modelo.getColumnCount() - 1);
            if (valorSubtotal != null) {
                try {
                    totalRepuestos += Double.parseDouble(valorSubtotal.toString().replace(",", "."));
                } catch (NumberFormatException ex) {
                    System.out.println("Error parseando subtotal de repuesto: " + ex.getMessage());
                }
            }
        }

        // Refresca el campo txtTotal de la vista principal
        if (vfm.getTxtTotal() != null) {
            vfm.getTxtTotal().setText(String.format("%.2f", totalRepuestos));
        }

        return totalRepuestos;
    }

    // Método auxiliar para consultar la marca y el modelo del vehículo por su placa
    private void cargarDetalleVehiculo(String placa) {
        String sql = "{CALL sp_obtener_detalle_vehiculo_por_placa(?)}";

        try (CallableStatement cs = conectado.prepareCall(sql)) {
            cs.setString(1, placa);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                String marca = rs.getString("marca");
                String modelo = rs.getString("modelo");

                vfm.getTxtMarca().setText(marca);
                vfm.getTxtModelo().setText(modelo);
            } else {
                vfm.getTxtMarca().setText("NA");
                vfm.getTxtModelo().setText("NA");
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Error al obtener detalle del vehículo: " + e.getMessage());
            vfm.getTxtMarca().setText("NA");
            vfm.getTxtModelo().setText("NA");
        }
    }

    private void guardarFacturaYAbrirMecanico() {
        // 1. Validaciones previas de campos obligatorios
        if (vfm.getTxtNumeroFactura().getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(vfm, "No hay un número de orden válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String detalleServicio = vfm.getTxtaDetalleServicio().getText().trim();
        if (detalleServicio.isEmpty()) {
            JOptionPane.showMessageDialog(vfm, "Debe ingresar el detalle del servicio/trabajo antes de guardar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String placa = "";
        if (vfm.getCmbPlaca().getSelectedItem() != null) {
            placa = vfm.getCmbPlaca().getSelectedItem().toString().trim();
        }

        int idCliente = 1;
        int idMecanicoInicial = 1;

        if (placa.isEmpty() || placa.toUpperCase().contains("SELEACCIONE")) {
            JOptionPane.showMessageDialog(vfm, "Debe seleccionar una placa de vehículo válida.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Guardar la Cabecera (orden_de_trabajo) y obtener el ID autogenerado
        int idOrdenGenerado = -1;
        String sqlCabecera = "{CALL sp_guardar_orden_trabajo(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = conectado.prepareCall(sqlCabecera)) {
            cs.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            cs.setString(2, placa);
            cs.setInt(3, idCliente);
            cs.setInt(4, idMecanicoInicial);

            cs.registerOutParameter(5, java.sql.Types.INTEGER);

            cs.execute();
            idOrdenGenerado = cs.getInt(5);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(vfm, "Error al guardar la orden de trabajo: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            System.out.println(ex);
            return;
        }

        // 3. Guardar repuestos y abrir ventana de Mecánico enviándole idOrden Y el subtotal de repuestos
        if (idOrdenGenerado > 0) {
            DetalleFacturaControlador detalleCtrl = new DetalleFacturaControlador(vfm);
            boolean repuestosGuardados = detalleCtrl.guardarDetalles(idOrdenGenerado, detalleServicio);

            if (repuestosGuardados) {
                double totalRepuestos = calcularYMostrarTotalRepuestos();

                // Abrir ventana del Mecánico PASÁNDOLE el idOrdenGenerado y totalRepuestos
                VistaMecanico vm = new VistaMecanico();
                MecanicoControlador mc = new MecanicoControlador(vm, idOrdenGenerado, totalRepuestos);

                vm.setLocationRelativeTo(vfm);
                vm.setVisible(true);

                // Reiniciar el formulario principal
                reiniciarFormulario();
            }
        } else {
            JOptionPane.showMessageDialog(vfm, "No se pudo generar el ID de la orden de trabajo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarFecha() {
        LocalDate fecha = LocalDate.now();
        vfm.setTxtFecha(fecha);
    }

    public void cargarSiguienteNumeroOrden() {
        String sql = "{CALL sp_obtener_siguiente_numero_orden()}";

        try (CallableStatement cs = conectado.prepareCall(sql); ResultSet rs = cs.executeQuery()) {

            if (rs.next()) {
                int siguienteNumero = rs.getInt("siguiente_numero");
                String numeroFormateado = String.format("%06d", siguienteNumero);
                vfm.getTxtNumeroFactura().setText(numeroFormateado);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener el número de orden: " + e.getMessage());
            vfm.getTxtNumeroFactura().setText("000001");
        }
    }

    public void reiniciarFormulario() {
        vfm.getTxtaDetalleServicio().setText("");
        vfm.getTxtModelo().setText("");
        vfm.getSpnCantidad().setValue(1);

        if (vfm.getCmbPlaca().getItemCount() > 0) {
            vfm.getCmbPlaca().setSelectedIndex(0);
        }
        if (vfm.getCmbCliente().getItemCount() > 0) {
            vfm.getCmbCliente().setSelectedIndex(0);
        }

        DefaultTableModel modelo = (DefaultTableModel) vfm.getTblDetalleFactura().getModel();
        modelo.setRowCount(0);

        cargarFecha();
        cargarSiguienteNumeroOrden();
        calcularYMostrarTotalRepuestos();
    }
}