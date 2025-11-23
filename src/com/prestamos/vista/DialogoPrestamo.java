package com.prestamos.vista;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;
import com.prestamos.modelo.Prestamo;
import com.prestamos.dao.PrestamoDAO;
import com.prestamos.dao.ClienteDAO;
import com.prestamos.dao.ArticuloDAO;

/**
 * Diálogo para crear nuevos préstamos.
 * Incluye validaciones completas y cálculo automático de intereses.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class DialogoPrestamo extends JDialog {

    // Componentes
    private JTextField txtIdPrestamo;
    private JTextField txtIdCliente;
    private JTextField txtIdArticulo;
    private JTextField txtIdAsesor;
    private JTextField txtMonto;
    private JTextField txtTasaInteres;
    private JSpinner spinnerPlazo;
    private JSpinner dateFechaPrestamo;
    private JLabel lblFechaVencimiento;
    private JLabel lblInteresCalculado;
    private JTextArea txtVistaPrevia;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JButton btnBuscarCliente;
    private JButton btnBuscarArticulo;

    // DAO
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ArticuloDAO articuloDAO = new ArticuloDAO();

    // Control
    private boolean confirmado = false;
    private Prestamo prestamo;

    /**
     * Constructor para crear nuevo préstamo.
     *
     * @param parent frame padre
     * @param prestamo préstamo a editar (null para nuevo)
     */
    public DialogoPrestamo(Frame parent, Prestamo prestamo) {
        super(parent, "Nuevo Préstamo", true);
        this.prestamo = prestamo;

        initComponents();

        setSize(700, 750);
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

        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblTitulo = new JLabel("DATOS DEL PRÉSTAMO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panelFormulario.add(lblTitulo, gbc);

        gbc.gridwidth = 1;

        // Fila 1: ID Préstamo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*ID Préstamo:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        txtIdPrestamo = new JTextField();
        txtIdPrestamo.setFont(new Font("Arial", Font.PLAIN, 14));
        txtIdPrestamo.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtIdPrestamo, gbc);

        gbc.gridwidth = 1;

        // Fila 2: ID Cliente
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*ID Cliente:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        txtIdCliente = new JTextField();
        txtIdCliente.setFont(new Font("Arial", Font.PLAIN, 14));
        txtIdCliente.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtIdCliente, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        btnBuscarCliente = crearBotonIcono("Buscar", new Color(52, 152, 219));
        panelFormulario.add(btnBuscarCliente, gbc);

        // Fila 3: ID Artículo
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*ID Artículo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        txtIdArticulo = new JTextField();
        txtIdArticulo.setFont(new Font("Arial", Font.PLAIN, 14));
        txtIdArticulo.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtIdArticulo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        btnBuscarArticulo = crearBotonIcono("Buscar", new Color(46, 204, 113));
        panelFormulario.add(btnBuscarArticulo, gbc);

        // Fila 4: ID Asesor
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*ID Asesor:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        txtIdAsesor = new JTextField();
        txtIdAsesor.setFont(new Font("Arial", Font.PLAIN, 14));
        txtIdAsesor.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtIdAsesor, gbc);

        gbc.gridwidth = 1;

        // Fila 5: Monto
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Monto:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        txtMonto = new JTextField();
        txtMonto.setFont(new Font("Arial", Font.PLAIN, 14));
        txtMonto.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtMonto, gbc);

        gbc.gridwidth = 1;

        // Fila 6: Tasa Interés
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Tasa Interés (%):"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        txtTasaInteres = new JTextField("5.0");
        txtTasaInteres.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTasaInteres.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(txtTasaInteres, gbc);

        gbc.gridwidth = 1;

        // Fila 7: Fecha Préstamo
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Fecha Préstamo:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateFechaPrestamo = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateFechaPrestamo, "dd/MM/yyyy");
        dateFechaPrestamo.setEditor(dateEditor);
        dateFechaPrestamo.setFont(new Font("Arial", Font.PLAIN, 14));
        dateFechaPrestamo.setPreferredSize(new Dimension(0, 35));
        dateFechaPrestamo.setValue(new Date());
        panelFormulario.add(dateFechaPrestamo, gbc);

        gbc.gridwidth = 1;

        // Fila 8: Plazo (días)
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("*Plazo (días):"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        SpinnerNumberModel numberModel = new SpinnerNumberModel(30, 1, 365, 1);
        spinnerPlazo = new JSpinner(numberModel);
        spinnerPlazo.setFont(new Font("Arial", Font.PLAIN, 14));
        spinnerPlazo.setPreferredSize(new Dimension(0, 35));
        panelFormulario.add(spinnerPlazo, gbc);

        gbc.gridwidth = 1;

        // Fila 9: Fecha Vencimiento (calculada)
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("Fecha Vencimiento:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        lblFechaVencimiento = new JLabel("Calculando...");
        lblFechaVencimiento.setFont(new Font("Arial", Font.BOLD, 14));
        lblFechaVencimiento.setForeground(new Color(41, 128, 185));
        panelFormulario.add(lblFechaVencimiento, gbc);

        gbc.gridwidth = 1;

        // Fila 10: Interés Calculado
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.weightx = 0.3;
        panelFormulario.add(crearLabel("Interés Generado:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        lblInteresCalculado = new JLabel("$0.00");
        lblInteresCalculado.setFont(new Font("Arial", Font.BOLD, 14));
        lblInteresCalculado.setForeground(new Color(46, 204, 113));
        panelFormulario.add(lblInteresCalculado, gbc);

        gbc.gridwidth = 1;

        // Fila 11: Vista Previa
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        JLabel lblVistaPrevia = crearLabel("Vista Previa:");
        panelFormulario.add(lblVistaPrevia, gbc);

        gbc.gridy = 12;
        txtVistaPrevia = new JTextArea(5, 30);
        txtVistaPrevia.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtVistaPrevia.setEditable(false);
        txtVistaPrevia.setBackground(new Color(236, 240, 241));
        txtVistaPrevia.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollVistaPrevia = new JScrollPane(txtVistaPrevia);
        scrollVistaPrevia.setPreferredSize(new Dimension(0, 120));
        panelFormulario.add(scrollVistaPrevia, gbc);

        // Listeners para cálculo automático
        txtMonto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularVistaPrevia();
            }
        });

        txtTasaInteres.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularVistaPrevia();
            }
        });

        spinnerPlazo.addChangeListener(e -> calcularVistaPrevia());
        dateFechaPrestamo.addChangeListener(e -> calcularVistaPrevia());

        btnBuscarCliente.addActionListener(e -> buscarCliente());

        btnBuscarArticulo.addActionListener(e -> buscarArticulo());

        add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));

        btnGuardar = crearBoton("Crear Préstamo", new Color(46, 204, 113));
        btnCancelar = crearBoton("Cancelar", new Color(149, 165, 166));

        btnGuardar.addActionListener(e -> guardarPrestamo());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        // Nota de campos obligatorios
        JLabel lblNota = new JLabel("* Campos obligatorios");
        lblNota.setFont(new Font("Arial", Font.ITALIC, 12));
        lblNota.setForeground(new Color(231, 76, 60));
        lblNota.setBorder(BorderFactory.createEmptyBorder(10, 30, 0, 0));
        add(lblNota, BorderLayout.NORTH);

        // Calcular vista previa inicial
        calcularVistaPrevia();
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
        boton.setPreferredSize(new Dimension(150, 35));
        return boton;
    }

    /**
     * Crea un botón pequeño con icono.
     */
    private JButton crearBotonIcono(String icono, Color color) {
        JButton boton = new JButton(icono);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(70, 35));
        return boton;
    }

    /**
     * Calcula y muestra la vista previa del préstamo.
     */
    private void calcularVistaPrevia() {
        try {
            double monto = txtMonto.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtMonto.getText().trim());
            double tasa = txtTasaInteres.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtTasaInteres.getText().trim());
            int plazo = (int) spinnerPlazo.getValue();
            Date fechaPrestamo = (Date) dateFechaPrestamo.getValue();

            // Calcular fecha vencimiento
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaPrestamo);
            cal.add(Calendar.DAY_OF_MONTH, plazo);
            Date fechaVencimiento = cal.getTime();

            // Calcular interés (fórmula simple: Monto * Tasa * Plazo / 365)
            double interes = monto * (tasa / 100.0) * (plazo / 365.0);
            double totalPagar = monto + interes;

            // Actualizar labels
            lblFechaVencimiento.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
            lblInteresCalculado.setText(String.format("$%.2f", interes));

            // Crear vista previa
            StringBuilder preview = new StringBuilder();
            preview.append("═══════════════════════════════════\n");
            preview.append("       RESUMEN DEL PRÉSTAMO\n");
            preview.append("═══════════════════════════════════\n");
            preview.append(String.format("Monto:          $%,.2f\n", monto));
            preview.append(String.format("Tasa:           %.2f%% anual\n", tasa));
            preview.append(String.format("Plazo:          %d días\n", plazo));
            preview.append(String.format("Interés:        $%,.2f\n", interes));
            preview.append("─────────────────────────────────\n");
            preview.append(String.format("TOTAL A PAGAR:  $%,.2f\n", totalPagar));
            preview.append("═══════════════════════════════════\n");

            txtVistaPrevia.setText(preview.toString());

        } catch (NumberFormatException e) {
            lblInteresCalculado.setText("$0.00");
            txtVistaPrevia.setText("Ingrese valores válidos para calcular...");
        }
    }

    /**
     * Guarda el préstamo con todas las validaciones.
     */
    private void guardarPrestamo() {
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear objeto Prestamo
            prestamo = new Prestamo();
            prestamo.setIdPrestamo(Integer.parseInt(txtIdPrestamo.getText().trim()));
            prestamo.setIdCliente(Integer.parseInt(txtIdCliente.getText().trim()));
            prestamo.setIdArticulo(Integer.parseInt(txtIdArticulo.getText().trim()));
            prestamo.setIdAsesor(Integer.parseInt(txtIdAsesor.getText().trim()));
            prestamo.setMonto(Double.parseDouble(txtMonto.getText().trim()));
            prestamo.setTasaInteres(Double.parseDouble(txtTasaInteres.getText().trim()));
            prestamo.setFechaPrestamo((Date) dateFechaPrestamo.getValue());

            // Calcular fecha vencimiento
            int plazo = (int) spinnerPlazo.getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(prestamo.getFechaPrestamo());
            cal.add(Calendar.DAY_OF_MONTH, plazo);
            prestamo.setFechaVencimiento(cal.getTime());

            // Guardar en base de datos
            if (prestamoDAO.crearPrestamo(prestamo)) {
                JOptionPane.showMessageDialog(this,
                    "Préstamo creado exitosamente.\nID: " + prestamo.getIdPrestamo(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                confirmado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo crear el préstamo.\nVerifique:\n" +
                    "- Cliente existe y está activo\n" +
                    "- Artículo no es defectuoso\n" +
                    "- Monto no excede valor del artículo",
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
        }
    }

    /**
     * Valida los campos del formulario.
     */
    private boolean validarCampos() {
        // Validar ID Préstamo
        if (txtIdPrestamo.getText().trim().isEmpty()) {
            mostrarMensajeError("El ID del préstamo es obligatorio.");
            txtIdPrestamo.requestFocus();
            return false;
        }

        try {
            int id = Integer.parseInt(txtIdPrestamo.getText().trim());
            if (id <= 0) {
                mostrarMensajeError("El ID debe ser un número positivo.");
                txtIdPrestamo.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El ID debe ser un número válido.");
            txtIdPrestamo.requestFocus();
            return false;
        }

        // Validar ID Cliente
        if (txtIdCliente.getText().trim().isEmpty()) {
            mostrarMensajeError("El ID del cliente es obligatorio.");
            txtIdCliente.requestFocus();
            return false;
        }

        // Validar ID Artículo
        if (txtIdArticulo.getText().trim().isEmpty()) {
            mostrarMensajeError("Debe seleccionar un artículo disponible para el préstamo.");
            txtIdArticulo.requestFocus();
            return false;
        }
        int idArticulo;
        try {
            idArticulo = Integer.parseInt(txtIdArticulo.getText().trim());
            if (idArticulo <= 0) {
                mostrarMensajeError("Debe seleccionar un artículo válido (ID > 0).");
                txtIdArticulo.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El ID del artículo debe ser un número válido.");
            txtIdArticulo.requestFocus();
            return false;
        }

        // Validar ID Asesor
        if (txtIdAsesor.getText().trim().isEmpty()) {
            mostrarMensajeError("El ID del asesor es obligatorio.");
            txtIdAsesor.requestFocus();
            return false;
        }

        // Validar Monto
        if (txtMonto.getText().trim().isEmpty()) {
            mostrarMensajeError("El monto es obligatorio.");
            txtMonto.requestFocus();
            return false;
        }

        try {
            double monto = Double.parseDouble(txtMonto.getText().trim());
            if (monto <= 0) {
                mostrarMensajeError("El monto debe ser mayor a cero.");
                txtMonto.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("El monto debe ser un número válido.");
            txtMonto.requestFocus();
            return false;
        }

        // Validar Tasa Interés
        if (txtTasaInteres.getText().trim().isEmpty()) {
            mostrarMensajeError("La tasa de interés es obligatoria.");
            txtTasaInteres.requestFocus();
            return false;
        }

        try {
            double tasa = Double.parseDouble(txtTasaInteres.getText().trim());
            if (tasa < 0 || tasa > 100) {
                mostrarMensajeError("La tasa de interés debe estar entre 0 y 100.");
                txtTasaInteres.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("La tasa de interés debe ser un número válido.");
            txtTasaInteres.requestFocus();
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
     */
    public boolean isConfirmado() {
        return confirmado;
    }

    /**
     * Abre un diálogo para buscar y seleccionar un cliente.
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

            // Crear lista para mostrar en el diálogo
            String[] opcionesClientes = new String[clientes.size()];
            for (int i = 0; i < clientes.size(); i++) {
                com.prestamos.modelo.Cliente cliente = clientes.get(i);
                opcionesClientes[i] = String.format("ID: %d - %s (Calificación: %.2f)",
                    cliente.getIdCliente(),
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

            // Si seleccionó algo, extraer el ID
            if (seleccion != null) {
                int idCliente = clientes.get(java.util.Arrays.asList(opcionesClientes).indexOf(seleccion)).getIdCliente();
                txtIdCliente.setText(String.valueOf(idCliente));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar clientes: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre un diálogo para buscar y seleccionar un artículo.
     */
    private void buscarArticulo() {
        try {
            // Verificar que se haya seleccionado un cliente primero
            if (txtIdCliente.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione primero un cliente.",
                    "Cliente Requerido",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idCliente = Integer.parseInt(txtIdCliente.getText().trim());

            // Obtener artículos disponibles del cliente
            java.util.List<com.prestamos.modelo.Articulo> articulos = articuloDAO.listarArticulosPorCliente(idCliente);

            // Filtrar solo artículos disponibles
            java.util.List<com.prestamos.modelo.Articulo> articulosDisponibles = new java.util.ArrayList<>();
            for (com.prestamos.modelo.Articulo art : articulos) {
                if ("DISPONIBLE".equalsIgnoreCase(art.getEstado())) {
                    articulosDisponibles.add(art);
                }
            }

            if (articulosDisponibles.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "El cliente no tiene artículos disponibles para empeñar.",
                    "Sin Artículos",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear lista para mostrar en el diálogo
            String[] opcionesArticulos = new String[articulosDisponibles.size()];
            for (int i = 0; i < articulosDisponibles.size(); i++) {
                com.prestamos.modelo.Articulo articulo = articulosDisponibles.get(i);
                opcionesArticulos[i] = String.format("ID: %d - %s (%s) - Valor: $%.2f",
                    articulo.getIdArticulo(),
                    articulo.getNombre(),
                    articulo.getTipoArticulo(),
                    articulo.getValorTasado());
            }

            // Mostrar diálogo de selección
            String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un artículo:",
                "Buscar Artículo",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesArticulos,
                opcionesArticulos[0]
            );

            // Si seleccionó algo, extraer el ID y sugerir monto
            if (seleccion != null) {
                com.prestamos.modelo.Articulo articuloSeleccionado =
                    articulosDisponibles.get(java.util.Arrays.asList(opcionesArticulos).indexOf(seleccion));

                txtIdArticulo.setText(String.valueOf(articuloSeleccionado.getIdArticulo()));

                // Sugerir monto (80% del valor tasado)
                double montoSugerido = articuloSeleccionado.getValorTasado() * 0.8;
                txtMonto.setText(String.format("%.2f", montoSugerido));

                // Recalcular vista previa
                calcularVistaPrevia();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "ID de cliente inválido.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar artículos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
