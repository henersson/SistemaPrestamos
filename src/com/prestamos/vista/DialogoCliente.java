package com.prestamos.vista;

import com.prestamos.dao.ClienteDAO;
import com.prestamos.modelo.Cliente;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diálogo modal para crear y editar clientes.
 * Incluye validaciones completas y diseño profesional.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class DialogoCliente extends JDialog {

    // Variables de instancia
    private Cliente clienteEditar;
    private boolean guardado = false;
    private ClienteDAO clienteDAO = new ClienteDAO();

    // Componentes del formulario
    private JTextField txtIdPersona;
    private JTextField txtNombre;
    private JComboBox<String> cmbTipoId;
    private JTextField txtIdCliente;
    private JLabel lblFechaRegistro;
    private JSpinner spnCalificacion;
    private JCheckBox chkActivo;
    private JButton btnGuardar;
    private JButton btnCancelar;

    /**
     * Constructor del diálogo.
     *
     * @param parent ventana padre
     * @param clienteEditar cliente a editar (null para crear nuevo)
     */
    public DialogoCliente(JFrame parent, Cliente clienteEditar) {
        super(parent, true);
        this.clienteEditar = clienteEditar;

        // Configurar ventana
        setTitle(clienteEditar == null ? "Nuevo Cliente" : "Editar Cliente");
        setSize(650, 580);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        inicializarComponentes();
        configurarEventos();
        cargarDatos();
    }

    /**
     * Indica si se guardó el cliente correctamente.
     *
     * @return true si se guardó, false en caso contrario
     */
    public boolean isGuardado() {
        return guardado;
    }

    /**
     * Inicializa y configura todos los componentes del formulario.
     */
    private void inicializarComponentes() {
        // Panel principal con fondo
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(null);
        panelPrincipal.setBackground(new Color(236, 240, 241));

        // ===== TÍTULO =====
        JLabel lblTitulo = new JLabel(
            clienteEditar == null ? "Agregar Nuevo Cliente" : "Editar Cliente"
        );
        lblTitulo.setBounds(0, 20, 650, 40);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelPrincipal.add(lblTitulo);

        // ===== CAMPO: ID PERSONA =====
        JLabel lblIdPersona = new JLabel("ID Persona:");
        lblIdPersona.setBounds(50, 90, 150, 25);
        lblIdPersona.setFont(new Font("Arial", Font.BOLD, 14));
        panelPrincipal.add(lblIdPersona);

        txtIdPersona = new JTextField();
        txtIdPersona.setBounds(220, 90, 380, 32);
        txtIdPersona.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(txtIdPersona);

        // ===== CAMPO: NOMBRE =====
        JLabel lblNombre = new JLabel("Nombre Completo:");
        lblNombre.setBounds(50, 140, 150, 25);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        panelPrincipal.add(lblNombre);

        txtNombre = new JTextField();
        txtNombre.setBounds(220, 140, 380, 32);
        txtNombre.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(txtNombre);

        // ===== CAMPO: TIPO ID =====
        JLabel lblTipoId = new JLabel("Tipo de ID:");
        lblTipoId.setBounds(50, 190, 150, 25);
        lblTipoId.setFont(new Font("Arial", Font.BOLD, 14));
        panelPrincipal.add(lblTipoId);

        cmbTipoId = new JComboBox<>(new String[]{"CC", "TI", "CE", "PASAPORTE"});
        cmbTipoId.setBounds(220, 190, 200, 32);
        cmbTipoId.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(cmbTipoId);

        // ===== CAMPO: ID CLIENTE =====
        JLabel lblIdCliente = new JLabel("ID Cliente:");
        lblIdCliente.setBounds(50, 240, 150, 25);
        lblIdCliente.setFont(new Font("Arial", Font.BOLD, 14));
        panelPrincipal.add(lblIdCliente);

        txtIdCliente = new JTextField();
        txtIdCliente.setBounds(220, 240, 200, 32);
        txtIdCliente.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(txtIdCliente);

        // ===== CAMPO: FECHA REGISTRO =====
        JLabel lblFecha = new JLabel("Fecha de Registro:");
        lblFecha.setBounds(50, 290, 150, 25);
        lblFecha.setFont(new Font("Arial", Font.BOLD, 14));
        panelPrincipal.add(lblFecha);

        lblFechaRegistro = new JLabel();
        lblFechaRegistro.setBounds(220, 290, 200, 32);
        lblFechaRegistro.setFont(new Font("Arial", Font.PLAIN, 14));
        lblFechaRegistro.setForeground(new Color(52, 73, 94));
        panelPrincipal.add(lblFechaRegistro);

        // ===== CAMPO: CALIFICACIÓN =====
        JLabel lblCalificacion = new JLabel("Calificación:");
        lblCalificacion.setBounds(50, 340, 150, 25);
        lblCalificacion.setFont(new Font("Arial", Font.BOLD, 14));
        panelPrincipal.add(lblCalificacion);

        SpinnerNumberModel modelCalif = new SpinnerNumberModel(5.0, 1.0, 10.0, 0.5);
        spnCalificacion = new JSpinner(modelCalif);
        spnCalificacion.setBounds(220, 340, 100, 32);
        spnCalificacion.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spnCalificacion.getEditor();
        editor.getFormat().setMinimumFractionDigits(1);
        editor.getFormat().setMaximumFractionDigits(1);
        panelPrincipal.add(spnCalificacion);

        JLabel lblRango = new JLabel("(1.0 - 10.0)");
        lblRango.setBounds(330, 340, 100, 32);
        lblRango.setFont(new Font("Arial", Font.ITALIC, 12));
        lblRango.setForeground(Color.GRAY);
        panelPrincipal.add(lblRango);

        // ===== CAMPO: ACTIVO =====
        chkActivo = new JCheckBox("Cliente Activo");
        chkActivo.setBounds(220, 390, 200, 30);
        chkActivo.setFont(new Font("Arial", Font.BOLD, 14));
        chkActivo.setBackground(new Color(236, 240, 241));
        chkActivo.setSelected(true);
        panelPrincipal.add(chkActivo);

        // ===== SEPARADOR =====
        JSeparator separador = new JSeparator();
        separador.setBounds(50, 440, 550, 2);
        panelPrincipal.add(separador);

        // ===== BOTONES =====
        btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(200, 470, 140, 45);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 15));
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelPrincipal.add(btnGuardar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(360, 470, 140, 45);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 15));
        btnCancelar.setBackground(new Color(149, 165, 166));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelPrincipal.add(btnCancelar);

        // Agregar panel a la ventana
        add(panelPrincipal);
    }

    /**
     * Carga los datos del cliente en el formulario.
     */
    private void cargarDatos() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (clienteEditar != null) {
            // Modo EDITAR
            txtIdPersona.setText(String.valueOf(clienteEditar.getIdPersona()));
            txtIdPersona.setEnabled(false);
            txtIdPersona.setBackground(new Color(220, 220, 220));

            txtNombre.setText(clienteEditar.getNombrePersona());
            cmbTipoId.setSelectedItem(clienteEditar.getTipoId());

            txtIdCliente.setText(String.valueOf(clienteEditar.getIdCliente()));
            txtIdCliente.setEnabled(false);
            txtIdCliente.setBackground(new Color(220, 220, 220));

            lblFechaRegistro.setText(sdf.format(clienteEditar.getFechaRegistro()));
            spnCalificacion.setValue(clienteEditar.getCalificacion());
            chkActivo.setSelected(clienteEditar.esActivo());

            System.out.println("✓ Cargados datos del cliente: " + clienteEditar.getNombrePersona());
        } else {
            // Modo NUEVO
            lblFechaRegistro.setText(sdf.format(new Date()));
            System.out.println("✓ Modo nuevo cliente");
        }
    }

    /**
     * Valida todos los campos del formulario.
     *
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        // Validar ID Persona
        String idPersonaStr = txtIdPersona.getText().trim();
        if (idPersonaStr.isEmpty()) {
            mostrarError("El ID de persona es obligatorio");
            txtIdPersona.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(idPersonaStr);
        } catch (NumberFormatException e) {
            mostrarError("El ID de persona debe ser un número válido");
            txtIdPersona.requestFocus();
            return false;
        }

        // Validar Nombre
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        if (nombre.length() < 3) {
            mostrarError("El nombre debe tener al menos 3 caracteres");
            txtNombre.requestFocus();
            return false;
        }

        // Validar ID Cliente (solo en modo NUEVO)
        if (clienteEditar == null) {
            String idClienteStr = txtIdCliente.getText().trim();
            if (idClienteStr.isEmpty()) {
                mostrarError("El ID de cliente es obligatorio");
                txtIdCliente.requestFocus();
                return false;
            }

            try {
                Integer.parseInt(idClienteStr);
            } catch (NumberFormatException e) {
                mostrarError("El ID de cliente debe ser un número válido");
                txtIdCliente.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Guarda el cliente en la base de datos (crear o actualizar).
     */
    private void guardarCliente() {
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear objeto Cliente
            Cliente cliente = new Cliente();
            cliente.setIdPersona(Integer.parseInt(txtIdPersona.getText().trim()));
            cliente.setNombrePersona(txtNombre.getText().trim());
            cliente.setTipoId((String) cmbTipoId.getSelectedItem());
            cliente.setTipoPersona("CLIENTE");
            cliente.setIdCliente(Integer.parseInt(txtIdCliente.getText().trim()));
            cliente.setFechaRegistro(new Date());
            cliente.setCalificacion((Double) spnCalificacion.getValue());
            cliente.setActivo(chkActivo.isSelected() ? 'S' : 'N');

            boolean exito = false;
            String mensaje = "";

            if (clienteEditar == null) {
                // MODO NUEVO
                exito = clienteDAO.insertarCliente(cliente);
                mensaje = "Cliente creado exitosamente";
                System.out.println("▸ Intentando crear cliente: " + cliente.getNombrePersona());
            } else {
                // MODO EDITAR
                cliente.setIdCliente(clienteEditar.getIdCliente());
                exito = clienteDAO.actualizarCliente(cliente);
                mensaje = "Cliente actualizado exitosamente";
                System.out.println("▸ Intentando actualizar cliente: " + cliente.getNombrePersona());
            }

            if (exito) {
                JOptionPane.showMessageDialog(this,
                    mensaje,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                guardado = true;
                System.out.println("✓ " + mensaje);
                dispose();
            } else {
                mostrarError("No se pudo guardar el cliente. Verifique los datos.");
            }

        } catch (NumberFormatException e) {
            mostrarError("Los IDs deben ser números válidos");
            e.printStackTrace();
        } catch (Exception e) {
            mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error al usuario.
     *
     * @param mensaje mensaje a mostrar
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error de Validación",
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Configura todos los eventos de los componentes.
     */
    private void configurarEventos() {
        // Botón Guardar
        btnGuardar.addActionListener(e -> guardarCliente());

        // Botón Cancelar
        btnCancelar.addActionListener(e -> {
            if (guardado || confirmarCierre()) {
                dispose();
            }
        });

        // Enter en campos de texto
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    guardarCliente();
                }
            }
        };

        txtIdPersona.addKeyListener(enterKeyAdapter);
        txtNombre.addKeyListener(enterKeyAdapter);
        txtIdCliente.addKeyListener(enterKeyAdapter);

        // ESC para cancelar
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /**
     * Confirma si el usuario desea cerrar el diálogo sin guardar.
     *
     * @return true si confirma, false en caso contrario
     */
    private boolean confirmarCierre() {
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de cancelar? Los cambios no se guardarán.",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        return opcion == JOptionPane.YES_OPTION;
    }
}

