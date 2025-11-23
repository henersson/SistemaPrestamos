package com.prestamos.vista;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.List;
import com.prestamos.dao.DiccionarioDatosDAO;

/**
 * Panel para visualizar el diccionario de datos de la base de datos.
 * Muestra estructura de tablas, paquetes, triggers y otros objetos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class PanelDiccionarioDatos extends JPanel {

    // Componentes
    private JTree tree;
    private JTextArea txtDetalles;
    private JLabel lblTitulo;

    // DAO
    private DiccionarioDatosDAO diccionarioDAO = new DiccionarioDatosDAO();

    /**
     * Constructor del panel de diccionario de datos.
     */
    public PanelDiccionarioDatos() {
        initComponents();
        cargarArbol();
        configurarEventos();
    }

    /**
     * Inicializa los componentes del panel.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));

        // Crear JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(5);

        // Panel izquierdo con árbol
        splitPane.setLeftComponent(crearPanelIzquierdo());

        // Panel derecho con detalles
        splitPane.setRightComponent(crearPanelDerecho());

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Crea el panel izquierdo con el árbol de navegación.
     *
     * @return JScrollPane con el árbol configurado
     */
    private JScrollPane crearPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel titulo = new JLabel("Estructura de Base de Datos");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titulo, BorderLayout.NORTH);

        // Crear árbol (se llenará en cargarArbol())
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Base de Datos");
        tree = new JTree(root);
        tree.setFont(new Font("Arial", Font.PLAIN, 14));
        tree.setRowHeight(25);

        JScrollPane scrollTree = new JScrollPane(tree);
        scrollTree.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollTree, BorderLayout.CENTER);

        JScrollPane scrollPanel = new JScrollPane(panel);
        scrollPanel.setBorder(null);
        return scrollPanel;
    }

    /**
     * Crea el panel derecho con el área de detalles.
     *
     * @return JPanel configurado
     */
    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del detalle
        lblTitulo = new JLabel("Seleccione un elemento del árbol");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Área de texto para detalles
        txtDetalles = new JTextArea();
        txtDetalles.setEditable(false);
        txtDetalles.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtDetalles.setBackground(new Color(250, 250, 250));
        txtDetalles.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtDetalles.setLineWrap(false);
        txtDetalles.setWrapStyleWord(false);
        txtDetalles.setText("Seleccione un elemento del árbol para ver sus detalles.\n\n" +
                           "Puede explorar:\n" +
                           "• Tablas: Estructura de columnas y estadísticas\n" +
                           "• Paquetes PL/SQL: Procedimientos y funciones\n" +
                           "• Triggers: Información de disparadores\n" +
                           "• Secuencias: Generadores de valores únicos");

        JScrollPane scrollDetalles = new JScrollPane(txtDetalles);
        scrollDetalles.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollDetalles, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Carga la estructura del árbol con los elementos de la base de datos.
     */
    private void cargarArbol() {
        System.out.println("\n→ Cargando estructura del diccionario de datos...");

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        root.removeAllChildren();

        // Nodo de Tablas
        DefaultMutableTreeNode nodoTablas = new DefaultMutableTreeNode("Tablas");
        try {
            List<String> tablas = diccionarioDAO.obtenerTablas();
            for (String tabla : tablas) {
                nodoTablas.add(new DefaultMutableTreeNode(tabla));
            }
            System.out.println("  ✓ Cargadas " + tablas.size() + " tablas");
        } catch (Exception e) {
            System.err.println("  ✗ Error al cargar tablas: " + e.getMessage());
            nodoTablas.add(new DefaultMutableTreeNode("Error al cargar tablas"));
        }
        root.add(nodoTablas);

        // Nodo de Paquetes PL/SQL
        DefaultMutableTreeNode nodoPaquetes = new DefaultMutableTreeNode("Paquetes PL/SQL");
        try {
            List<String> paquetes = diccionarioDAO.obtenerPaquetes();
            for (String paquete : paquetes) {
                nodoPaquetes.add(new DefaultMutableTreeNode(paquete));
            }
            System.out.println("  ✓ Cargados " + paquetes.size() + " paquetes");
        } catch (Exception e) {
            System.err.println("  ✗ Error al cargar paquetes: " + e.getMessage());
            nodoPaquetes.add(new DefaultMutableTreeNode("Error al cargar paquetes"));
        }
        root.add(nodoPaquetes);

        // Nodo de Triggers
        DefaultMutableTreeNode nodoTriggers = new DefaultMutableTreeNode("Triggers");
        try {
            List<String> triggers = diccionarioDAO.obtenerTriggers();
            for (String trigger : triggers) {
                nodoTriggers.add(new DefaultMutableTreeNode(trigger));
            }
            System.out.println("  ✓ Cargados " + triggers.size() + " triggers");
        } catch (Exception e) {
            System.err.println("  ✗ Error al cargar triggers: " + e.getMessage());
            nodoTriggers.add(new DefaultMutableTreeNode("Error al cargar triggers"));
        }
        root.add(nodoTriggers);

        // Nodo de Secuencias
        DefaultMutableTreeNode nodoSecuencias = new DefaultMutableTreeNode("Secuencias");
        nodoSecuencias.add(new DefaultMutableTreeNode("Funcionalidad en desarrollo"));
        root.add(nodoSecuencias);

        // Actualizar árbol
        ((DefaultTreeModel) tree.getModel()).reload();

        // Expandir nodo raíz
        tree.expandRow(0);

        System.out.println("✓ Estructura del árbol cargada exitosamente\n");
    }

    /**
     * Configura los eventos de los componentes del panel.
     */
    private void configurarEventos() {
        // Evento de selección en el árbol
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (nodo == null) {
                return;
            }

            Object valor = nodo.getUserObject();
            DefaultMutableTreeNode padre = (DefaultMutableTreeNode) nodo.getParent();

            if (padre == null) {
                return;
            }

            String nombrePadre = padre.toString();

            // Mostrar indicador de carga
            txtDetalles.setText("Cargando información...");
            txtDetalles.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Usar SwingWorker para no bloquear la interfaz
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    String detalles = "";

                    if (nombrePadre.contains("Tablas")) {
                        // Mostrar detalles de tabla
                        lblTitulo.setText("Tabla: " + valor);
                        System.out.println("→ Consultando tabla: " + valor);
                        detalles = diccionarioDAO.obtenerDetallesTabla(valor.toString());

                    } else if (nombrePadre.contains("Paquetes")) {
                        // Mostrar detalles de paquete
                        lblTitulo.setText("Paquete: " + valor);
                        System.out.println("→ Consultando paquete: " + valor);
                        detalles = diccionarioDAO.obtenerDetallesPaquete(valor.toString());

                    } else if (nombrePadre.contains("Triggers")) {
                        // Mostrar detalles de trigger
                        lblTitulo.setText("Trigger: " + valor);
                        System.out.println("→ Consultando trigger: " + valor);
                        detalles = diccionarioDAO.obtenerDetallesTrigger(valor.toString());

                    } else if (nombrePadre.contains("Secuencias")) {
                        // Funcionalidad de secuencias en desarrollo
                        lblTitulo.setText("Secuencia: " + valor);
                        detalles = "╔════════════════════════════════════════════════════════════╗\n" +
                                  "║          FUNCIONALIDAD EN DESARROLLO                       ║\n" +
                                  "╚════════════════════════════════════════════════════════════╝\n\n" +
                                  "La consulta de secuencias estará disponible próximamente.\n";

                    } else {
                        // Nodo padre (carpeta)
                        lblTitulo.setText(valor.toString());
                        detalles = "Seleccione un elemento específico para ver sus detalles.\n\n" +
                                  "Este nodo contiene " + nodo.getChildCount() + " elemento(s).";
                    }

                    return detalles;
                }

                @Override
                protected void done() {
                    try {
                        String detalles = get();
                        txtDetalles.setText(detalles);
                        txtDetalles.setCaretPosition(0); // Scroll arriba
                        System.out.println("  ✓ Detalles cargados exitosamente");
                    } catch (Exception ex) {
                        System.err.println("  ✗ Error al cargar detalles: " + ex.getMessage());
                        txtDetalles.setText("ERROR: No se pudieron cargar los detalles\n" + ex.getMessage());
                    } finally {
                        txtDetalles.setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

            worker.execute();
        });
    }
}
