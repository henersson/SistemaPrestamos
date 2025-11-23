package com.prestamos.vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.prestamos.modelo.Prestamo;
import com.prestamos.dao.PrestamoDAO;

/**
 * Panel para la gestión de préstamos del sistema.
 * Módulo central que permite crear, consultar y gestionar préstamos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class PanelPrestamos extends JPanel {

    // Componentes
    private JTable tblPrestamos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnRefrescar;
    private JButton btnRegistrarPago;
    private JButton btnVerHistorial;
    private JButton btnCancelar;
    private JComboBox<String> cmbFiltroEstado;
    private JLabel lblTotal;
    private JLabel lblSumaMontos;

    // DAO
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    // Formato de fecha
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor del panel de préstamos.
     */
    public PanelPrestamos() {
        initComponents();
        cargarDatosTabla();
        configurarEventos();
    }

    /**
     * Inicializa los componentes del panel.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));

        // Panel norte con búsqueda y filtros
        add(crearPanelNorte(), BorderLayout.NORTH);

        // Panel central con tabla
        add(crearPanelCentro(), BorderLayout.CENTER);

        // Panel sur con botones de acción
        add(crearPanelSur(), BorderLayout.SOUTH);
    }

    /**
     * Crea el panel norte con barra de búsqueda y filtros.
     */
    private JPanel crearPanelNorte() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Búsqueda
        JLabel lblBuscar = new JLabel("Buscar (ID/Cliente):");
        lblBuscar.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblBuscar);

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(txtBuscar);

        btnBuscar = crearBoton("Buscar", new Color(52, 152, 219), 100);
        panel.add(btnBuscar);

        panel.add(Box.createHorizontalStrut(20));

        btnRefrescar = crearBoton("Refrescar", new Color(46, 204, 113), 120);
        panel.add(btnRefrescar);

        // Línea 2: Filtros
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        JLabel lblFiltroEstado = new JLabel("Filtrar por Estado:");
        lblFiltroEstado.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblFiltroEstado);

        cmbFiltroEstado = new JComboBox<>(new String[]{
            "TODOS", "ACTIVO", "EN_MORA", "VENCIDO", "CANCELADO", "ARTICULO_TRANSFERIDO"
        });
        cmbFiltroEstado.setPreferredSize(new Dimension(180, 35));
        cmbFiltroEstado.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(cmbFiltroEstado);

        return panel;
    }

    /**
     * Crea el panel central con la tabla de préstamos.
     */
    private JPanel crearPanelCentro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Crear modelo de tabla
        String[] columnas = {
            "ID", "Cliente", "Monto", "Interés",
            "Total Deuda", "F. Préstamo", "F. Vencimiento", "Estado"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Crear tabla
        tblPrestamos = new JTable(modeloTabla);
        tblPrestamos.setFont(new Font("Arial", Font.PLAIN, 13));
        tblPrestamos.setRowHeight(30);
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPrestamos.setGridColor(new Color(189, 195, 199));

        // Configurar anchos de columnas
        tblPrestamos.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tblPrestamos.getColumnModel().getColumn(1).setPreferredWidth(80);   // Cliente
        tblPrestamos.getColumnModel().getColumn(2).setPreferredWidth(100);  // Monto
        tblPrestamos.getColumnModel().getColumn(3).setPreferredWidth(100);  // Interés
        tblPrestamos.getColumnModel().getColumn(4).setPreferredWidth(100);  // Total
        tblPrestamos.getColumnModel().getColumn(5).setPreferredWidth(100);  // F. Préstamo
        tblPrestamos.getColumnModel().getColumn(6).setPreferredWidth(100);  // F. Vencimiento
        tblPrestamos.getColumnModel().getColumn(7).setPreferredWidth(120);  // Estado

        // Renderizador personalizado para estados con colores
        DefaultTableCellRenderer rendererEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 7 && value != null) { // Columna Estado
                    String estado = value.toString();
                    if (!isSelected) {
                        switch (estado) {
                            case "ACTIVO":
                                c.setBackground(new Color(46, 204, 113, 80));
                                c.setForeground(new Color(22, 102, 56));
                                break;
                            case "EN_MORA":
                                c.setBackground(new Color(241, 196, 15, 80));
                                c.setForeground(new Color(120, 98, 7));
                                break;
                            case "VENCIDO":
                                c.setBackground(new Color(231, 76, 60, 80));
                                c.setForeground(new Color(115, 38, 30));
                                break;
                            case "CANCELADO":
                                c.setBackground(new Color(149, 165, 166, 80));
                                c.setForeground(new Color(74, 82, 83));
                                break;
                            case "ARTICULO_TRANSFERIDO":
                                c.setBackground(new Color(155, 89, 182, 80));
                                c.setForeground(new Color(77, 44, 91));
                                break;
                            default:
                                c.setBackground(Color.WHITE);
                                c.setForeground(Color.BLACK);
                        }
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        tblPrestamos.getColumnModel().getColumn(7).setCellRenderer(rendererEstado);

        // Centrar contenido numérico
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 8; i++) {
            tblPrestamos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Estilo del encabezado
        tblPrestamos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tblPrestamos.getTableHeader().setBackground(new Color(52, 73, 94));
        tblPrestamos.getTableHeader().setForeground(Color.BLACK);
        tblPrestamos.getTableHeader().setPreferredSize(new Dimension(0, 35));

        JScrollPane scrollPane = new JScrollPane(tblPrestamos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel sur con botones de acción.
     */
    private JPanel crearPanelSur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setPreferredSize(new Dimension(0, 70));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        btnNuevo = crearBoton("Nuevo Préstamo", new Color(46, 204, 113), 150);
        panel.add(btnNuevo);

        btnRegistrarPago = crearBoton("Registrar Pago", new Color(52, 152, 219), 150);
        panel.add(btnRegistrarPago);

        btnVerHistorial = crearBoton("Ver Historial", new Color(241, 196, 15), 130);
        panel.add(btnVerHistorial);

        btnCancelar = crearBoton("Cancelar", new Color(231, 76, 60), 120);
        panel.add(btnCancelar);

        panel.add(Box.createHorizontalStrut(50));

        lblTotal = new JLabel("Total préstamos: 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(52, 73, 94));
        panel.add(lblTotal);

        panel.add(Box.createHorizontalStrut(20));

        lblSumaMontos = new JLabel("Monto total: $0.00");
        lblSumaMontos.setFont(new Font("Arial", Font.BOLD, 14));
        lblSumaMontos.setForeground(new Color(46, 204, 113));
        panel.add(lblSumaMontos);

        return panel;
    }

    /**
     * Crea un botón con estilo personalizado.
     */
    private JButton crearBoton(String texto, Color color, int ancho) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(ancho, 35));
        return boton;
    }

    /**
     * Configura los eventos de los componentes.
     */
    private void configurarEventos() {
        btnNuevo.addActionListener(e -> mostrarDialogoNuevo());
        btnRegistrarPago.addActionListener(e -> registrarPago());
        btnVerHistorial.addActionListener(e -> verHistorial());
        btnCancelar.addActionListener(e -> cancelarPrestamo());
        btnRefrescar.addActionListener(e -> cargarDatosTabla());
        btnBuscar.addActionListener(e -> buscarPrestamo());
        cmbFiltroEstado.addActionListener(e -> aplicarFiltros());

        // Enter en campo de búsqueda
        txtBuscar.addActionListener(e -> buscarPrestamo());

        // Doble clic en tabla para ver historial
        tblPrestamos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    verHistorial();
                }
            }
        });
    }

    /**
     * Carga los datos de préstamos en la tabla.
     */
    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        List<Prestamo> prestamos = prestamoDAO.listarTodosPrestamos();

        double sumaTotal = 0.0;

        for (Prestamo prestamo : prestamos) {
            double totalDeuda = prestamo.getMonto() + prestamo.getInteresGenerado() + prestamo.getMulta();

            Object[] fila = {
                prestamo.getIdPrestamo(),
                "ID: " + prestamo.getIdCliente(),
                String.format("$%.2f", prestamo.getMonto()),
                String.format("$%.2f", prestamo.getInteresGenerado()),
                String.format("$%.2f", totalDeuda),
                prestamo.getFechaPrestamo() != null ? sdf.format(prestamo.getFechaPrestamo()) : "N/A",
                prestamo.getFechaVencimiento() != null ? sdf.format(prestamo.getFechaVencimiento()) : "N/A",
                prestamo.getEstadoPrestamo()
            };
            modeloTabla.addRow(fila);

            if (!"CANCELADO".equals(prestamo.getEstadoPrestamo())) {
                sumaTotal += totalDeuda;
            }
        }

        lblTotal.setText("Total préstamos: " + prestamos.size());
        lblSumaMontos.setText(String.format("Monto total activo: $%.2f", sumaTotal));
    }

    /**
     * Calcula los días restantes o vencidos de un préstamo.
     */
    private String calcularDias(Date fechaVencimiento) {
        if (fechaVencimiento == null) return "N/A";

        long diferencia = fechaVencimiento.getTime() - System.currentTimeMillis();
        long dias = TimeUnit.MILLISECONDS.toDays(diferencia);

        if (dias > 0) {
            return "+" + dias + "d";
        } else if (dias < 0) {
            return dias + "d";
        } else {
            return "HOY";
        }
    }

    /**
     * Aplica filtros a la tabla de préstamos.
     */
    private void aplicarFiltros() {
        String estadoSeleccionado = (String) cmbFiltroEstado.getSelectedItem();

        modeloTabla.setRowCount(0);
        List<Prestamo> prestamos;

        if ("TODOS".equals(estadoSeleccionado)) {
            prestamos = prestamoDAO.listarTodosPrestamos();
        } else {
            prestamos = prestamoDAO.listarPrestamosPorEstado(estadoSeleccionado);
        }

        double sumaTotal = 0.0;

        for (Prestamo prestamo : prestamos) {
            double totalDeuda = prestamo.getMonto() + prestamo.getInteresGenerado();
            String diasInfo = calcularDias(prestamo.getFechaVencimiento());

            Object[] fila = {
                prestamo.getIdPrestamo(),
                "ID: " + prestamo.getIdCliente(),
                String.format("$%.2f", prestamo.getMonto()),
                String.format("$%.2f", prestamo.getInteresGenerado()),
                String.format("$%.2f", totalDeuda),
                prestamo.getFechaPrestamo() != null ? sdf.format(prestamo.getFechaPrestamo()) : "N/A",
                prestamo.getFechaVencimiento() != null ? sdf.format(prestamo.getFechaVencimiento()) : "N/A",
                prestamo.getEstadoPrestamo()
            };
            modeloTabla.addRow(fila);

            if (!"CANCELADO".equals(prestamo.getEstadoPrestamo())) {
                sumaTotal += totalDeuda;
            }
        }

        lblTotal.setText("Préstamos encontrados: " + prestamos.size());
        lblSumaMontos.setText(String.format("Monto total: $%.2f", sumaTotal));
    }

    /**
     * Busca préstamos por ID o cliente.
     */
    private void buscarPrestamo() {
        String textoBuscar = txtBuscar.getText().trim();

        if (textoBuscar.isEmpty()) {
            cargarDatosTabla();
            return;
        }

        try {
            int idBuscar = Integer.parseInt(textoBuscar);
            Prestamo prestamo = prestamoDAO.obtenerPrestamoPorId(idBuscar);

            modeloTabla.setRowCount(0);

            if (prestamo != null) {
                double totalDeuda = prestamo.getMonto() + prestamo.getInteresGenerado();
                String diasInfo = calcularDias(prestamo.getFechaVencimiento());

                Object[] fila = {
                    prestamo.getIdPrestamo(),
                    "ID: " + prestamo.getIdCliente(),
                    String.format("$%.2f", prestamo.getMonto()),
                    String.format("$%.2f", prestamo.getInteresGenerado()),
                    String.format("$%.2f", totalDeuda),
                    prestamo.getFechaPrestamo() != null ? sdf.format(prestamo.getFechaPrestamo()) : "N/A",
                    prestamo.getFechaVencimiento() != null ? sdf.format(prestamo.getFechaVencimiento()) : "N/A",
                    prestamo.getEstadoPrestamo()
                };
                modeloTabla.addRow(fila);

                lblTotal.setText("Préstamo encontrado: 1");
                lblSumaMontos.setText(String.format("Monto total: $%.2f", totalDeuda));
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se encontró préstamo con ID: " + idBuscar,
                    "Sin Resultados",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor ingrese un ID numérico válido.",
                "Error de Búsqueda",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Muestra el diálogo para crear un nuevo préstamo.
     */
    private void mostrarDialogoNuevo() {
        DialogoPrestamo dialogo = new DialogoPrestamo((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialogo.setVisible(true);

        if (dialogo.isConfirmado()) {
            cargarDatosTabla();
        }
    }

    /**
     * Registra un pago para el préstamo seleccionado.
     */
    private void registrarPago() {
        int filaSeleccionada = tblPrestamos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un préstamo de la tabla.",
                "Sin Selección",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String totalDeudaStr = (String) modeloTabla.getValueAt(filaSeleccionada, 5);

        String input = JOptionPane.showInputDialog(this,
            "Deuda total: " + totalDeudaStr + "\n\nIngrese el monto del pago:",
            "Registrar Pago",
            JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double montoPago = Double.parseDouble(input.trim());

                if (montoPago <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "El monto debe ser mayor a cero.",
                        "Validación",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (prestamoDAO.registrarPago(idPrestamo, montoPago)) {
                    JOptionPane.showMessageDialog(this,
                        "Pago registrado exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarDatosTabla();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "No se pudo registrar el pago.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Monto inválido. Ingrese un número válido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra el historial del préstamo seleccionado.
     */
    private void verHistorial() {
        int filaSeleccionada = tblPrestamos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un préstamo de la tabla.",
                "Sin Selección",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        Prestamo prestamo = prestamoDAO.obtenerPrestamoPorId(idPrestamo);

        if (prestamo != null) {
            mostrarDetallesPrestamo(prestamo);
        }
    }

    /**
     * Muestra los detalles completos de un préstamo.
     */
    private void mostrarDetallesPrestamo(Prestamo prestamo) {
        double totalDeuda = prestamo.getMonto() + prestamo.getInteresGenerado();

        StringBuilder detalle = new StringBuilder();
        detalle.append("═══════════════════════════════���═══════\n");
        detalle.append("        DETALLES DEL PRÉSTAMO\n");
        detalle.append("═══════════════════════════════════════\n\n");
        detalle.append("ID Préstamo: ").append(prestamo.getIdPrestamo()).append("\n");
        detalle.append("ID Cliente: ").append(prestamo.getIdCliente()).append("\n");
        detalle.append("ID Artículo: ").append(prestamo.getIdArticulo()).append("\n");
        detalle.append("ID Asesor: ").append(prestamo.getIdAsesor()).append("\n\n");
        detalle.append("Monto Prestado: $").append(String.format("%.2f", prestamo.getMonto())).append("\n");
        detalle.append("Tasa Interés: ").append(String.format("%.2f", prestamo.getTasaInteres())).append("%\n");
        detalle.append("Interés Generado: $").append(String.format("%.2f", prestamo.getInteresGenerado())).append("\n");
        detalle.append("TOTAL DEUDA: $").append(String.format("%.2f", totalDeuda)).append("\n\n");
        detalle.append("Fecha Préstamo: ").append(prestamo.getFechaPrestamo() != null ? sdf.format(prestamo.getFechaPrestamo()) : "N/A").append("\n");
        detalle.append("Fecha Vencimiento: ").append(prestamo.getFechaVencimiento() != null ? sdf.format(prestamo.getFechaVencimiento()) : "N/A").append("\n");
        detalle.append("Estado: ").append(prestamo.getEstadoPrestamo()).append("\n");
        detalle.append("═══════════════════════════════���═══════\n");

        JTextArea textArea = new JTextArea(detalle.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 350));

        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Historial del Préstamo",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Cancela el préstamo seleccionado.
     */
    private void cancelarPrestamo() {
        int filaSeleccionada = tblPrestamos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un préstamo de la tabla.",
                "Sin Selección",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String estado = (String) modeloTabla.getValueAt(filaSeleccionada, 7);

        if ("CANCELADO".equals(estado)) {
            JOptionPane.showMessageDialog(this,
                "El préstamo ya está cancelado.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de cancelar el préstamo ID " + idPrestamo + "?",
            "Confirmar Cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (prestamoDAO.cancelarPrestamo(idPrestamo)) {
                JOptionPane.showMessageDialog(this,
                    "Préstamo cancelado exitosamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo cancelar el préstamo.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

