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
    private JComboBox<String> cmbTipo;
    private JTextArea txtDescripcion;
    private JComboBox<String> cmbEstado;
    private JTextField txtValorTasado;
    private JTextField txtPrecioMercado;
    private JTextField txtPorcentajeTasacion;
    private JSpinner dateAvaluo;
    private JButton btnGuardar;
    private JButton btnCancelar;

    // DAO
    private final ArticuloDAO articuloDAO = new ArticuloDAO();

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

        // Fila 0: ID
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

        // Fila 1: Tipo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Tipo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cmbTipo = new JComboBox<>(new String[]{
            "ELECTRODOMESTICO", "JOYA", "HERRAMIENTA", "VEHICULO", "ELECTRONICO", "OTRO"
        });
        cmbTipo.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbTipo.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(cmbTipo, gbc);

        // Fila 2: Descripción
        gbc.gridx = 0;
        gbc.gridy = 2;
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

        // Fila 3: Estado
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Estado:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cmbEstado = new JComboBox<>(new String[]{
            "OPTIMO", "FUNCIONABLE", "DEFECTUOSO", "PROPIEDAD_CASA"
        });
        cmbEstado.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEstado.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(cmbEstado, gbc);

        // Fila 4: Precio Mercado
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Precio Mercado:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPrecioMercado = new JTextField();
        txtPrecioMercado.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPrecioMercado.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtPrecioMercado, gbc);

        // Fila 5: Porcentaje Tasación
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*% Tasación:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPorcentajeTasacion = new JTextField("70.0");
        txtPorcentajeTasacion.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPorcentajeTasacion.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtPorcentajeTasacion, gbc);

        // Fila 6: Valor Tasado (calculado)
        gbc.gridx = 0;
        gbc.gridy = 6;
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

        // Fila 7: Fecha Avalúo
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Fecha Avalúo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateAvaluo = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateAvaluo, "dd/MM/yyyy");
        dateAvaluo.setEditor(dateEditor);
        dateAvaluo.setFont(new Font("Arial", Font.PLAIN, 14));
        dateAvaluo.setPreferredSize(new Dimension(0, 35));
        dateAvaluo.setValue(new Date());
        panelFormulario.add(dateAvaluo, gbc);

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
            cmbTipo.setSelectedItem(articulo.getTipoArticulo());
            txtDescripcion.setText(articulo.getDescripcion());
            cmbEstado.setSelectedItem(articulo.getEstado());
            txtPrecioMercado.setText(String.valueOf(articulo.getPrecioMercadoBase()));
            txtPorcentajeTasacion.setText(String.valueOf(articulo.getPorcentajeTasacion()));
            txtValorTasado.setText(String.format("$%.2f", articulo.getValorTasado()));

            if (articulo.getFechaAvaluo() != null) {
                dateAvaluo.setValue(articulo.getFechaAvaluo());
            }
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

            articulo.setTipoArticulo((String) cmbTipo.getSelectedItem());
            articulo.setDescripcion(txtDescripcion.getText().trim());
            articulo.setEstado((String) cmbEstado.getSelectedItem());
            articulo.setPrecioMercadoBase(Double.parseDouble(txtPrecioMercado.getText().trim()));
            articulo.setPorcentajeTasacion(Double.parseDouble(txtPorcentajeTasacion.getText().trim()));

            // Calcular valor tasado
            double valorTasado = articulo.getPrecioMercadoBase() * (articulo.getPorcentajeTasacion() / 100.0);
            articulo.setValorTasado(valorTasado);

            articulo.setFechaAvaluo((Date) dateAvaluo.getValue());

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

        // Validar fecha
        if (dateAvaluo.getValue() == null) {
            mostrarMensajeError("La fecha de avalúo es obligatoria.");
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
}
