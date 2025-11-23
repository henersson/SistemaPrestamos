package com.prestamos.util;

import com.prestamos.modelo.Articulo;
import com.prestamos.modelo.Cliente;
import com.prestamos.config.ConexionOracle;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Clase utilitaria para centralizar todas las validaciones de negocio del sistema.
 * Implementa las reglas de negocio definidas en el universo del discurso.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class ValidacionesNegocio {

    // Constantes de negocio
    private static final double PORCENTAJE_MAXIMO_PRESTAMO = 0.80; // 80% del valor del artículo
    private static final double CALIFICACION_MINIMA_CLIENTE = 3.0;
    private static final int PLAZO_MAXIMO_MESES = 12;
    private static final int DIAS_POR_MES = 30;

    /**
     * Excepción personalizada para errores de validación de negocio.
     */
    public static class ValidacionNegocioException extends Exception {
        public ValidacionNegocioException(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * Valida si un artículo puede ser usado para un préstamo.
     *
     * Reglas:
     * - No puede estar en estado DEFECTUOSO
     * - Debe tener valor tasado mayor a 0
     * - No debe estar ya en un préstamo activo
     *
     * @param articulo artículo a validar
     * @throws ValidacionNegocioException si la validación falla
     */
    public static void validarArticuloParaPrestamo(Articulo articulo) throws ValidacionNegocioException {
        if (articulo == null) {
            throw new ValidacionNegocioException("El artículo no puede ser nulo");
        }

        // Validación 1: Estado no defectuoso
        if ("DEFECTUOSO".equals(articulo.getEstado())) {
            throw new ValidacionNegocioException(
                "No se puede crear préstamo con artículo DEFECTUOSO. " +
                "Los artículos defectuosos no son aceptados como garantía."
            );
        }

        // Validación 2: Valor tasado mayor a 0
        if (articulo.getValorTasado() <= 0) {
            throw new ValidacionNegocioException(
                "El artículo debe tener un valor tasado mayor a $0.00. " +
                "Valor actual: $" + String.format("%.2f", articulo.getValorTasado())
            );
        }

        // Validación 3: No debe estar en préstamo activo
        if (estaArticuloEnPrestamoActivo(articulo.getIdArticulo())) {
            throw new ValidacionNegocioException(
                "El artículo ID " + articulo.getIdArticulo() + " ya está en un préstamo activo. " +
                "No se puede usar el mismo artículo para múltiples préstamos simultáneos."
            );
        }

        System.out.println("✓ Artículo validado correctamente para préstamo");
    }

    /**
     * Valida si el monto del préstamo es válido según el valor del artículo.
     *
     * Regla: El monto no puede exceder el 80% del valor tasado del artículo.
     *
     * @param monto monto del préstamo solicitado
     * @param valorArticulo valor tasado del artículo
     * @throws ValidacionNegocioException si el monto excede el límite permitido
     */
    public static void validarMontoPrestamo(double monto, double valorArticulo) throws ValidacionNegocioException {
        if (monto <= 0) {
            throw new ValidacionNegocioException(
                "El monto del préstamo debe ser mayor a $0.00"
            );
        }

        if (valorArticulo <= 0) {
            throw new ValidacionNegocioException(
                "El valor del artículo debe ser mayor a $0.00"
            );
        }

        double montoMaximo = calcularMontoMaximo(valorArticulo);

        if (monto > montoMaximo) {
            throw new ValidacionNegocioException(
                String.format(
                    "El monto del préstamo ($%.2f) excede el límite permitido.\n" +
                    "Valor del artículo: $%.2f\n" +
                    "Monto máximo permitido (80%%): $%.2f\n" +
                    "Diferencia: $%.2f",
                    monto, valorArticulo, montoMaximo, (monto - montoMaximo)
                )
            );
        }

        // Advertencia si el monto es muy bajo
        double montoPorcentaje = (monto / valorArticulo) * 100;
        if (montoPorcentaje < 30) {
            System.out.println("⚠ Advertencia: El monto solicitado es solo el " +
                String.format("%.1f%%", montoPorcentaje) + " del valor del artículo");
        }

        System.out.println("✓ Monto del préstamo validado correctamente");
    }

    /**
     * Valida la calificación del cliente para otorgar un préstamo.
     *
     * Regla: El cliente debe tener calificación >= 3.0 para ser elegible.
     *
     * @param cliente cliente a validar
     * @throws ValidacionNegocioException si el cliente no cumple con la calificación mínima
     */
    public static void validarCalificacionCliente(Cliente cliente) throws ValidacionNegocioException {
        if (cliente == null) {
            throw new ValidacionNegocioException("El cliente no puede ser nulo");
        }

        // Validar que el cliente esté activo
        if (cliente.getActivo() != 'S') {
            throw new ValidacionNegocioException(
                "El cliente ID " + cliente.getIdCliente() + " está inactivo. " +
                "No se pueden otorgar préstamos a clientes inactivos."
            );
        }

        // Validar calificación mínima
        double calificacion = cliente.getCalificacion();
        if (calificacion < CALIFICACION_MINIMA_CLIENTE) {
            throw new ValidacionNegocioException(
                String.format(
                    "El cliente no cumple con la calificación mínima requerida.\n" +
                    "Calificación actual: %.2f\n" +
                    "Calificación mínima: %.2f\n" +
                    "El cliente debe mejorar su historial de pagos antes de solicitar un nuevo préstamo.",
                    calificacion, CALIFICACION_MINIMA_CLIENTE
                )
            );
        }

        // Mensajes informativos según calificación
        if (calificacion >= 8.0) {
            System.out.println("✓ Cliente VIP - Calificación excelente: " + String.format("%.2f", calificacion));
        } else if (calificacion >= 5.0) {
            System.out.println("✓ Cliente con buena calificación: " + String.format("%.2f", calificacion));
        } else {
            System.out.println("⚠ Cliente con calificación mínima: " + String.format("%.2f", calificacion));
        }
    }

    /**
     * Valida las fechas de un préstamo.
     *
     * Reglas:
     * - Fecha fin debe ser posterior a fecha inicio
     * - Plazo máximo: 12 meses (360 días)
     * - Fecha inicio no puede ser futura (más de 1 día adelante)
     *
     * @param fechaInicio fecha de inicio del préstamo
     * @param fechaFin fecha de vencimiento del préstamo
     * @throws ValidacionNegocioException si las fechas no son válidas
     */
    public static void validarFechasPrestamo(Date fechaInicio, Date fechaFin) throws ValidacionNegocioException {
        if (fechaInicio == null || fechaFin == null) {
            throw new ValidacionNegocioException("Las fechas del préstamo no pueden ser nulas");
        }

        // Validación 1: Fecha fin posterior a fecha inicio
        if (!fechaFin.after(fechaInicio)) {
            throw new ValidacionNegocioException(
                "La fecha de vencimiento debe ser posterior a la fecha de inicio del préstamo"
            );
        }

        // Validación 2: Fecha inicio no muy futura
        Date hoy = new Date();
        long diferenciaInicio = fechaInicio.getTime() - hoy.getTime();
        long diasDiferenciaInicio = TimeUnit.MILLISECONDS.toDays(diferenciaInicio);

        if (diasDiferenciaInicio > 1) {
            throw new ValidacionNegocioException(
                "La fecha de inicio no puede ser más de 1 día en el futuro"
            );
        }

        // Validación 3: Plazo máximo
        long diferenciaPlazo = fechaFin.getTime() - fechaInicio.getTime();
        long diasPlazo = TimeUnit.MILLISECONDS.toDays(diferenciaPlazo);
        int plazoMaximoDias = PLAZO_MAXIMO_MESES * DIAS_POR_MES;

        if (diasPlazo > plazoMaximoDias) {
            throw new ValidacionNegocioException(
                String.format(
                    "El plazo del préstamo excede el máximo permitido.\n" +
                    "Plazo solicitado: %d días (%.1f meses)\n" +
                    "Plazo máximo: %d días (%d meses)",
                    diasPlazo, diasPlazo / 30.0, plazoMaximoDias, PLAZO_MAXIMO_MESES
                )
            );
        }

        // Advertencia para plazos muy cortos
        if (diasPlazo < 7) {
            System.out.println("⚠ Advertencia: Plazo muy corto (" + diasPlazo + " días)");
        }

        System.out.println("✓ Fechas del préstamo validadas correctamente (plazo: " + diasPlazo + " días)");
    }

    /**
     * Calcula el monto máximo que se puede prestar sobre un artículo.
     *
     * Regla: Máximo 80% del valor tasado del artículo.
     *
     * @param valorArticulo valor tasado del artículo
     * @return monto máximo permitido
     */
    public static double calcularMontoMaximo(double valorArticulo) {
        if (valorArticulo <= 0) {
            return 0;
        }
        return valorArticulo * PORCENTAJE_MAXIMO_PRESTAMO;
    }

    /**
     * Calcula el monto máximo recomendado (estrategia conservadora).
     *
     * @param articulo artículo en garantía
     * @return monto recomendado (70% del valor tasado)
     */
    public static double calcularMontoRecomendado(Articulo articulo) {
        if (articulo == null || articulo.getValorTasado() <= 0) {
            return 0;
        }

        double montoRecomendado = articulo.getValorTasado() * 0.70; // 70% conservador

        // Ajustar según estado del artículo
        String estado = articulo.getEstado();
        if ("OPTIMO".equals(estado)) {
            montoRecomendado = articulo.getValorTasado() * 0.75; // 75% para óptimos
        } else if ("FUNCIONABLE".equals(estado)) {
            montoRecomendado = articulo.getValorTasado() * 0.65; // 65% para funcionables
        }

        return montoRecomendado;
    }

    /**
     * Valida que un cliente no tenga demasiados préstamos activos.
     *
     * @param idCliente ID del cliente
     * @param limiteMaximo límite máximo de préstamos simultáneos (default: 5)
     * @throws ValidacionNegocioException si excede el límite
     */
    public static void validarLimitePrestamosActivos(int idCliente, int limiteMaximo)
            throws ValidacionNegocioException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT COUNT(*) AS TOTAL " +
                "FROM PRESTAMO " +
                "WHERE ID_CLIENTE = ? AND ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int prestamosActivos = rs.getInt("TOTAL");

                if (prestamosActivos >= limiteMaximo) {
                    throw new ValidacionNegocioException(
                        String.format(
                            "El cliente ya tiene %d préstamo(s) activo(s).\n" +
                            "Límite máximo permitido: %d\n" +
                            "Debe cancelar préstamos existentes antes de solicitar uno nuevo.",
                            prestamosActivos, limiteMaximo
                        )
                    );
                }

                if (prestamosActivos > 0) {
                    System.out.println("ℹ Cliente tiene " + prestamosActivos + " préstamo(s) activo(s)");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al validar límite de préstamos: " + e.getMessage());
            // No lanzar excepción para no bloquear el proceso
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }

    /**
     * Valida una tasa de interés.
     *
     * @param tasaInteres tasa de interés anual (porcentaje)
     * @throws ValidacionNegocioException si la tasa no es válida
     */
    public static void validarTasaInteres(double tasaInteres) throws ValidacionNegocioException {
        if (tasaInteres < 0) {
            throw new ValidacionNegocioException("La tasa de interés no puede ser negativa");
        }

        if (tasaInteres > 100) {
            throw new ValidacionNegocioException(
                "La tasa de interés (" + tasaInteres + "%) es excesivamente alta"
            );
        }

        // Advertencia para tasas inusuales
        if (tasaInteres < 1) {
            System.out.println("⚠ Advertencia: Tasa de interés muy baja (" + tasaInteres + "%)");
        } else if (tasaInteres > 50) {
            System.out.println("⚠ Advertencia: Tasa de interés muy alta (" + tasaInteres + "%)");
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Verifica si un artículo está en un préstamo activo.
     */
    private static boolean estaArticuloEnPrestamoActivo(int idArticulo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT COUNT(*) AS TOTAL " +
                "FROM PRESTAMO " +
                "WHERE ID_ARTICULO = ? AND ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idArticulo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("TOTAL") > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar artículo en préstamo: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, pstmt);
        }

        return false;
    }

    /**
     * Cierra recursos de base de datos de forma segura.
     */
    private static void cerrarRecursos(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar recursos: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS DE INFORMACIÓN ====================

    /**
     * Obtiene información de resumen sobre las validaciones disponibles.
     */
    public static String obtenerResumenValidaciones() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("═══════════════════════════════════════════════════════════\n");
        resumen.append("        REGLAS DE NEGOCIO DEL SISTEMA DE PRÉSTAMOS\n");
        resumen.append("═══════════════════════════════════════════════════════════\n\n");
        resumen.append("1. ARTÍCULOS:\n");
        resumen.append("   • No se aceptan artículos DEFECTUOSOS\n");
        resumen.append("   • Valor tasado debe ser > $0.00\n");
        resumen.append("   • No puede estar en préstamo activo\n\n");
        resumen.append("2. MONTOS DE PRÉSTAMO:\n");
        resumen.append("   • Máximo: 80% del valor tasado del artículo\n");
        resumen.append("   • Recomendado: 70% (conservador)\n");
        resumen.append("   • Óptimos: hasta 75%\n");
        resumen.append("   • Funcionables: hasta 65%\n\n");
        resumen.append("3. CLIENTES:\n");
        resumen.append("   • Calificación mínima: 3.0\n");
        resumen.append("   • Debe estar activo en el sistema\n");
        resumen.append("   • Límite de préstamos simultáneos: 5\n\n");
        resumen.append("4. PLAZOS:\n");
        resumen.append("   • Plazo máximo: 12 meses (360 días)\n");
        resumen.append("   • Plazo mínimo recomendado: 7 días\n");
        resumen.append("   • Fecha inicio: máximo 1 día futuro\n\n");
        resumen.append("5. TASAS DE INTERÉS:\n");
        resumen.append("   • Rango válido: 0% - 100%\n");
        resumen.append("   • Típica: 3% - 10% anual\n\n");
        resumen.append("═══════════════════════════════════════════════════════════\n");

        return resumen.toString();
    }

    /**
     * Obtiene las constantes de negocio.
     */
    public static String obtenerConstantesNegocio() {
        return String.format(
            "Porcentaje máximo préstamo: %.0f%%\n" +
            "Calificación mínima cliente: %.1f\n" +
            "Plazo máximo: %d meses\n",
            PORCENTAJE_MAXIMO_PRESTAMO * 100,
            CALIFICACION_MINIMA_CLIENTE,
            PLAZO_MAXIMO_MESES
        );
    }
}

