package com.prestamos.util;

import com.prestamos.modelo.Articulo;
import com.prestamos.modelo.Cliente;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Clase utilitaria para centralizar todas las validaciones de negocio
 * del sistema de casa de empeño.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class ValidacionesNegocio {

    /**
     * Valida que un artículo pueda ser usado como garantía en un préstamo.
     *
     * Reglas de negocio:
     * - El artículo no puede estar en estado DEFECTUOSO
     * - El artículo debe tener un valor tasado mayor a 0
     *
     * @param articulo el artículo a validar
     * @return true si el artículo es válido, false en caso contrario
     * @throws IllegalArgumentException si el artículo es null
     */
    public static boolean validarArticuloParaPrestamo(Articulo articulo) {
        if (articulo == null) {
            throw new IllegalArgumentException("El artículo no puede ser null");
        }

        // Validar que no sea defectuoso
        if ("DEFECTUOSO".equalsIgnoreCase(articulo.getEstado())) {
            System.err.println("✗ VALIDACIÓN FALLIDA: Los artículos defectuosos no se aceptan como garantía");
            return false;
        }

        // Validar que tenga valor tasado
        if (articulo.getValorTasado() <= 0) {
            System.err.println("✗ VALIDACIÓN FALLIDA: El artículo debe tener un valor tasado mayor a 0");
            return false;
        }

        System.out.println("✓ Artículo válido para préstamo");
        return true;
    }

    /**
     * Valida que el monto del préstamo no exceda el porcentaje permitido
     * del valor del artículo (máximo 80%).
     *
     * @param montoPrestamo el monto solicitado del préstamo
     * @param valorArticulo el valor tasado del artículo
     * @return true si el monto es válido, false en caso contrario
     */
    public static boolean validarMontoPrestamo(double montoPrestamo, double valorArticulo) {
        if (montoPrestamo <= 0) {
            System.err.println("✗ VALIDACIÓN FALLIDA: El monto del préstamo debe ser mayor a 0");
            return false;
        }

        if (valorArticulo <= 0) {
            System.err.println("✗ VALIDACIÓN FALLIDA: El valor del artículo debe ser mayor a 0");
            return false;
        }

        double montoMaximo = calcularMontoMaximo(valorArticulo);
        if (montoPrestamo > montoMaximo) {
            System.err.println("✗ VALIDACIÓN FALLIDA: El monto del préstamo ($" + montoPrestamo +
                             ") excede el máximo permitido ($" + montoMaximo + ")");
            return false;
        }

        System.out.println("✓ Monto del préstamo válido");
        return true;
    }

    /**
     * Valida que la calificación del cliente sea suficiente para solicitar un préstamo.
     * Se requiere una calificación mínima de 3.0
     *
     * @param cliente el cliente a validar
     * @return true si la calificación es suficiente, false en caso contrario
     */
    public static boolean validarCalificacionCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }

        final double CALIFICACION_MINIMA = 3.0;

        if (cliente.getCalificacion() < CALIFICACION_MINIMA) {
            System.err.println("✗ VALIDACIÓN FALLIDA: El cliente requiere una calificación mínima de " +
                             CALIFICACION_MINIMA + ". Calificación actual: " + cliente.getCalificacion());
            return false;
        }

        System.out.println("✓ Calificación del cliente suficiente: " + cliente.getCalificacion());
        return true;
    }

    /**
     * Valida que las fechas del préstamo sean coherentes.
     *
     * Reglas:
     * - La fecha de vencimiento debe ser posterior a la fecha de inicio
     * - El plazo máximo es de 12 meses (365 días)
     *
     * @param fechaInicio fecha de inicio del préstamo
     * @param fechaVencimiento fecha de vencimiento del préstamo
     * @return true si las fechas son válidas, false en caso contrario
     */
    public static boolean validarFechasPrestamo(Date fechaInicio, Date fechaVencimiento) {
        if (fechaInicio == null || fechaVencimiento == null) {
            System.err.println("✗ VALIDACIÓN FALLIDA: Las fechas no pueden ser null");
            return false;
        }

        // Validar que fecha vencimiento sea posterior a fecha inicio
        if (!fechaVencimiento.after(fechaInicio)) {
            System.err.println("✗ VALIDACIÓN FALLIDA: La fecha de vencimiento debe ser posterior a la fecha de inicio");
            return false;
        }

        // Calcular días de diferencia
        long diferenciaMilisegundos = fechaVencimiento.getTime() - fechaInicio.getTime();
        long diasDiferencia = TimeUnit.MILLISECONDS.toDays(diferenciaMilisegundos);

        // Validar plazo máximo de 12 meses (365 días)
        final long PLAZO_MAXIMO_DIAS = 365;
        if (diasDiferencia > PLAZO_MAXIMO_DIAS) {
            System.err.println("✗ VALIDACIÓN FALLIDA: El plazo máximo es de " + PLAZO_MAXIMO_DIAS +
                             " días. Plazo solicitado: " + diasDiferencia + " días");
            return false;
        }

        System.out.println("✓ Fechas del préstamo válidas. Plazo: " + diasDiferencia + " días");
        return true;
    }

    /**
     * Calcula el monto máximo que se puede prestar basado en el valor del artículo.
     * El monto máximo es el 80% del valor tasado.
     *
     * @param valorArticulo valor tasado del artículo
     * @return el monto máximo permitido para el préstamo
     */
    public static double calcularMontoMaximo(double valorArticulo) {
        final double PORCENTAJE_MAXIMO = 0.80; // 80%
        return valorArticulo * PORCENTAJE_MAXIMO;
    }

    /**
     * Calcula la tasa de interés según el plazo del préstamo.
     *
     * Reglas:
     * - Menos de 3 meses (90 días) → 5%
     * - Entre 3 y 6 meses (90-180 días) → 10%
     * - Más de 6 meses (más de 180 días) → 15%
     *
     * @param fechaInicio fecha de inicio del préstamo
     * @param fechaVencimiento fecha de vencimiento del préstamo
     * @return la tasa de interés aplicable
     */
    public static double calcularTasaInteres(Date fechaInicio, Date fechaVencimiento) {
        long diferenciaMilisegundos = fechaVencimiento.getTime() - fechaInicio.getTime();
        long diasDiferencia = TimeUnit.MILLISECONDS.toDays(diferenciaMilisegundos);

        if (diasDiferencia < 90) {
            return 0.05; // 5%
        } else if (diasDiferencia <= 180) {
            return 0.10; // 10%
        } else {
            return 0.15; // 15%
        }
    }

    /**
     * Calcula el interés generado por un préstamo.
     *
     * @param monto monto del préstamo
     * @param tasaInteres tasa de interés (ejemplo: 0.05 para 5%)
     * @return el monto del interés generado
     */
    public static double calcularInteresGenerado(double monto, double tasaInteres) {
        return monto * tasaInteres;
    }

    /**
     * Calcula la multa por mora (2% adicional sobre el saldo).
     *
     * @param saldoPendiente saldo pendiente del préstamo
     * @return el monto de la multa
     */
    public static double calcularMultaPorMora(double saldoPendiente) {
        final double PORCENTAJE_MULTA = 0.02; // 2%
        return saldoPendiente * PORCENTAJE_MULTA;
    }

    /**
     * Calcula el total a pagar de un préstamo (monto + interés + multa).
     *
     * @param monto monto principal del préstamo
     * @param interes interés generado
     * @param multa multa aplicada
     * @return el total a pagar
     */
    public static double calcularTotalAPagar(double monto, double interes, double multa) {
        return monto + interes + multa;
    }
}
