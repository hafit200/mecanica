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
import javax.swing.JOptionPane;
import vista.MenuLogin;
import vista.MenuMecanico;
import vista.VistaFacturaMecanica;
import vista.VistaMecanico;

/**
 *
 * @author Asus
 */
public class LoginControlador {

    private MenuLogin vl;
    private ConexionBDD conectar = new ConexionBDD();
    private Connection conectado = (Connection) conectar.conectar();

    public LoginControlador(MenuLogin vl) {
        this.vl = vl;
        initListeners();
    }

    private void initListeners() {
        this.vl.getBtnIniciar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validarIngreso();
            }
        });
    }

    private void validarIngreso() {
    String usuario = vl.getTxtUsuario().getText().trim();
    String clave = new String(vl.getPswContraseña().getPassword()).trim();

    if (usuario.isEmpty() || clave.isEmpty()) {
        JOptionPane.showMessageDialog(vl, "Por favor ingrese usuario y contraseña.", "Atención", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String rolUsuario = autenticarUsuarioSP(usuario, clave);

    if (rolUsuario != null) {
        JOptionPane.showMessageDialog(vl, "¡Bienvenido! Rol: " + rolUsuario, "Acceso Concedido", JOptionPane.INFORMATION_MESSAGE);

        if (rolUsuario.equalsIgnoreCase("RECEPCIONISTA")) {
            VistaFacturaMecanica vfm = new VistaFacturaMecanica();

            // Instancia de TODOS los controladores para la factura
            FacturaControlador fc = new FacturaControlador(vfm);
            VehiculoControlador vc = new VehiculoControlador(vfm);
            ClienteControlador cc = new ClienteControlador(vfm, vc);
            DetalleFacturaControlador dfc = new DetalleFacturaControlador(vfm);
            RepuestoControlador rc = new RepuestoControlador(vfm);
            MarcaControlador mac = new MarcaControlador(vfm, rc);

            fc.cargarFecha();

            vfm.setLocationRelativeTo(null);
            vfm.setVisible(true);

        } else if (rolUsuario.equalsIgnoreCase("MECANICO")) {
            MenuMecanico mm = new MenuMecanico();
            HistorialTrabajosControlador htc = new HistorialTrabajosControlador(mm);
            mm.setLocationRelativeTo(null);
            mm.setVisible(true);
        }

        vl.dispose();
    } else {
        JOptionPane.showMessageDialog(vl, "Usuario/Contraseña incorrectos o usuario INACTIVO.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
    }
}

    public String autenticarUsuarioSP(String usuario, String clave) {
        String sql = "{CALL sp_validar_usuario(?, ?)}";

        try (CallableStatement cs = conectado.prepareCall(sql)) {
            cs.setString(1, usuario);
            cs.setString(2, clave);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("rol");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar sp_validar_usuario: " + e.getMessage());
        }

        return null;
    }
}