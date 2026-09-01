/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;

import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PdfGenerador {

    public void generarPdfFactura(int idOrden, String placa, String servicio, JTable tablaRepuestos, double totalFinalConRecargo) {
        String carpetaDestino = "C:\\Users\\Asus\\Documents\\PdfFacturas";
        File directorio = new File(carpetaDestino);

        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String rutaDestino = carpetaDestino + "\\Factura_Orden_" + idOrden + ".pdf";
        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(rutaDestino));
            documento.open();

            // Estilos de tipografía
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
            Font textoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font cabeceraTablaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

            // 1. Encabezado Título
            Paragraph titulo = new Paragraph("ORDEN DE TRABAJO Y FACTURA", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph numOrden = new Paragraph("N° Orden: " + String.format("%06d", idOrden), subtituloFont);
            numOrden.setAlignment(Element.ALIGN_CENTER);
            documento.add(numOrden);
            documento.add(new Paragraph(" "));

            // 2. Información General
            PdfPTable tablaInfo = new PdfPTable(2);
            tablaInfo.setWidthPercentage(100);

            tablaInfo.addCell(crearCelda("Placa: " + placa, textoFont, false));
            tablaInfo.addCell(crearCelda("Servicio: " + servicio, textoFont, false));

            documento.add(tablaInfo);
            documento.add(new Paragraph(" "));

            // 3. Tabla de Repuestos
            PdfPTable tablaDetalle = new PdfPTable(4);
            tablaDetalle.setWidthPercentage(100);
            tablaDetalle.setWidths(new float[]{12f, 48f, 20f, 20f});

            String[] columnas = {"Cant.", "Descripción / Repuesto", "P. Unitario", "Subtotal"};
            for (String col : columnas) {
                PdfPCell cell = new PdfPCell(new Phrase(col, cabeceraTablaFont));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaDetalle.addCell(cell);
            }

            double subtotalGeneral = 0.0;
            DefaultTableModel model = (DefaultTableModel) tablaRepuestos.getModel();

            for (int i = 0; i < model.getRowCount(); i++) {
                String desc = obtenerTextoCelda(model.getValueAt(i, 0));
                String precioStr = obtenerTextoCelda(model.getValueAt(i, 1));
                String cantStr = obtenerTextoCelda(model.getValueAt(i, 2));
                String subtotalStr = obtenerTextoCelda(model.getValueAt(i, 3));

                double precioUnitario = limpiarNumero(precioStr);
                double subtotal = limpiarNumero(subtotalStr);
                subtotalGeneral += subtotal;

                PdfPCell cCant = new PdfPCell(new Phrase(cantStr, textoFont));
                cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaDetalle.addCell(cCant);

                tablaDetalle.addCell(new Phrase(desc, textoFont));

                PdfPCell cPrecio = new PdfPCell(new Phrase("$ " + String.format("%.2f", precioUnitario), textoFont));
                cPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tablaDetalle.addCell(cPrecio);

                PdfPCell cSub = new PdfPCell(new Phrase("$ " + String.format("%.2f", subtotal), textoFont));
                cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tablaDetalle.addCell(cSub);
            }

            documento.add(tablaDetalle);
            documento.add(new Paragraph(" "));

            // 4. Seccion Totales (Subtotal, Recargo y Total Final)
            double valorRecargo = totalFinalConRecargo - subtotalGeneral;

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(50);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);

            // Subtotal
            tablaTotales.addCell(crearCelda("Subtotal:", subtituloFont, false));
            tablaTotales.addCell(crearCeldaDer("$ " + String.format("%.2f", subtotalGeneral), textoFont));

            // Recargo (solo si existe diferencia superior a 1 centavo)
            if (valorRecargo > 0.01) {
                tablaTotales.addCell(crearCelda("Recargo (Pesado):", subtituloFont, false));
                tablaTotales.addCell(crearCeldaDer("$ " + String.format("%.2f", valorRecargo), textoFont));
            }

            // Total Final
            tablaTotales.addCell(crearCelda("TOTAL:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK), false));
            tablaTotales.addCell(crearCeldaDer("$ " + String.format("%.2f", totalFinalConRecargo), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));

            documento.add(tablaTotales);

            documento.close();
            JOptionPane.showMessageDialog(null, "PDF Generado exitosamente en:\n" + rutaDestino, "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String obtenerTextoCelda(Object valor) {
        return valor != null ? valor.toString().trim() : "";
    }

    private double limpiarNumero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String limpio = texto.replaceAll("[^0-9.,]", "").replace(",", ".");
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private PdfPCell crearCelda(String contenido, Font fuente, boolean conBorde) {
        PdfPCell celda = new PdfPCell(new Phrase(contenido, fuente));
        if (!conBorde) {
            celda.setBorder(PdfPCell.NO_BORDER);
        }
        celda.setPadding(3);
        return celda;
    }

    private PdfPCell crearCeldaDer(String contenido, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(contenido, fuente));
        celda.setBorder(PdfPCell.NO_BORDER);
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celda.setPadding(3);
        return celda;
    }
}