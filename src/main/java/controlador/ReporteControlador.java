package controlador;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import vista.VistaFacturaMecanica;

import vista.VistaReportes; // Ajusta el nombre de tu vista si es diferente

public class ReporteControlador {

    private VistaReportes vista;
    private ArrayList<String[]> listaMarcas;
    private Connection conectado;

    public ReporteControlador(VistaReportes vista) {
        this.vista = vista;

        ConexionBDD conectar = new ConexionBDD();
        this.conectado = (Connection) conectar.conectar();

        this.listaMarcas = obtenerMarcas();
        cargarMarcasInCombo();

        // Evento para generar el PDF al dar clic en el botón principal
        this.vista.getBtnGenerar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarPDF();
            }
        });

        // ---------------------------------------------------------------------
        // OPCIÓN 1: LISTENERS AUTOMÁTICOS PARA PREVISUALIZACIÓN EN TIEMPO REAL
        // ---------------------------------------------------------------------
        
        // 1. Escuchar cuando el usuario cambia la marca seleccionada
        this.vista.getCmbMarca().addActionListener(e -> actualizarPrevisualizacion());

        // 2. Escuchar cuando el usuario edita las fechas en las cajas de texto
        DocumentListener fechaListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarPrevisualizacion(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarPrevisualizacion(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarPrevisualizacion(); }
        };

        this.vista.getTxtFechaInicio().getDocument().addDocumentListener(fechaListener);
        this.vista.getTxtFechaFin().getDocument().addDocumentListener(fechaListener);

        // Renderizar la vista previa inicial al abrir la pantalla
        actualizarPrevisualizacion();
    }

    public ArrayList<String[]> obtenerMarcas() {
        ArrayList<String[]> lregistros = new ArrayList<>();
        String sentenciaSQL = "{CALL sp_obtener_marcas()}";

        try (CallableStatement ejecutar = conectado.prepareCall(sentenciaSQL); ResultSet res = ejecutar.executeQuery()) {

            while (res.next()) {
                String[] marca = new String[2];
                marca[0] = String.valueOf(res.getInt("ID_marca"));
                marca[1] = res.getString("nombre");
                lregistros.add(marca);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener marcas: " + e.getMessage());
        }

        return lregistros;
    }

    private void cargarMarcasInCombo() {
        this.vista.getCmbMarca().removeAllItems();
        this.vista.getCmbMarca().addItem("-- TODAS LAS MARCAS --");

        if (listaMarcas != null && !listaMarcas.isEmpty()) {
            for (String[] marca : listaMarcas) {
                this.vista.getCmbMarca().addItem(marca[1]);
            }
        }
    }

    public void actualizarPrevisualizacion() {
    String fechaInicio = this.vista.getTxtFechaInicio().getText().trim();
    String fechaFin = this.vista.getTxtFechaFin().getText().trim();

    DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
    DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();

    // 1. Cargar datos del gráfico de Repuestos
    int index = this.vista.getCmbMarca().getSelectedIndex();
    Integer idMarca = (index > 0 && listaMarcas != null) ? Integer.parseInt(listaMarcas.get(index - 1)[0]) : null;

    String sqlRepuestos = "{CALL sp_reporte_ventas_repuestos_por_marca(?)}";
    try (CallableStatement ejecutar1 = conectado.prepareCall(sqlRepuestos)) {
        if (idMarca == null) {
            ejecutar1.setNull(1, Types.INTEGER);
        } else {
            ejecutar1.setInt(1, idMarca);
        }

        try (ResultSet res1 = ejecutar1.executeQuery()) {
            while (res1.next()) {
                dataset1.addValue(res1.getDouble("total_recaudado"), "Ventas ($)", res1.getString("repuesto"));
            }
        }
    } catch (SQLException e) {
        System.out.println("Error preview repuestos: " + e.getMessage());
    }

    // 2. Cargar datos del gráfico de Clientes (solo si las fechas no están vacías)
    if (!fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
        String sqlClientes = "{CALL sp_reporte_top_clientes_por_fecha(?, ?)}";
        try (CallableStatement ejecutar2 = conectado.prepareCall(sqlClientes)) {
            ejecutar2.setDate(1, java.sql.Date.valueOf(fechaInicio));
            ejecutar2.setDate(2, java.sql.Date.valueOf(fechaFin));

            try (ResultSet res2 = ejecutar2.executeQuery()) {
                while (res2.next()) {
                    dataset2.addValue(res2.getInt("total_ordenes"), "Órdenes", res2.getString("cliente"));
                }
            }
        } catch (Exception e) {
            // Silencioso mientras el usuario está escribiendo la fecha
        }
    }

    // 3. Crear gráficos con JFreeChart
    JFreeChart chart1 = ChartFactory.createBarChart("Ventas por Repuesto", "Repuesto", "Total ($)", dataset1, PlotOrientation.VERTICAL, false, true, false);
    JFreeChart chart2 = ChartFactory.createBarChart("Top Clientes con Más Órdenes", "Cliente", "Órdenes", dataset2, PlotOrientation.VERTICAL, false, true, false);

    TextTitle subtituloFechas = new TextTitle("Periodo: " + fechaInicio + " a " + fechaFin);
    subtituloFechas.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
    chart2.addSubtitle(subtituloFechas);

    // 4. Configurar el tamaño de los gráficos y agregarlos con FlowLayout
    ChartPanel chartPanel1 = new ChartPanel(chart1);
    ChartPanel chartPanel2 = new ChartPanel(chart2);

    // Control de dimensiones para evitar el estiramiento gigantesco
    chartPanel1.setPreferredSize(new java.awt.Dimension(450, 220));
    chartPanel2.setPreferredSize(new java.awt.Dimension(450, 220));

    this.vista.getPanelGrafico1().removeAll();
    this.vista.getPanelGrafico1().setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
    this.vista.getPanelGrafico1().add(chartPanel1);
    this.vista.getPanelGrafico1().revalidate();
    this.vista.getPanelGrafico1().repaint();

    this.vista.getPanelGrafico2().removeAll();
    this.vista.getPanelGrafico2().setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
    this.vista.getPanelGrafico2().add(chartPanel2);
    this.vista.getPanelGrafico2().revalidate();
    this.vista.getPanelGrafico2().repaint();
}

    private void generarPDF() {
        int index = this.vista.getCmbMarca().getSelectedIndex();
        Integer idMarca = null;
        String nombreMarcaSeleccionada = "TODAS LAS MARCAS";

        if (index > 0 && listaMarcas != null) {
            String[] marcaSeleccionada = listaMarcas.get(index - 1);
            idMarca = Integer.parseInt(marcaSeleccionada[0]);
            nombreMarcaSeleccionada = marcaSeleccionada[1];
        }

        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();

        String rutaCarpeta = "C:\\Users\\Asus\\Documents\\Reportes PDF";
        java.io.File directorio = new java.io.File(rutaCarpeta);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        String rutaArchivo = rutaCarpeta + "\\Reporte_Ventas_Repuestos.pdf";

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subTituloFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            Paragraph titulo = new Paragraph("REPORTE DE VENTAS DE REPUESTOS Y CLIENTES", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph marcaInfo = new Paragraph("Filtro Aplicado: Marca - " + nombreMarcaSeleccionada, subTituloFont);
            marcaInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(marcaInfo);
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(100);

            tabla.addCell(new PdfPCell(new Phrase("Repuesto", subTituloFont)));
            tabla.addCell(new PdfPCell(new Phrase("Unidades Vendidas", subTituloFont)));
            tabla.addCell(new PdfPCell(new Phrase("Total Recaudado ($)", subTituloFont)));

            // PASO 1: CARGAR TABLA Y DATASET 1
            String sqlRepuestos = "{CALL sp_reporte_ventas_repuestos_por_marca(?)}";
            try (CallableStatement ejecutar1 = conectado.prepareCall(sqlRepuestos)) {

                if (idMarca == null) {
                    ejecutar1.setNull(1, Types.INTEGER);
                } else {
                    ejecutar1.setInt(1, idMarca);
                }

                try (ResultSet res1 = ejecutar1.executeQuery()) {
                    while (res1.next()) {
                        String repuesto = res1.getString("repuesto");
                        int unidades = res1.getInt("unidades_vendidas");
                        double totalRecaudado = res1.getDouble("total_recaudado");

                        dataset1.addValue(totalRecaudado, "Ventas ($)", repuesto);

                        tabla.addCell(new Phrase(repuesto, normalFont));
                        tabla.addCell(new Phrase(String.valueOf(unidades), normalFont));
                        tabla.addCell(new Phrase(String.format("$%.2f", totalRecaudado), normalFont));
                    }
                }
            } catch (SQLException e1) {
                System.out.println("Error en SP Repuestos: " + e1.getMessage());
            }

            // PASO 2: CARGAR DATASET 2
            String fechaInicio = this.vista.getTxtFechaInicio().getText().trim();
            String fechaFin = this.vista.getTxtFechaFin().getText().trim();

            String sqlClientes = "{CALL sp_reporte_top_clientes_por_fecha(?, ?)}";

            try (CallableStatement ejecutar2 = conectado.prepareCall(sqlClientes)) {
                ejecutar2.setDate(1, java.sql.Date.valueOf(fechaInicio));
                ejecutar2.setDate(2, java.sql.Date.valueOf(fechaFin));

                try (ResultSet res2 = ejecutar2.executeQuery()) {
                    dataset2.clear();

                    while (res2.next()) {
                        String cliente = res2.getString("cliente");
                        int totalOrdenes = res2.getInt("total_ordenes");

                        dataset2.addValue(totalOrdenes, "Órdenes", cliente);
                    }
                }
            } catch (IllegalArgumentException exFechas) {
                JOptionPane.showMessageDialog(vista, "Formato de fecha inválido. Usa el formato YYYY-MM-DD (Ej: 2026-08-01).");
                return;
            } catch (SQLException e2) {
                System.out.println("Error en SP Clientes por Fecha: " + e2.getMessage());
            }

            // PASO 3: DIBUJAR Y RENDERIZAR GRÁFICO 1
            JFreeChart chart1 = ChartFactory.createBarChart(
                    "Ventas por Repuesto",
                    "Repuesto",
                    "Total Recaudado ($)",
                    dataset1,
                    PlotOrientation.VERTICAL,
                    false, true, false
            );

            BufferedImage img1 = chart1.createBufferedImage(500, 240);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ImageIO.write(img1, "png", baos1);
            Image imagenGrafico1 = Image.getInstance(baos1.toByteArray());
            imagenGrafico1.setAlignment(Element.ALIGN_CENTER);

            // PASO 4: DIBUJAR Y RENDERIZAR GRÁFICO 2 CON SUBTÍTULO
            JFreeChart chart2 = ChartFactory.createBarChart(
                    "Top Clientes con Más Órdenes de Trabajo",
                    "Cliente",
                    "Número de Órdenes",
                    dataset2,
                    PlotOrientation.VERTICAL,
                    false, true, false
            );

            TextTitle subtituloFechas = new TextTitle("Periodo: " + fechaInicio + " a " + fechaFin);
            subtituloFechas.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
            chart2.addSubtitle(subtituloFechas);

            BufferedImage img2 = chart2.createBufferedImage(500, 240);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            ImageIO.write(img2, "png", baos2);
            Image imagenGrafico2 = Image.getInstance(baos2.toByteArray());
            imagenGrafico2.setAlignment(Element.ALIGN_CENTER);

            // PASO 5: ARMAR PDF
            document.add(tabla);
            document.add(new Paragraph(" "));
            document.add(imagenGrafico1);
            document.add(new Paragraph(" "));
            document.add(imagenGrafico2);

            document.close();

            JOptionPane.showMessageDialog(null, "¡PDF generado correctamente en:\n" + rutaArchivo);

            try {
                java.io.File file = new java.io.File(rutaArchivo);
                if (file.exists()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                System.out.println("Error al abrir PDF automáticamente: " + ex.getMessage());
            }

            if (this.vista != null) {
                this.vista.dispose();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar el reporte en PDF: " + e.getMessage());
        }
    }
}
