/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import modelo.Factura;
import vista.VistaFacturaMecanica;

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
    }

    public FacturaControlador(Factura f, VistaFacturaMecanica vfm) {
        this.f = f;
        this.vfm = vfm;
        cargarFecha();
        cargarSiguienteNumeroOrden();
    }

    public void cargarFecha() {
        LocalDate fecha = LocalDate.now();
        vfm.setTxtFecha(fecha);
    }

    // Obtener el siguiente ID_orden consecutivo usando el Stored Procedure
    public void cargarSiguienteNumeroOrden() {
        String sql = "{CALL sp_obtener_siguiente_numero_orden()}";

        try (CallableStatement cs = conectado.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            if (rs.next()) {
                int siguienteNumero = rs.getInt("siguiente_numero");
                
                // Formatea a 6 dígitos (Ej: 1 -> 000001)
                String numeroFormateado = String.format("%06d", siguienteNumero);
                
                // Asigna el valor al campo N° de la vista
                vfm.getTxtNumeroFactura().setText(numeroFormateado);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener el número de orden: " + e.getMessage());
            vfm.getTxtNumeroFactura().setText("000001");
        }
    }
}
