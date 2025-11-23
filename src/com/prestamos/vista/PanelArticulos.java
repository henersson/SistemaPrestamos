package com.prestamos.vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import com.prestamos.modelo.Articulo;
import com.prestamos.dao.ArticuloDAO;

/**
 * Panel para la gestión de artículos del sistema de préstamos.
 * Permite listar, buscar, agregar, modificar y eliminar artículos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class PanelArticulos extends JPanel {

    // Componentes
    private JTable tblArticulos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnRefrescar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JComboBox<String> cmbFiltroEstado;
    private JComboBox<String> cmbFiltroTipo;
    private JLabel lblTotal;

    // DAO
    private ArticuloDAO articuloDAO = new ArticuloDAO();

    // Formato de fecha
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor del panel de artículos.
     */
    public PanelArticulos() {
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
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelNorte() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Línea 1: Búsqueda
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblBuscar);

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(250, 35));
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(txtBuscar);

        btnBuscar = crearBoton("Buscar", new Color(52, 152, 219), 100);
        panel.add(btnBuscar);

        panel.add(Box.createHorizontalStrut(20));

        btnRefrescar = crearBoton("Refrescar", new Color(46, 204, 113), 120);
        panel.add(btnRefrescar);

        // Separador de línea
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        // Línea 2: Filtros
        JLabel lblFiltroEstado = new JLabel("Estado:");
        lblFiltroEstado.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblFiltroEstado);

        cmbFiltroEstado = new JComboBox<>(new String[]{
            "TODOS", "OPTIMO", "FUNCIONABLE", "DEFECTUOSO", "PROPIEDAD_CASA"
        });
        cmbFiltroEstado.setPreferredSize(new Dimension(150, 35));
        cmbFiltroEstado.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(cmbFiltroEstado);

        panel.add(Box.createHorizontalStrut(10));

        JLabel lblFiltroTipo = new JLabel("Tipo:");
        lblFiltroTipo.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblFiltroTipo);

        cmbFiltroTipo = new JComboBox<>(new String[]{
            "TODOS", "ELECTRODOMESTICO", "JOYA", "HERRAMIENTA", "VEHICULO", "ELECTRONICO", "OTRO"
        });
        cmbFiltroTipo.setPreferredSize(new Dimension(180, 35));
        cmbFiltroTipo.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(cmbFiltroTipo);

        return panel;
    }

    /**
     * Crea el panel central con la tabla de artículos.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelCentro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Crear modelo de tabla
        String[] columnas = {
            "ID", "Tipo", "Descripción", "Estado", "Valor Tasado",
            "Fecha Avalúo", "% Tasación", "Precio Mercado"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Crear tabla
        tblArticulos = new JTable(modeloTabla);
        tblArticulos.setFont(new Font("Arial", Font.PLAIN, 13));
        tblArticulos.setRowHeight(30);
        tblArticulos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblArticulos.setGridColor(new Color(189, 195, 199));

        // Configurar anchos de columnas
        tblArticulos.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tblArticulos.getColumnModel().getColumn(1).setPreferredWidth(120); // Tipo
        tblArticulos.getColumnModel().getColumn(2).setPreferredWidth(250); // Descripción
        tblArticulos.getColumnModel().getColumn(3).setPreferredWidth(100); // Estado
        tblArticulos.getColumnModel().getColumn(4).setPreferredWidth(100); // Valor
        tblArticulos.getColumnModel().getColumn(5).setPreferredWidth(100); // Fecha
        tblArticulos.getColumnModel().getColumn(6).setPreferredWidth(80);  // %
        tblArticulos.getColumnModel().getColumn(7).setPreferredWidth(100); // Precio

        // Renderizador personalizado para estados con colores
        DefaultTableCellRenderer rendererEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 3 && value != null) { // Columna Estado
                    String estado = value.toString();
                    if (!isSelected) {
                        switch (estado) {
                            case "OPTIMO":
                                c.setBackground(new Color(46, 204, 113, 50));
                                break;
                            case "FUNCIONABLE":
                                c.setBackground(new Color(241, 196, 15, 50));
                                break;
                            case "DEFECTUOSO":
                                c.setBackground(new Color(231, 76, 60, 50));
                                break;
                            case "PROPIEDAD_CASA":
                                c.setBackground(new Color(155, 89, 182, 50));
                                break;
                            default:
                                c.setBackground(Color.WHITE);
                        }
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        tblArticulos.getColumnModel().getColumn(3).setCellRenderer(rendererEstado);

        // Centrar contenido numérico
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblArticulos.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblArticulos.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblArticulos.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblArticulos.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tblArticulos.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Estilo del encabezado
        tblArticulos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tblArticulos.getTableHeader().setBackground(new Color(52, 73, 94));
        tblArticulos.getTableHeader().setForeground(Color.WHITE);
        tblArticulos.getTableHeader().setPreferredSize(new Dimension(0, 35));

        JScrollPane scrollPane = new JScrollPane(tblArticulos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel sur con botones de acción.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelSur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setPreferredSize(new Dimension(0, 70));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        btnNuevo = crearBoton("Nuevo Artículo", new Color(46, 204, 113), 150);
        panel.add(btnNuevo);

        btnEditar = crearBoton("Editar", new Color(241, 196, 15), 120);
        panel.add(btnEditar);

        btnEliminar = crearBoton("Eliminar", new Color(231, 76, 60), 120);
        panel.add(btnEliminar);

        panel.add(Box.createHorizontalStrut(50));

        lblTotal = new JLabel("Total de artículos: 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(52, 73, 94));
        panel.add(lblTotal);

        return panel;
    }

    /**
     * Crea un botón con estilo personalizado.
     *
     * @param texto texto del botón
     * @param color color de fondo
     * @param ancho ancho del botón
     * @return JButton configurado
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
        btnEditar.addActionListener(e -> mostrarDialogoEditar());
        btnEliminar.addActionListener(e -> eliminarArticulo());
        btnRefrescar.addActionListener(e -> cargarDatosTabla());
        btnBuscar.addActionListener(e -> buscarArticulo());
        cmbFiltroEstado.addActionListener(e -> aplicarFiltros());
        cmbFiltroTipo.addActionListener(e -> aplicarFiltros());

        // Enter en campo de búsqueda
        txtBuscar.addActionListener(e -> buscarArticulo());

        // Doble clic en tabla para editar
        tblArticulos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    mostrarDialogoEditar();
                }
            }
        });
    }

    /**
     * Carga los datos de artículos en la tabla.
     */
    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        List<Articulo> articulos = articuloDAO.listarTodosArticulos();

        for (Articulo articulo : articulos) {
            Object[] fila = {
                articulo.getIdArticulo(),
                articulo.getTipoArticulo(),
                articulo.getDescripcion(),
                articulo.getEstado(),
                String.format("$%.2f", articulo.getValorTasado()),
                articulo.getFechaAvaluo() != null ? sdf.format(articulo.getFechaAvaluo()) : "N/A",
                String.format("%.1f%%", articulo.getPorcentajeTasacion()),
                String.format("$%.2f", articulo.getPrecioMercadoBase())
            };
            modeloTabla.addRow(fila);
        }

        lblTotal.setText("Total de artículos: " + articulos.size());
    }

    /**
     * Aplica filtros a la tabla de artículos.
     */
    private void aplicarFiltros() {
        String estadoSeleccionado = (String) cmbFiltroEstado.getSelectedItem();
        String tipoSeleccionado = (String) cmbFiltroTipo.getSelectedItem();

        modeloTabla.setRowCount(0);
        List<Articulo> articulos;

        // Aplicar filtro por estado
        if (!"TODOS".equals(estadoSeleccionado)) {
            articulos = articuloDAO.listarArticulosPorEstado(estadoSeleccionado);
        } else {
            articulos = articuloDAO.listarTodosArticulos();
        }

        // Filtrar por tipo si es necesario
        for (Articulo articulo : articulos) {
            if ("TODOS".equals(tipoSeleccionado) || articulo.getTipoArticulo().equals(tipoSeleccionado)) {
                Object[] fila = {
                    articulo.getIdArticulo(),
                    articulo.getTipoArticulo(),
                    articulo.getDescripcion(),
                    articulo.getEstado(),
                    String.format("$%.2f", articulo.getValorTasado()),
                    articulo.getFechaAvaluo() != null ? sdf.format(articulo.getFechaAvaluo()) : "N/A",
                    String.format("%.1f%%", articulo.getPorcentajeTasacion()),
                    String.format("$%.2f", articulo.getPrecioMercadoBase())
                };
                modeloTabla.addRow(fila);
            }
        }

        lblTotal.setText("Artículos encontrados: " + modeloTabla.getRowCount());
    }

    /**
     * Busca artículos por descripción o tipo.
     */
    private void buscarArticulo() {
        String textoBuscar = txtBuscar.getText().trim().toUpperCase();

        if (textoBuscar.isEmpty()) {
            cargarDatosTabla();
            return;
        }

        modeloTabla.setRowCount(0);
        List<Articulo> articulos = articuloDAO.listarTodosArticulos();

        for (Articulo articulo : articulos) {
            if (articulo.getDescripcion().toUpperCase().contains(textoBuscar) ||
                articulo.getTipoArticulo().toUpperCase().contains(textoBuscar)) {
                Object[] fila = {
                    articulo.getIdArticulo(),
                    articulo.getTipoArticulo(),
                    articulo.getDescripcion(),
                    articulo.getEstado(),
                    String.format("$%.2f", articulo.getValorTasado()),
                    articulo.getFechaAvaluo() != null ? sdf.format(articulo.getFechaAvaluo()) : "N/A",
                    String.format("%.1f%%", articulo.getPorcentajeTasacion()),
                    String.format("$%.2f", articulo.getPrecioMercadoBase())
                };
                modeloTabla.addRow(fila);
            }
        }

        lblTotal.setText("Artículos encontrados: " + modeloTabla.getRowCount());
    }

    /**
     * Muestra el diálogo para crear un nuevo artículo.
     */
    private void mostrarDialogoNuevo() {
        DialogoArticulo dialogo = new DialogoArticulo((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialogo.setVisible(true);

        if (dialogo.isConfirmado()) {
            cargarDatosTabla();
        }
    }

    /**
     * Muestra el diálogo para editar el artículo seleccionado.
     */
    private void mostrarDialogoEditar() {
        int filaSeleccionada = tblArticulos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un artículo de la tabla.",
                "Sin Selección",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idArticulo = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        Articulo articulo = articuloDAO.obtenerArticuloPorId(idArticulo);

        if (articulo != null) {
            DialogoArticulo dialogo = new DialogoArticulo((Frame) SwingUtilities.getWindowAncestor(this), articulo);
            dialogo.setVisible(true);

            if (dialogo.isConfirmado()) {
                cargarDatosTabla();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "No se pudo cargar el artículo seleccionado.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el artículo seleccionado.
     */
    private void eliminarArticulo() {
        int filaSeleccionada = tblArticulos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un artículo de la tabla.",
                "Sin Selección",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idArticulo = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String descripcion = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar el artículo:\n" + descripcion + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (articuloDAO.eliminarArticulo(idArticulo)) {
                JOptionPane.showMessageDialog(this,
                    "Artículo eliminado exitosamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el artículo.\nVerifique que no esté en un préstamo activo.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

