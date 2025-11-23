package com.prestamos.vista;

import javax.swing.*;
import javax.swing.SwingWorker;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.prestamos.dao.ReportesDAO;

/**
 * Panel para la generación y exportación de reportes del sistema.
 * Permite visualizar y exportar diferentes tipos de reportes.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class PanelReportes extends JPanel {

    // Componentes
    private JList<String> listReportes;
    private JTextArea txtReporte;
    private JButton btnGenerar;
    private JButton btnExportar;
    private JButton btnLimpiar;

    // DAO
    private ReportesDAO reportesDAO = new ReportesDAO();

    /**
     * Constructor del panel de reportes.
     */
    public PanelReportes() {
        initComponents();
        configurarEventos();
    }

    /**
     * Inicializa los componentes del panel.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));

        // Panel oeste con lista de reportes
        add(crearPanelOeste(), BorderLayout.WEST);

        // Panel centro con área de texto
        add(crearPanelCentro(), BorderLayout.CENTER);

        // Panel sur con botones
        add(crearPanelSur(), BorderLayout.SOUTH);
    }

    /**
     * Crea el panel oeste con la lista de reportes disponibles.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelOeste() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Título
        JLabel lblTitulo = new JLabel("Reportes Disponibles");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Lista de reportes
        String[] reportes = {
            "Préstamos Vencidos",
            "Clientes VIP",
            "Inventario de Artículos",
            "Rendimiento de Asesores",
            "Estado Financiero"
        };

        listReportes = new JList<>(reportes);
        listReportes.setFont(new Font("Arial", Font.PLAIN, 14));
        listReportes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listReportes.setBackground(Color.WHITE);
        listReportes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollLista = new JScrollPane(listReportes);
        scrollLista.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollLista, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel central con el área de texto para mostrar reportes.
     *
     * @return JScrollPane con el área de texto configurada
     */
    private JScrollPane crearPanelCentro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Vista Previa del Reporte");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Área de texto para el reporte
        txtReporte = new JTextArea();
        txtReporte.setEditable(false);
        txtReporte.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtReporte.setBackground(new Color(245, 245, 245));
        txtReporte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtReporte.setLineWrap(false);
        txtReporte.setWrapStyleWord(false);
        txtReporte.setText("Seleccione un reporte de la lista y presione 'Generar Reporte' para visualizarlo.");

        JScrollPane scrollReporte = new JScrollPane(txtReporte);
        scrollReporte.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollReporte, BorderLayout.CENTER);

        JScrollPane scrollPanel = new JScrollPane(panel);
        scrollPanel.setBorder(null);
        return scrollPanel;
    }

    /**
     * Crea el panel sur con los botones de acción.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelSur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));

        // Botón Generar
        btnGenerar = new JButton("Generar Reporte");
        btnGenerar.setFont(new Font("Arial", Font.BOLD, 14));
        btnGenerar.setBackground(new Color(52, 152, 219));
        btnGenerar.setForeground(Color.WHITE);
        btnGenerar.setFocusPainted(false);
        btnGenerar.setBorderPainted(false);
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerar.setPreferredSize(new Dimension(180, 40));
        panel.add(btnGenerar);

        // Botón Exportar
        btnExportar = new JButton("Exportar a TXT");
        btnExportar.setFont(new Font("Arial", Font.BOLD, 14));
        btnExportar.setBackground(new Color(46, 204, 113));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFocusPainted(false);
        btnExportar.setBorderPainted(false);
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportar.setPreferredSize(new Dimension(180, 40));
        panel.add(btnExportar);

        // Botón Limpiar
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimpiar.setBackground(new Color(127, 140, 141));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.setPreferredSize(new Dimension(140, 40));
        panel.add(btnLimpiar);

        return panel;
    }

    /**
     * Genera el reporte seleccionado y lo muestra en el área de texto.
     */
    private void generarReporte() {
        String seleccionado = listReportes.getSelectedValue();

        if (seleccionado == null) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, seleccione un reporte de la lista",
                "Reporte no seleccionado",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        System.out.println("\n→ Generando reporte: " + seleccionado);

        // Mostrar indicador de carga
        txtReporte.setText("Generando reporte, por favor espere...");
        txtReporte.setEnabled(false);
        btnGenerar.setEnabled(false);

        // Usar SwingWorker para no bloquear la interfaz
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String reporte = "";

                switch (seleccionado) {
                    case "Préstamos Vencidos":
                        reporte = reportesDAO.generarReportePrestamosVencidos();
                        break;

                    case "Clientes VIP":
                        reporte = reportesDAO.generarReporteClientesVIP();
                        break;

                    case "Inventario de Artículos":
                        reporte = reportesDAO.generarReporteInventarioArticulos();
                        break;

                    case "Rendimiento de Asesores":
                    case "Estado Financiero":
                        reporte = "===============================================================\n" +
                                 "              REPORTE NO IMPLEMENTADO AUN                  \n" +
                                 "===============================================================\n\n" +
                                 "El reporte '" + seleccionado + "' estara disponible proximamente.\n\n" +
                                 "Reportes implementados:\n" +
                                 "  - Prestamos Vencidos\n" +
                                 "  - Clientes VIP\n" +
                                 "  - Inventario de Articulos\n\n" +
                                 "Desarrollado por:\n" +
                                 "  - Henersson Cobo\n";
                        break;

                    default:
                        reporte = "Reporte no implementado";
                        break;
                }

                return reporte;
            }

            @Override
            protected void done() {
                try {
                    String reporte = get();
                    txtReporte.setText(reporte);
                    txtReporte.setCaretPosition(0); // Scroll arriba
                    System.out.println("✓ Reporte generado exitosamente: " + seleccionado);
                } catch (Exception e) {
                    System.err.println("✗ Error al generar reporte: " + e.getMessage());
                    txtReporte.setText("ERROR: No se pudo generar el reporte\n\n" + e.getMessage());
                    e.printStackTrace();
                } finally {
                    txtReporte.setEnabled(true);
                    btnGenerar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    /**
     * Exporta el contenido del reporte a un archivo de texto.
     */
    private void exportarReporte() {
        String contenido = txtReporte.getText();

        if (contenido.isEmpty() || contenido.equals("Seleccione un reporte de la lista y presione 'Generar Reporte' para visualizarlo.")) {
            JOptionPane.showMessageDialog(
                this,
                "No hay ningún reporte generado para exportar.\n" +
                "Primero genere un reporte.",
                "Reporte vacío",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Crear JFileChooser para seleccionar ubicación
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte como...");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Sugerir nombre de archivo basado en fecha y reporte seleccionado
        String nombreSugerido = "reporte_";
        String seleccionado = listReportes.getSelectedValue();
        if (seleccionado != null) {
            nombreSugerido += seleccionado.toLowerCase()
                .replace(" ", "_")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u") + "_";
        }
        nombreSugerido += new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";

        fileChooser.setSelectedFile(new File(nombreSugerido));

        int resultado = fileChooser.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();

            // Asegurar extensión .txt
            if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            }

            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(contenido);

                System.out.println("✓ Reporte exportado: " + archivo.getAbsolutePath());

                JOptionPane.showMessageDialog(
                    this,
                    "Reporte exportado exitosamente a:\n" + archivo.getAbsolutePath(),
                    "Exportación exitosa",
                    JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IOException e) {
                System.err.println("✗ Error al exportar reporte: " + e.getMessage());

                JOptionPane.showMessageDialog(
                    this,
                    "Error al exportar el reporte:\n" + e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Configura todos los eventos de los componentes del panel.
     */
    private void configurarEventos() {
        // Botón Generar
        btnGenerar.addActionListener(e -> generarReporte());

        // Botón Exportar
        btnExportar.addActionListener(e -> exportarReporte());

        // Botón Limpiar
        btnLimpiar.addActionListener(e -> {
            txtReporte.setText("Seleccione un reporte de la lista y presione 'Generar Reporte' para visualizarlo.");
            listReportes.clearSelection();
            System.out.println("→ Vista de reporte limpiada");
        });

        // Double-click en la lista para generar reporte
        listReportes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    generarReporte();
                }
            }
        });
    }
}
