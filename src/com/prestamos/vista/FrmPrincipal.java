package com.prestamos.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.prestamos.modelo.Usuario;

/**
 * Formulario principal del sistema de préstamos.
 * Contiene el menú de navegación y el área de trabajo central.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class FrmPrincipal extends JFrame {

    // Usuario autenticado
    private Usuario usuario;

    // Paneles principales
    private JPanel panelCentro;

    // Componentes del menú
    private JButton btnInicio;
    private JButton btnClientes;
    private JButton btnPrestamos;
    private JButton btnArticulos;
    private JButton btnAsesores;
    private JButton btnReportes;
    private JButton btnDiccionario;
    private JButton btnSalir;

    // Componentes del header
    private JButton btnCerrarSesion;

    /**
     * Constructor del formulario principal.
     *
     * @param usuario objeto Usuario con la información del usuario autenticado
     */
    public FrmPrincipal(Usuario usuario) {
        this.usuario = usuario;
        initComponents();
        configurarEventos();
    }

    /**
     * Inicializa y configura todos los componentes del formulario.
     */
    private void initComponents() {
        // Configuración del JFrame
        setTitle("Sistema de Préstamos - Casa de Empeño");
        setSize(1280, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Centrar en pantalla
        setLocationRelativeTo(null);

        // Crear paneles principales
        add(crearPanelNorte(), BorderLayout.NORTH);
        add(crearPanelOeste(), BorderLayout.WEST);
        add(crearPanelCentro(), BorderLayout.CENTER);
        add(crearPanelSur(), BorderLayout.SOUTH);
    }

    /**
     * Crea el panel superior (header) del sistema.
     *
     * @return JPanel configurado con el header
     */
    private JPanel crearPanelNorte() {
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setPreferredSize(new Dimension(0, 70));
        panelNorte.setBackground(new Color(52, 73, 94));
        panelNorte.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Título del sistema (WEST)
        JLabel lblTitulo = new JLabel("Sistema de Préstamos y Casa de Empeño");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelNorte.add(lblTitulo, BorderLayout.WEST);

        // Panel de usuario (EAST)
        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelUsuario.setOpaque(false);

        JLabel lblUsuario = new JLabel("Usuario: " + usuario.getNombre());
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUsuario.setForeground(Color.WHITE);
        panelUsuario.add(lblUsuario);

        JLabel lblRol = new JLabel("| Rol: " + usuario.getTipoPersona());
        lblRol.setFont(new Font("Arial", Font.BOLD, 14));
        lblRol.setForeground(Color.WHITE);
        panelUsuario.add(lblRol);

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 13));
        btnCerrarSesion.setBackground(new Color(231, 76, 60));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setPreferredSize(new Dimension(150, 35));
        panelUsuario.add(btnCerrarSesion);

        panelNorte.add(panelUsuario, BorderLayout.EAST);

        return panelNorte;
    }

    /**
     * Crea el panel lateral izquierdo (menú) del sistema.
     *
     * @return JPanel configurado con el menú de navegación
     */
    private JPanel crearPanelOeste() {
        JPanel panelOeste = new JPanel();
        panelOeste.setPreferredSize(new Dimension(250, 0));
        panelOeste.setBackground(new Color(44, 62, 80));
        panelOeste.setLayout(new BoxLayout(panelOeste, BoxLayout.Y_AXIS));
        panelOeste.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Crear botones del menú
        btnInicio = crearBotonMenu("Inicio");
        btnClientes = crearBotonMenu("Gestión de Clientes");
        btnPrestamos = crearBotonMenu("Gestión de Préstamos");
        btnArticulos = crearBotonMenu("Gestión de Artículos");
        btnAsesores = crearBotonMenu("Gestión de Asesores");
        btnReportes = crearBotonMenu("Reportes");
        btnDiccionario = crearBotonMenu("Diccionario de Datos");
        btnSalir = crearBotonMenu("Salir");

        // Agregar botones al panel
        panelOeste.add(btnInicio);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnClientes);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnPrestamos);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnArticulos);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnAsesores);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnReportes);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnDiccionario);
        panelOeste.add(Box.createVerticalGlue());
        panelOeste.add(btnSalir);

        return panelOeste;
    }

    /**
     * Crea un botón estilizado para el menú lateral.
     *
     * @param texto texto del botón
     * @return JButton configurado con el estilo del menú
     */
    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(240, 45));
        btn.setPreferredSize(new Dimension(240, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setIcon(null);
        btn.setIconTextGap(0);

        // Efecto hover
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(52, 152, 219));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(52, 73, 94));
            }
        });

        return btn;
    }

    /**
     * Crea el panel central del sistema (área de trabajo).
     *
     * @return JPanel configurado como área de trabajo
     */
    private JPanel crearPanelCentro() {
        panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(new Color(236, 240, 241));

        // Mostrar panel de inicio por defecto
        panelCentro.add(new PanelInicio(), BorderLayout.CENTER);

        return panelCentro;
    }

    /**
     * Crea el panel inferior (barra de estado) del sistema.
     *
     * @return JPanel configurado con la barra de estado
     */
    private JPanel crearPanelSur() {
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        panelSur.setPreferredSize(new Dimension(0, 35));
        panelSur.setBackground(new Color(52, 73, 94));

        JLabel lblEstado = new JLabel("Conectado");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 12));
        lblEstado.setForeground(new Color(46, 204, 113)); // Verde para indicar conectado
        panelSur.add(lblEstado);

        // Fecha y hora actual
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        JLabel lblFecha = new JLabel(sdf.format(new Date()));
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 12));
        lblFecha.setForeground(Color.WHITE);
        panelSur.add(lblFecha);

        return panelSur;
    }

    /**
     * Cambia el panel central por un nuevo panel.
     *
     * @param nuevoPanel panel a mostrar en el área de trabajo central
     */
    private void cambiarPanel(JPanel nuevoPanel) {
        panelCentro.removeAll();
        panelCentro.add(nuevoPanel, BorderLayout.CENTER);
        panelCentro.revalidate();
        panelCentro.repaint();
    }

    /**
     * Configura todos los eventos de los componentes del formulario.
     */
    private void configurarEventos() {
        // Evento botón Inicio
        btnInicio.addActionListener(e -> {
            System.out.println("→ Navegando a: Inicio");
            cambiarPanel(new PanelInicio());
        });

        // Evento botón Clientes
        btnClientes.addActionListener(e -> {
            System.out.println("→ Navegando a: Gestión de Clientes");
            cambiarPanel(new PanelClientes());
        });

        // Evento botón Préstamos
        btnPrestamos.addActionListener(e -> {
            System.out.println("→ Navegando a: Gestión de Préstamos");
            JOptionPane.showMessageDialog(
                this,
                "Módulo de Gestión de Préstamos en desarrollo",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Evento botón Artículos
        btnArticulos.addActionListener(e -> {
            System.out.println("→ Navegando a: Gestión de Artículos");
            JOptionPane.showMessageDialog(
                this,
                "Módulo de Gestión de Artículos en desarrollo",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Evento botón Asesores
        btnAsesores.addActionListener(e -> {
            System.out.println("→ Navegando a: Gestión de Asesores");
            JOptionPane.showMessageDialog(
                this,
                "Módulo de Gestión de Asesores en desarrollo",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Evento botón Reportes
        btnReportes.addActionListener(e -> {
            System.out.println("→ Navegando a: Reportes");
            cambiarPanel(new PanelReportes());
        });

        // Evento botón Diccionario
        btnDiccionario.addActionListener(e -> {
            System.out.println("→ Navegando a: Diccionario de Datos");
            cambiarPanel(new PanelDiccionarioDatos());
        });

        // Evento botón Salir
        btnSalir.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea salir del sistema?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (opcion == JOptionPane.YES_OPTION) {
                System.out.println("→ Aplicación cerrada por el usuario");
                System.exit(0);
            }
        });

        // Evento botón Cerrar Sesión
        btnCerrarSesion.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar cierre de sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (opcion == JOptionPane.YES_OPTION) {
                System.out.println("→ Cerrando sesión del usuario: " + usuario.getNombre());
                this.dispose();
                new FrmLogin().setVisible(true);
            }
        });
    }
}
