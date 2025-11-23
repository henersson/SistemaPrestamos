package com.prestamos.vista;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.prestamos.config.ConexionOracle;

/**
 * Panel de inicio del sistema de préstamos.
 * Muestra estadísticas generales y bienvenida al usuario.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class PanelInicio extends JPanel {

    // Variables para estadísticas
    private int totalClientes = 0;
    private int totalPrestamos = 0;
    private int totalArticulos = 0;
    private int totalAsesores = 0;

    /**
     * Constructor del panel de inicio.
     */
    public PanelInicio() {
        cargarEstadisticas();
        initComponents();
    }

    /**
     * Inicializa los componentes del panel.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));

        // Panel superior con título de bienvenida
        add(crearPanelSuperior(), BorderLayout.NORTH);

        // Panel central con estadísticas
        add(crearPanelCentral(), BorderLayout.CENTER);
    }

    /**
     * Crea el panel superior con el título de bienvenida.
     *
     * @return JPanel configurado con el título
     */
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setOpaque(false);

        JLabel lblBienvenida = new JLabel("¡Bienvenido al Sistema de Préstamos!");
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 28));
        lblBienvenida.setForeground(new Color(52, 73, 94));
        panel.add(lblBienvenida);

        return panel;
    }

    /**
     * Crea el panel central con las estadísticas.
     *
     * @return JPanel configurado con las tarjetas de estadísticas
     */
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Crear paneles de estadísticas
        JPanel panelClientes = crearPanelEstadistica(
            "Clientes Registrados",
            "",
            totalClientes,
            new Color(52, 152, 219)
        );

        JPanel panelPrestamos = crearPanelEstadistica(
            "Préstamos Activos",
            "",
            totalPrestamos,
            new Color(46, 204, 113)
        );

        JPanel panelArticulos = crearPanelEstadistica(
            "Artículos Empeñados",
            "",
            totalArticulos,
            new Color(155, 89, 182)
        );

        JPanel panelAsesores = crearPanelEstadistica(
            "Asesores Activos",
            "",
            totalAsesores,
            new Color(230, 126, 34)
        );

        // Agregar paneles al grid
        panel.add(panelClientes);
        panel.add(panelPrestamos);
        panel.add(panelArticulos);
        panel.add(panelAsesores);

        return panel;
    }

    /**
     * Crea un panel de estadística con el diseño especificado.
     *
     * @param titulo título de la estadística
     * @param icono emoji del icono
     * @param valor valor numérico de la estadística
     * @param color color de fondo del panel
     * @return JPanel configurado con la estadística
     */
    private JPanel crearPanelEstadistica(String titulo, String icono, int valor, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(color);
        panel.setPreferredSize(new Dimension(250, 150));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Panel norte con icono y título
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel(icono + " " + titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        panelNorte.add(lblTitulo);

        panel.add(panelNorte, BorderLayout.NORTH);

        // Panel centro con el valor
        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelCentro.setOpaque(false);

        JLabel lblValor = new JLabel(String.valueOf(valor));
        lblValor.setFont(new Font("Arial", Font.BOLD, 48));
        lblValor.setForeground(Color.WHITE);
        panelCentro.add(lblValor);

        panel.add(panelCentro, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Carga las estadísticas desde la base de datos.
     * Ejecuta consultas para obtener los conteos de cada entidad.
     */
    private void cargarEstadisticas() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    CARGANDO ESTADÍSTICAS DEL SISTEMA   ║");
        System.out.println("╚════════════════════════════════════════╝");

        // Cargar estadística de Clientes
        totalClientes = cargarEstadisticaClientes();

        // Cargar estadística de Préstamos
        totalPrestamos = cargarEstadisticaPrestamos();

        // Cargar estadística de Artículos
        totalArticulos = cargarEstadisticaArticulos();

        // Cargar estadística de Asesores
        totalAsesores = cargarEstadisticaAsesores();

        System.out.println("══════════════════════════════════════════");
        System.out.println("✓ Estadísticas cargadas exitosamente\n");
    }

    /**
     * Obtiene el número total de clientes registrados.
     *
     * @return número de clientes, 0 si hay error
     */
    private int cargarEstadisticaClientes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int total = 0;

        try {
            conn = ConexionOracle.getConexion();
            String sql = "SELECT COUNT(*) AS TOTAL FROM CLIENTE";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getInt("TOTAL");
                System.out.println("  ✓ Clientes registrados: " + total);
            }

        } catch (SQLException e) {
            System.err.println("  ✗ Error al cargar clientes: " + e.getMessage());
            total = 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return total;
    }

    /**
     * Obtiene el número de préstamos activos.
     *
     * @return número de préstamos activos, 0 si hay error
     */
    private int cargarEstadisticaPrestamos() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int total = 0;

        try {
            conn = ConexionOracle.getConexion();
            String sql = "SELECT COUNT(*) AS TOTAL FROM PRESTAMO WHERE ESTADO_PRESTAMO = 'ACTIVO'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getInt("TOTAL");
                System.out.println("  ✓ Préstamos activos: " + total);
            }

        } catch (SQLException e) {
            System.err.println("  ✗ Error al cargar préstamos: " + e.getMessage());
            total = 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return total;
    }

    /**
     * Obtiene el número de artículos empeñados.
     *
     * @return número de artículos empeñados, 0 si hay error
     */
    private int cargarEstadisticaArticulos() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int total = 0;

        try {
            conn = ConexionOracle.getConexion();
            String sql = "SELECT COUNT(*) AS TOTAL FROM ARTICULO WHERE ESTADO = 'EMPEÑADO'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getInt("TOTAL");
                System.out.println("  ✓ Artículos empeñados: " + total);
            }

        } catch (SQLException e) {
            System.err.println("  ✗ Error al cargar artículos: " + e.getMessage());
            total = 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return total;
    }

    /**
     * Obtiene el número de asesores activos.
     *
     * @return número de asesores activos, 0 si hay error
     */
    private int cargarEstadisticaAsesores() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int total = 0;

        try {
            conn = ConexionOracle.getConexion();
            String sql = "SELECT COUNT(*) AS TOTAL FROM ASESOR WHERE ACTIVO = 'S'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getInt("TOTAL");
                System.out.println("  ✓ Asesores activos: " + total);
            }

        } catch (SQLException e) {
            System.err.println("  ✗ Error al cargar asesores: " + e.getMessage());
            total = 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return total;
    }
}
