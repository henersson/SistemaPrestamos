package com.prestamos.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.prestamos.modelo.Usuario;
import com.prestamos.controlador.AutenticacionController;

/**
 * Formulario de inicio de sesión del sistema de préstamos.
 * Permite la autenticación de usuarios (Administradores y Asesores).
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class FrmLogin extends JFrame {

    // Componentes de la interfaz
    private JLabel lblTitulo;
    private JLabel lblUsuario;
    private JTextField txtUsuario;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCancelar;

    /**
     * Constructor del formulario de login.
     * Inicializa y configura todos los componentes de la interfaz.
     */
    public FrmLogin() {
        initComponents();
    }

    /**
     * Inicializa y configura todos los componentes del formulario.
     */
    private void initComponents() {
        // Configuración del JFrame
        setTitle("Login - Sistema de Préstamos");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Centrar en pantalla
        setLocationRelativeTo(null);

        // Crear y configurar componentes

        // Título
        lblTitulo = new JLabel("Sistema de Préstamos - Casa de Empeño");
        lblTitulo.setBounds(50, 30, 350, 40);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setForeground(new Color(41, 128, 185));
        add(lblTitulo);

        // Label Usuario
        lblUsuario = new JLabel("Usuario:");
        lblUsuario.setBounds(70, 100, 100, 25);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        add(lblUsuario);

        // TextField Usuario
        txtUsuario = new JTextField();
        txtUsuario.setBounds(170, 100, 200, 30);
        txtUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        add(txtUsuario);

        // Label Password
        lblPassword = new JLabel("Contraseña:");
        lblPassword.setBounds(70, 150, 100, 25);
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        add(lblPassword);

        // PasswordField
        txtPassword = new JPasswordField();
        txtPassword.setBounds(170, 150, 200, 30);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        add(txtPassword);

        // Botón Ingresar
        btnIngresar = new JButton("Iniciar Sesión");
        btnIngresar.setBounds(120, 220, 200, 35);
        btnIngresar.setFont(new Font("Arial", Font.BOLD, 14));
        btnIngresar.setBackground(new Color(52, 152, 219));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(btnIngresar);

        // Botón Cancelar
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(120, 270, 200, 35);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(231, 76, 60));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(btnCancelar);

        // ActionListener para btnIngresar
        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });

        // ActionListener para btnCancelar
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("→ Aplicación cerrada por el usuario");
                System.exit(0);
            }
        });

        // Permitir login con Enter en los campos de texto
        txtUsuario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtPassword.requestFocus();
            }
        });

        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });
    }

    /**
     * Método que maneja el proceso de inicio de sesión.
     * Valida campos, autentica usuario y abre el formulario principal si es exitoso.
     */
    private void iniciarSesion() {
        // Obtener valores de los campos
        String user = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());

        // Validar que los campos no estén vacíos
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, ingrese usuario y contraseña",
                "Campos vacíos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Validar usuario
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      INTENTO DE INICIO DE SESIÓN       ║");
        System.out.println("╚════════════════════════════════════════╝");

        AutenticacionController controller = new AutenticacionController();
        Usuario usuario = controller.validarUsuario(user, pass);

        if (usuario != null) {
            // Autenticación exitosa
            System.out.println("✓ Autenticación exitosa");
            System.out.println("══════════════════════════════════════════\n");

            JOptionPane.showMessageDialog(
                this,
                "¡Bienvenido " + usuario.getNombre() + "!\nRol: " + usuario.getTipoPersona(),
                "Acceso concedido",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Abrir formulario principal
            FrmPrincipal frmPrincipal = new FrmPrincipal(usuario);
            frmPrincipal.setVisible(true);

            // Cerrar formulario de login
            this.dispose();

        } else {
            // Autenticación fallida
            System.out.println("✗ Autenticación fallida");
            System.out.println("══════════════════════════════════════════\n");

            JOptionPane.showMessageDialog(
                this,
                "Credenciales inválidas.\nVerifique su usuario y contraseña.",
                "Error de autenticación",
                JOptionPane.ERROR_MESSAGE
            );

            // Limpiar campos
            limpiarCampos();
        }
    }

    /**
     * Limpia los campos de texto del formulario.
     */
    private void limpiarCampos() {
        txtUsuario.setText("");
        txtPassword.setText("");
        txtUsuario.requestFocus();
    }

    /**
     * Método principal para ejecutar el formulario de login.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo establecer el Look and Feel del sistema");
        }

        // Ejecutar en el Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            FrmLogin frmLogin = new FrmLogin();
            frmLogin.setVisible(true);
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║   SISTEMA DE PRÉSTAMOS - CASA DE EMPEÑO      ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println("→ Formulario de login inicializado");
            System.out.println("→ Esperando credenciales de usuario...\n");
        });
    }
}
