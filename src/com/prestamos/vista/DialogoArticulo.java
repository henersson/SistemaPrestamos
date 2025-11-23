package com.prestamos.vista;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import com.prestamos.modelo.Articulo;
import com.prestamos.dao.ArticuloDAO;

/**
 * Diálogo para crear o editar artículos.
 * Valida los datos antes de guardar.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class DialogoArticulo extends JDialog {

    // Componentes
    private JTextField txtId;
    private JTextField txtNombreArticulo; // Nuevo campo para el nombre del artículo
    private JTextField txtIdCliente;
    private JTextField txtNombre;
    private JButton btnBuscarCliente;
    private JComboBox<String> cmbTipo;
    private JTextArea txtDescripcion;
    private JComboBox<String> cmbEstado;
    private JTextField txtValorTasado;
    private JTextField txtPrecioMercado;
    private JTextField txtPorcentajeTasacion;
    private JButton btnGuardar;
    private JButton btnCancelar;

    // DAO
    private final ArticuloDAO articuloDAO = new ArticuloDAO();
    private final com.prestamos.dao.ClienteDAO clienteDAO = new com.prestamos.dao.ClienteDAO();

    // Control
    private boolean confirmado = false;
    private Articulo articulo;
    private final boolean esNuevo;

    /**
     * Constructor para crear o editar artículo.
     *
     * @param parent frame padre
     * @param articulo artículo a editar (null para nuevo)
     */
    public DialogoArticulo(Frame parent, Articulo articulo) {
        super(parent, articulo == null ? "Nuevo Artículo" : "Editar Artículo", true);
        this.articulo = articulo;
        this.esNuevo = (articulo == null);

        initComponents();
        if (!esNuevo) {
            cargarDatosArticulo();
        }

        setSize(600, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Inicializa los componentes del diálogo.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel principal con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Fila 0: ID Artículo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("ID Artículo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtId = new JTextField();
        txtId.setFont(new Font("Arial", Font.PLAIN, 14));
        txtId.setPreferredSize(new Dimension(0, 35));
        txtId.setEnabled(esNuevo);
        panelFormulario.add(txtId, gbc);

        // Fila 1: Nombre Artículo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Nombre Artículo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtNombreArticulo = new JTextField();
        txtNombreArticulo.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNombreArticulo.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtNombreArticulo, gbc);

        // Fila 2: Tipo (ajustar el índice de las filas siguientes)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Tipo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cmbTipo = new JComboBox<>(new String[]{
            "ELECTRONICO", "VEHICULO", "INMUEBLE", "JOYA", "INSTRUMENTO", "OTRO"
        });
        cmbTipo.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbTipo.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(cmbTipo, gbc);

        // Fila 3: Descripción
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Descripción:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 14));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setPreferredSize(new Dimension(0, 80));
        panelFormulario.add(scrollDescripcion, gbc);

        // Fila 4: Estado (solo valores válidos según la BD)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Estado:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cmbEstado = new JComboBox<>(new String[]{
            "DISPONIBLE", "EMPEÑADO", "VENDIDO", "RETIRADO"
        });
        cmbEstado.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEstado.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(cmbEstado, gbc);

        // Fila 5: Precio Mercado
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Precio Mercado:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPrecioMercado = new JTextField();
        txtPrecioMercado.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPrecioMercado.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtPrecioMercado, gbc);

        // Fila 6: Porcentaje Tasación
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*% Tasación:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPorcentajeTasacion = new JTextField("70.0");
        txtPorcentajeTasacion.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPorcentajeTasacion.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtPorcentajeTasacion, gbc);

        // Fila 7: Valor Tasado (calculado)
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("Valor Tasado:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtValorTasado = new JTextField();
        txtValorTasado.setFont(new Font("Arial", Font.BOLD, 14));
        txtValorTasado.setPreferredSize(new Dimension(0, 35));
        txtValorTasado.setEditable(false);
        txtValorTasado.setBackground(new Color(236, 240, 241));
        panelFormulario.add(txtValorTasado, gbc);

        // Fila 8: ID Cliente
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*ID Cliente:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtIdCliente = new JTextField();
        txtIdCliente.setFont(new Font("Arial", Font.PLAIN, 14));
        txtIdCliente.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtIdCliente, gbc);

        // Fila 9: Nombre (opcional - solo para referencia)
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("Nombre Cliente:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNombre.setPreferredSize(new Dimension(0, 35));
        txtNombre.setEditable(false);
        txtNombre.setBackground(new Color(236, 240, 241));
        txtNombre.setToolTipText("Campo informativo - se obtiene automáticamente de la base de datos");
        panelFormulario.add(txtNombre, gbc);

        // Fila 10: Botón Buscar Cliente
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel(""), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        btnBuscarCliente = new JButton("Buscar Cliente");
        btnBuscarCliente.setFont(new Font("Arial", Font.BOLD, 14));
        btnBuscarCliente.setBackground(new Color(52, 152, 219));
        btnBuscarCliente.setForeground(Color.WHITE);
        btnBuscarCliente.setFocusPainted(false);
        btnBuscarCliente.setBorderPainted(false);
        btnBuscarCliente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscarCliente.setPreferredSize(new Dimension(150, 35));
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        panelFormulario.add(btnBuscarCliente, gbc);

        // Agregar listener para calcular valor tasado automáticamente
        txtPrecioMercado.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularValorTasado();
            }
        });

        txtPorcentajeTasacion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularValorTasado();
            }
        });

        add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));

        btnGuardar = crearBoton("Guardar", new Color(46, 204, 113));
        btnCancelar = crearBoton("Cancelar", new Color(149, 165, 166));

        btnGuardar.addActionListener(e -> guardarArticulo());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        // Nota de campos obligatorios
        JLabel lblNota = new JLabel("* Campos obligatorios");
        lblNota.setFont(new Font("Arial", Font.ITALIC, 12));
        lblNota.setForeground(new Color(231, 76, 60));
        lblNota.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 0));
        add(lblNota, BorderLayout.NORTH);
    }

    /**
     * Crea un JLabel con estilo.
     */
    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }

    /**
     * Crea un JButton con estilo.
     */
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(120, 35));
        return boton;
    }

    /**
     * Calcula automáticamente el valor tasado.
     */
    private void calcularValorTasado() {
        try {
            double precioMercado = Double.parseDouble(txtPrecioMercado.getText().trim());
            double porcentaje = Double.parseDouble(txtPorcentajeTasacion.getText().trim());
            double valorTasado = precioMercado * (porcentaje / 100.0);
            txtValorTasado.setText(String.format("$%.2f", valorTasado));
        } catch (NumberFormatException e) {
            txtValorTasado.setText("$0.00");
        }
    }

    /**
     * Carga los datos del artículo en el formulario.
     */
    private void cargarDatosArticulo() {
        if (articulo != null) {
            txtId.setText(String.valueOf(articulo.getIdArticulo()));
            txtNombreArticulo.setText(articulo.getNombre()); // Usar getNombre()
            cmbTipo.setSelectedItem(articulo.getTipoArticulo());
            txtDescripcion.setText(articulo.getDescripcion());
            cmbEstado.setSelectedItem(articulo.getEstado());
            txtPrecioMercado.setText(String.valueOf(articulo.getPrecioMercadoBase()));
            txtPorcentajeTasacion.setText(String.valueOf(articulo.getPorcentajeTasacion()));
            txtValorTasado.setText(String.format("$%.2f", articulo.getValorTasado()));
            txtIdCliente.setText(String.valueOf(articulo.getIdCliente()));
            txtNombre.setText(articulo.getNombreCliente());
        }
    }

    /**
     * Guarda el artículo (nuevo o actualizado).
     */
    private void guardarArticulo() {
        // Validar campos obligatorios
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear o actualizar objeto Articulo
            if (esNuevo) {
                articulo = new Articulo();
                articulo.setIdArticulo(Integer.parseInt(txtId.getText().trim()));
            }

            articulo.setNombre(txtNombreArticulo.getText().trim()); // Usar setNombre()
            articulo.setTipoArticulo((String) cmbTipo.getSelectedItem());
            articulo.setDescripcion(txtDescripcion.getText().trim());
            articulo.setEstado((String) cmbEstado.getSelectedItem());
            articulo.setPrecioMercadoBase(Double.parseDouble(txtPrecioMercado.getText().trim()));
            articulo.setPorcentajeTasacion(Double.parseDouble(txtPorcentajeTasacion.getText().trim()));
            articulo.setIdCliente(Integer.parseInt(txtIdCliente.getText().trim()));
            articulo.setNombreCliente(txtNombre.getText().trim());

            // Calcular valor tasado
            double valorTasado = articulo.getPrecioMercadoBase() * (articulo.getPorcentajeTasacion() / 100.0);
            articulo.setValorTasado(valorTasado);

            // Validación: No permitir artículos defectuosos en inserción
            if (esNuevo && "DEFECTUOSO".equals(articulo.getEstado())) {
                JOptionPane.showMessageDialog(this,
                    "No se pueden registrar artículos con estado DEFECTUOSO.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Guardar en base de datos
            boolean exito;
            if (esNuevo) {
                exito = articuloDAO.insertarArticulo(articulo);
            } else {
                exito = articuloDAO.actualizarArticulo(articulo);
            }

            if (exito) {
                JOptionPane.showMessageDialog(this,
                    "Artículo " + (esNuevo ? "creado" : "actualizado") + " exitosamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                confirmado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo " + (esNuevo ? "crear" : "actualizar") + " el artículo.\nVerifique los datos e intente nuevamente.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Error en formato numérico. Verifique los valores ingresados.",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error inesperado: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.err.println("Error al guardar artículo: " + e.getMessage());
        }
    }

    /**
     * Valida los campos del formulario.
     *
     * @return true si todos los campos son válidos
     */
    private boolean validarCampos() {
        // Validar ID
        if (esNuevo && txtId.getText().trim().isEmpty()) {
            mostrarMensajeError("El ID del artículo es obligatorio.");
            txtId.requestFocus();
            return false;
        }
        try {
            if (esNuevo) {
                int id = Integer.parseInt(txtId.getText().trim());
                if (id <= 0) {
                    mostrarMensajeError("El ID debe ser un número positivo.");
                    txtId.requestFocus();
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El ID debe ser un número válido.");
            txtId.requestFocus();
            return false;
        }
        // Validar nombre del artículo
        if (txtNombreArticulo.getText().trim().isEmpty()) {
            mostrarMensajeError("El nombre del artículo es obligatorio.");
            txtNombreArticulo.requestFocus();
            return false;
        }
        // Validar descripción
        if (txtDescripcion.getText().trim().isEmpty()) {
            mostrarMensajeError("La descripción es obligatoria.");
            txtDescripcion.requestFocus();
            return false;
        }
        // Validar precio mercado
        if (txtPrecioMercado.getText().trim().isEmpty()) {
            mostrarMensajeError("El precio de mercado es obligatorio.");
            txtPrecioMercado.requestFocus();
            return false;
        }
        try {
            double precio = Double.parseDouble(txtPrecioMercado.getText().trim());
            if (precio <= 0) {
                mostrarMensajeError("El precio de mercado debe ser mayor a cero.");
                txtPrecioMercado.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El precio de mercado debe ser un número válido.");
            txtPrecioMercado.requestFocus();
            return false;
        }
        // Validar porcentaje tasación
        if (txtPorcentajeTasacion.getText().trim().isEmpty()) {
            mostrarMensajeError("El porcentaje de tasación es obligatorio.");
            txtPorcentajeTasacion.requestFocus();
            return false;
        }
        try {
            double porcentaje = Double.parseDouble(txtPorcentajeTasacion.getText().trim());
            if (porcentaje <= 0 || porcentaje > 100) {
                mostrarMensajeError("El porcentaje de tasación debe estar entre 0 y 100.");
                txtPorcentajeTasacion.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El porcentaje de tasación debe ser un número válido.");
            txtPorcentajeTasacion.requestFocus();
            return false;
        }
        // Validar ID Cliente
        if (txtIdCliente.getText().trim().isEmpty()) {
            mostrarMensajeError("El ID del cliente es obligatorio.");
            txtIdCliente.requestFocus();
            return false;
        }
        int idCliente;
        try {
            idCliente = Integer.parseInt(txtIdCliente.getText().trim());
            if (idCliente <= 0) {
                mostrarMensajeError("El ID del cliente debe ser un número positivo.");
                txtIdCliente.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El ID del cliente debe ser un número válido.");
            txtIdCliente.requestFocus();
            return false;
        }
        // Validar existencia del cliente en la BD
        if (clienteDAO.obtenerClientePorId(idCliente) == null) {
            mostrarMensajeError("El ID del cliente no existe en la base de datos. Debe ser un ID válido de CLIENTE (ID_PERSONA).");
            txtIdCliente.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarMensajeError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Validación",
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Verifica si el diálogo fue confirmado.
     *
     * @return true si se guardó el artículo
     */
    public boolean isConfirmado() {
        return confirmado;
    }

    /**
     * Busca y carga los datos del cliente en el formulario.
     */
    private void buscarCliente() {
        try {
            // Obtener lista de clientes
            java.util.List<com.prestamos.modelo.Cliente> clientes = clienteDAO.listarTodosClientes();

            if (clientes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay clientes registrados en el sistema.",
                    "Sin Clientes",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear lista para mostrar en el diálogo (ID_PERSONA)
            String[] opcionesClientes = new String[clientes.size()];
            for (int i = 0; i < clientes.size(); i++) {
                com.prestamos.modelo.Cliente cliente = clientes.get(i);
                opcionesClientes[i] = String.format("ID: %d - %s (Calificación: %.2f)",
                    cliente.getIdPersona(), // Usar ID_PERSONA
                    cliente.getNombrePersona(),
                    cliente.getCalificacion());
            }

            // Mostrar diálogo de selección
            String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un cliente:",
                "Buscar Cliente",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesClientes,
                opcionesClientes[0]
            );

            // Si seleccionó algo, llenar los campos
            if (seleccion != null) {
                int idx = java.util.Arrays.asList(opcionesClientes).indexOf(seleccion);
                com.prestamos.modelo.Cliente clienteSeleccionado = clientes.get(idx);
                txtIdCliente.setText(String.valueOf(clienteSeleccionado.getIdPersona())); // Usar ID_PERSONA
                txtNombre.setText(clienteSeleccionado.getNombrePersona());
                txtNombre.setEditable(false); // Bloquear edición del nombre
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar clientes: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.err.println("Error al buscar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
