package com.prestamos.vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;
import com.prestamos.modelo.Cliente;
import com.prestamos.dao.ClienteDAO;

/**
 * Panel para la gestión de clientes del sistema de préstamos.
 * Permite listar, buscar, agregar, modificar y eliminar clientes.
 *
 * @author Henersson Cobo
 * @version 1.0
 * @since 2025-11-22
 */
public class PanelClientes extends JPanel {

    // Componentes
    private JTable tblClientes;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnRefrescar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JLabel lblTotal;

    // DAO
    private ClienteDAO clienteDAO = new ClienteDAO();

    /**
     * Constructor del panel de clientes.
     */
    public PanelClientes() {
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

        // Panel norte con búsqueda y botones
        add(crearPanelNorte(), BorderLayout.NORTH);

        // Panel central con tabla
        add(crearPanelCentro(), BorderLayout.CENTER);

        // Panel sur con botones de acción
        add(crearPanelSur(), BorderLayout.SOUTH);
    }

    /**
     * Crea el panel norte con barra de búsqueda y botones.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelNorte() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Label Buscar
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblBuscar);

        // TextField Buscar
        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(250, 35));
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(txtBuscar);

        // Botón Buscar
        btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(new Font("Arial", Font.BOLD, 13));
        btnBuscar.setBackground(new Color(52, 152, 219));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.setPreferredSize(new Dimension(100, 35));
        panel.add(btnBuscar);

        // Separador
        panel.add(Box.createHorizontalStrut(20));

        // Botón Nuevo Cliente
        btnNuevo = new JButton("Nuevo Cliente");
        btnNuevo.setFont(new Font("Arial", Font.BOLD, 13));
        btnNuevo.setBackground(new Color(46, 204, 113));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFocusPainted(false);
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevo.setPreferredSize(new Dimension(160, 35));
        panel.add(btnNuevo);

        // Botón Refrescar
        btnRefrescar = new JButton("Refrescar");
        btnRefrescar.setFont(new Font("Arial", Font.BOLD, 13));
        btnRefrescar.setBackground(new Color(52, 152, 219));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setBorderPainted(false);
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefrescar.setPreferredSize(new Dimension(120, 35));
        panel.add(btnRefrescar);

        return panel;
    }

    /**
     * Crea el panel central con la tabla de clientes.
     *
     * @return JScrollPane con la tabla configurada
     */
    private JScrollPane crearPanelCentro() {
        // PARTE 1 - MODELO DE TABLA CON ENCABEZADOS
        // Definir columnas de la tabla
        String[] columnas = {
            "ID Persona",
            "Nombre Completo",
            "Tipo ID",
            "ID Cliente",
            "Calificación",
            "Estado"
        };

        // Crear modelo no editable
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3) {
                    return Integer.class; // Para ordenar numéricamente
                }
                return String.class;
            }
        };

        // Crear tabla
        tblClientes = new JTable(modeloTabla);
        tblClientes.setFont(new Font("Arial", Font.PLAIN, 13));
        tblClientes.setRowHeight(30);
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClientes.setSelectionBackground(new Color(52, 152, 219));
        tblClientes.setSelectionForeground(Color.WHITE);

        // PARTE 2 - CONFIGURAR ENCABEZADOS
        // Configurar encabezado
        JTableHeader header = tblClientes.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(Color.BLACK); // Azul oscuro
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false); // No permitir reordenar columnas
        header.setResizingAllowed(true);

        // PARTE 3 - AJUSTAR ANCHO DE COLUMNAS
        // Configurar ancho de columnas
        TableColumnModel columnModel = tblClientes.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(90);  // ID Persona
        columnModel.getColumn(0).setMinWidth(70);
        columnModel.getColumn(0).setMaxWidth(100);

        columnModel.getColumn(1).setPreferredWidth(220); // Nombre
        columnModel.getColumn(1).setMinWidth(150);

        columnModel.getColumn(2).setPreferredWidth(90);  // Tipo ID
        columnModel.getColumn(2).setMinWidth(70);
        columnModel.getColumn(2).setMaxWidth(120);

        columnModel.getColumn(3).setPreferredWidth(90);  // ID Cliente
        columnModel.getColumn(3).setMinWidth(70);
        columnModel.getColumn(3).setMaxWidth(100);

        columnModel.getColumn(4).setPreferredWidth(100); // Calificación
        columnModel.getColumn(4).setMinWidth(80);
        columnModel.getColumn(4).setMaxWidth(120);

        columnModel.getColumn(5).setPreferredWidth(80);  // Estado
        columnModel.getColumn(5).setMinWidth(60);
        columnModel.getColumn(5).setMaxWidth(100);

        // PARTE 4 - CENTRAR CONTENIDO
        // Renderer para centrar columnas numéricas y pequeñas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        // PARTE 7 - ALTERNAR COLORES DE FILAS
        // Renderer personalizado para alternar colores y alineación
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            private Color evenColor = Color.WHITE;
            private Color oddColor = new Color(240, 240, 240);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value,
                                                                  isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? evenColor : oddColor);
                }

                // Aplicar alineación según la columna
                if (column == 0 || column == 2 || column == 3 || column == 5) {
                    // Centrar: ID Persona, Tipo ID, ID Cliente, Estado
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 4) {
                    // Alinear a la derecha: Calificación
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    // Alinear a la izquierda: Nombre
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        // Aplicar renderer personalizado a todas las columnas
        for (int i = 0; i < tblClientes.getColumnCount(); i++) {
            tblClientes.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(tblClientes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scrollPane;
    }

    /**
     * Crea el panel sur con botones de acción.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelSur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));

        // Botón Editar
        btnEditar = new JButton("Editar");
        btnEditar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEditar.setBackground(new Color(52, 152, 219));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.setBorderPainted(false);
        btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditar.setPreferredSize(new Dimension(140, 40));
        btnEditar.setEnabled(false); // Deshabilitado inicialmente
        panel.add(btnEditar);

        // Botón Eliminar
        btnEliminar = new JButton("Eliminar");
        btnEliminar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEliminar.setBackground(new Color(231, 76, 60));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setBorderPainted(false);
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.setPreferredSize(new Dimension(140, 40));
        btnEliminar.setEnabled(false); // Deshabilitado inicialmente
        panel.add(btnEliminar);

        // Separador
        panel.add(Box.createHorizontalStrut(30));

        // Label Total
        lblTotal = new JLabel("Total clientes: 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(52, 73, 94));
        panel.add(lblTotal);

        return panel;
    }

    /**
     * Carga los datos de clientes desde la base de datos y los muestra en la tabla.
     */
    private void cargarDatosTabla() {
        System.out.println("\n→ Cargando lista de clientes...");

        // PARTE 5 - MEJORAR MÉTODO cargarDatosTabla()
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        try {
            List<Cliente> clientes = clienteDAO.listarTodosClientes();

            for (Cliente cliente : clientes) {
                Object[] fila = new Object[6];
                fila[0] = cliente.getIdPersona();
                fila[1] = cliente.getNombrePersona();
                fila[2] = cliente.getTipoId();
                fila[3] = cliente.getIdCliente();
                fila[4] = String.format("%.2f", cliente.getCalificacion()); // Formato 2 decimales
                fila[5] = cliente.esActivo() ? "Activo" : "Inactivo";

                modeloTabla.addRow(fila);
            }

            // Actualizar contador
            lblTotal.setText("Total de clientes: " + clientes.size());

            System.out.println("✓ " + clientes.size() + " clientes cargados en la tabla");

        } catch (Exception e) {
            System.err.println("✗ Error al cargar clientes: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error al cargar clientes:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            lblTotal.setText("Total de clientes: 0");
            e.printStackTrace();
        }
    }

    /**
     * Configura todos los eventos de los componentes del panel.
     */
    private void configurarEventos() {
        // Evento de selección de fila en la tabla
        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean haySeleccion = tblClientes.getSelectedRow() >= 0;
                btnEditar.setEnabled(haySeleccion);
                btnEliminar.setEnabled(haySeleccion);
            }
        });

        // Botón Nuevo Cliente
        btnNuevo.addActionListener(e -> {
            System.out.println("→ Abriendo diálogo de nuevo cliente");
            DialogoCliente dialogo = new DialogoCliente((JFrame) SwingUtilities.getWindowAncestor(this), null);
            dialogo.setVisible(true);
            if (dialogo.isGuardado()) {
                cargarDatosTabla();
            }
        });

        // Botón Editar
        btnEditar.addActionListener(e -> {
            int fila = tblClientes.getSelectedRow();
            if (fila >= 0) {
                int idPersona = (int) modeloTabla.getValueAt(fila, 0);
                System.out.println("→ Editando cliente con ID: " + idPersona);

                // Obtener cliente desde la base de datos
                Cliente cliente = clienteDAO.obtenerClientePorId(idPersona);

                if (cliente != null) {
                    DialogoCliente dialogo = new DialogoCliente((JFrame) SwingUtilities.getWindowAncestor(this), cliente);
                    dialogo.setVisible(true);
                    if (dialogo.isGuardado()) {
                        cargarDatosTabla();
                    }
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "No se pudo cargar los datos del cliente",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // Botón Eliminar
        btnEliminar.addActionListener(e -> {
            int fila = tblClientes.getSelectedRow();
            if (fila >= 0) {
                int idPersona = (int) modeloTabla.getValueAt(fila, 0);
                String nombre = (String) modeloTabla.getValueAt(fila, 1);

                // Confirmar eliminación
                int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro que desea eliminar al cliente?\n\n" +
                    "Nombre: " + nombre + "\n" +
                    "ID: " + idPersona + "\n\n" +
                    "Esta acción no se puede deshacer.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (opcion == JOptionPane.YES_OPTION) {
                    System.out.println("→ Eliminando cliente con ID: " + idPersona);

                    if (clienteDAO.eliminarCliente(idPersona)) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Cliente eliminado correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        cargarDatosTabla();
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Error al eliminar el cliente.\n" +
                            "Puede que tenga registros relacionados.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        // Botón Refrescar
        btnRefrescar.addActionListener(e -> {
            System.out.println("→ Refrescando lista de clientes");
            cargarDatosTabla();
        });

        // Botón Buscar (implementar después)
        btnBuscar.addActionListener(e -> {
            String textoBuscar = txtBuscar.getText().trim();
            if (textoBuscar.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Por favor, ingrese un criterio de búsqueda",
                    "Búsqueda vacía",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                // TODO: Implementar búsqueda
                JOptionPane.showMessageDialog(
                    this,
                    "Funcionalidad de búsqueda en desarrollo.\n" +
                    "Criterio: " + textoBuscar,
                    "Función en desarrollo",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        // Permitir buscar con Enter en el campo de texto
        txtBuscar.addActionListener(e -> btnBuscar.doClick());
    }
}
