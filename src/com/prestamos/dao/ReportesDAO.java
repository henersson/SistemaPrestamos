package com.prestamos.dao;

import com.prestamos.config.ConexionOracle;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DAO para la generación de reportes del sistema de préstamos.
 * Proporciona métodos para generar diferentes tipos de reportes conectados a Oracle.
 *
 * @author Henersson Cobo
 * @version 2.0
 * @since 2025-11-23
 */
public class ReportesDAO {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat sdfCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera reporte de prestamos vencidos con detalles completos
     * @return String formateado con el reporte
     */
    public String generarReportePrestamosVencidos() {
        StringBuilder reporte = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT p.ID_PRESTAMO, " +
                "       p.MONTO, " +
                "       p.INTERES_GENERADO, " +
                "       NVL(p.MULTA, 0) AS MULTA, " +
                "       TO_CHAR(p.FECHA_PRESTAMO, 'DD/MM/YYYY') AS FECHA_PREST, " +
                "       TO_CHAR(p.FECHA_VENCIMIENTO, 'DD/MM/YYYY') AS FECHA_VENC, " +
                "       per.NOMBRE_PERSONA AS CLIENTE, " +
                "       ases.NOMBRE_PERSONA AS ASESOR, " +
                "       TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_VENCIDO, " +
                "       (p.MONTO + p.INTERES_GENERADO + NVL(p.MULTA, 0)) AS TOTAL_DEUDA " +
                "FROM PRESTAMO p " +
                "JOIN PERSONA per ON p.ID_CLIENTE = per.ID_PERSONA " +
                "JOIN PERSONA ases ON p.ID_ASESOR = ases.ID_PERSONA " +
                "WHERE p.ESTADO_PRESTAMO = 'VENCIDO' " +
                "ORDER BY DIAS_VENCIDO DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // Encabezado del reporte
            reporte.append("===============================================================================\n");
            reporte.append("               REPORTE DE PRESTAMOS VENCIDOS - CASA DE EMPENO                 \n");
            reporte.append("===============================================================================\n");
            reporte.append(String.format("Fecha de generacion: %s\n", sdfCompleto.format(new Date())));
            reporte.append("Usuario: Sistema\n\n");

            // Encabezados de columnas
            reporte.append(String.format("%-6s %-20s %-20s %-12s %-10s %-8s %-14s\n",
                "ID", "Cliente", "Asesor", "Monto", "Interes", "Dias", "Total Deuda"));
            reporte.append("-------------------------------------------------------------------------------\n");

            // Datos
            int contador = 0;
            double sumaMontos = 0;
            double sumaIntereses = 0;
            double sumaMultas = 0;
            double sumaTotales = 0;

            while (rs.next()) {
                contador++;
                int idPrestamo = rs.getInt("ID_PRESTAMO");
                String cliente = rs.getString("CLIENTE");
                String asesor = rs.getString("ASESOR");
                double monto = rs.getDouble("MONTO");
                double interes = rs.getDouble("INTERES_GENERADO");
                double multa = rs.getDouble("MULTA");
                int diasVencido = rs.getInt("DIAS_VENCIDO");
                double totalDeuda = rs.getDouble("TOTAL_DEUDA");

                // Truncar nombres si son muy largos
                if (cliente.length() > 18) cliente = cliente.substring(0, 18) + "..";
                if (asesor.length() > 18) asesor = asesor.substring(0, 18) + "..";

                reporte.append(String.format("%-6d %-20s %-20s $%,10.2f $%,8.2f %6d   $%,12.2f\n",
                    idPrestamo, cliente, asesor, monto, interes, diasVencido, totalDeuda));

                sumaMontos += monto;
                sumaIntereses += interes;
                sumaMultas += multa;
                sumaTotales += totalDeuda;
            }

            // Totales
            reporte.append("-------------------------------------------------------------------------------\n");
            reporte.append(String.format("TOTALES:                           $%,10.2f $%,8.2f          $%,12.2f\n",
                sumaMontos, sumaIntereses, sumaTotales));
            reporte.append("-------------------------------------------------------------------------------\n\n");

            // Resumen
            reporte.append("RESUMEN:\n");
            reporte.append(String.format("  - Total de prestamos vencidos: %d\n", contador));
            reporte.append(String.format("  - Total prestado: $%,.2f\n", sumaMontos));
            reporte.append(String.format("  - Total intereses: $%,.2f\n", sumaIntereses));
            reporte.append(String.format("  - Total multas: $%,.2f\n", sumaMultas));
            reporte.append(String.format("  - DEUDA TOTAL: $%,.2f\n", sumaTotales));

            if (contador == 0) {
                reporte.append("\nNo hay prestamos vencidos en este momento.\n");
            }

            reporte.append("\n===============================================================================\n");

            System.out.println("Reporte de prestamos vencidos generado: " + contador + " registros");

        } catch (SQLException e) {
            System.err.println("Error generando reporte de prestamos vencidos: " + e.getMessage());
            e.printStackTrace();
            return "ERROR al generar reporte:\n" + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reporte.toString();
    }

    /**
     * Genera reporte de clientes VIP (calificacion >= 8.0)
     * @return String formateado con el reporte
     */
    public String generarReporteClientesVIP() {
        StringBuilder reporte = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT p.NOMBRE_PERSONA, " +
                "       c.CALIFICACION, " +
                "       COUNT(pr.ID_PRESTAMO) AS NUM_PRESTAMOS, " +
                "       NVL(SUM(CASE WHEN pr.ESTADO_PRESTAMO = 'CANCELADO' THEN 1 ELSE 0 END), 0) AS PRESTAMOS_PAGADOS, " +
                "       NVL(SUM(CASE WHEN pr.ESTADO_PRESTAMO = 'ACTIVO' THEN 1 ELSE 0 END), 0) AS PRESTAMOS_ACTIVOS, " +
                "       NVL(SUM(pr.MONTO), 0) AS MONTO_TOTAL " +
                "FROM CLIENTE c " +
                "JOIN PERSONA p ON c.ID_PERSONA = p.ID_PERSONA " +
                "LEFT JOIN PRESTAMO pr ON c.ID_PERSONA = pr.ID_CLIENTE " +
                "WHERE c.CALIFICACION >= 8.0 AND c.ACTIVO = 'S' " +
                "GROUP BY p.NOMBRE_PERSONA, c.CALIFICACION " +
                "ORDER BY c.CALIFICACION DESC, MONTO_TOTAL DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // Encabezado
            reporte.append("===============================================================================\n");
            reporte.append("                  REPORTE DE CLIENTES VIP - CASA DE EMPENO                    \n");
            reporte.append("===============================================================================\n");
            reporte.append(String.format("Fecha de generacion: %s\n", sdfCompleto.format(new Date())));
            reporte.append("Criterio: Clientes activos con calificacion >= 8.0\n\n");

            // Columnas
            reporte.append(String.format("%-30s %-12s %-10s %-10s %-10s %-14s\n",
                "Cliente", "Calific.", "Prestamos", "Pagados", "Activos", "Monto Total"));
            reporte.append("-------------------------------------------------------------------------------\n");

            // Datos
            int contador = 0;
            double sumaMontos = 0;
            int totalPrestamos = 0;

            while (rs.next()) {
                contador++;
                String nombre = rs.getString("NOMBRE_PERSONA");
                double calificacion = rs.getDouble("CALIFICACION");
                int numPrestamos = rs.getInt("NUM_PRESTAMOS");
                int prestamosPagados = rs.getInt("PRESTAMOS_PAGADOS");
                int prestamosActivos = rs.getInt("PRESTAMOS_ACTIVOS");
                double montoTotal = rs.getDouble("MONTO_TOTAL");

                if (nombre.length() > 28) nombre = nombre.substring(0, 28) + "..";

                reporte.append(String.format("%-30s %10.2f %10d %10d %10d   $%,12.2f\n",
                    nombre, calificacion, numPrestamos, prestamosPagados, prestamosActivos, montoTotal));

                sumaMontos += montoTotal;
                totalPrestamos += numPrestamos;
            }

            // Totales
            reporte.append("-------------------------------------------------------------------------------\n");
            reporte.append(String.format("TOTALES:                                 %10d                    $%,12.2f\n",
                totalPrestamos, sumaMontos));
            reporte.append("-------------------------------------------------------------------------------\n\n");

            // Resumen
            reporte.append("RESUMEN:\n");
            reporte.append(String.format("  - Total de clientes VIP: %d\n", contador));
            reporte.append(String.format("  - Total de prestamos gestionados: %d\n", totalPrestamos));
            reporte.append(String.format("  - Monto total prestado: $%,.2f\n", sumaMontos));

            if (contador > 0) {
                reporte.append(String.format("  - Promedio por cliente: $%,.2f\n", sumaMontos / contador));
            } else {
                reporte.append("\nNo hay clientes VIP registrados en este momento.\n");
            }

            reporte.append("\n===============================================================================\n");

            System.out.println("Reporte de clientes VIP generado: " + contador + " clientes");

        } catch (SQLException e) {
            System.err.println("Error generando reporte de clientes VIP: " + e.getMessage());
            e.printStackTrace();
            return "ERROR al generar reporte:\n" + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reporte.toString();
    }

    /**
     * Genera reporte del inventario de articulos
     * @return String formateado con el reporte
     */
    public String generarReporteInventarioArticulos() {
        StringBuilder reporte = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT TIPO_ARTICULO, " +
                "       ESTADO, " +
                "       COUNT(*) AS CANTIDAD, " +
                "       NVL(SUM(VALOR_TASADO), 0) AS VALOR_TOTAL, " +
                "       NVL(AVG(VALOR_TASADO), 0) AS VALOR_PROMEDIO " +
                "FROM ARTICULO " +
                "GROUP BY TIPO_ARTICULO, ESTADO " +
                "ORDER BY TIPO_ARTICULO, CANTIDAD DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // Encabezado
            reporte.append("===============================================================================\n");
            reporte.append("            REPORTE DE INVENTARIO DE ARTICULOS - CASA DE EMPENO               \n");
            reporte.append("===============================================================================\n");
            reporte.append(String.format("Fecha de generacion: %s\n\n", sdfCompleto.format(new Date())));

            // Columnas
            reporte.append(String.format("%-20s %-15s %-10s %-15s %-15s\n",
                "Tipo de Articulo", "Estado", "Cantidad", "Valor Total", "Valor Promedio"));
            reporte.append("-------------------------------------------------------------------------------\n");

            // Datos
            int totalArticulos = 0;
            double valorTotalGeneral = 0;
            String tipoActual = "";
            int subtotalTipo = 0;
            double subtotalValorTipo = 0;

            while (rs.next()) {
                String tipo = rs.getString("TIPO_ARTICULO");
                String estado = rs.getString("ESTADO");
                int cantidad = rs.getInt("CANTIDAD");
                double valorTotal = rs.getDouble("VALOR_TOTAL");
                double valorPromedio = rs.getDouble("VALOR_PROMEDIO");

                // Si cambia el tipo, mostrar subtotal del anterior
                if (!tipo.equals(tipoActual) && !tipoActual.isEmpty()) {
                    reporte.append(String.format("  Subtotal %-12s:          %10d                  $%,12.2f\n",
                        tipoActual, subtotalTipo, subtotalValorTipo));
                    reporte.append("  -----------------------------------------------------------------------------\n");
                    subtotalTipo = 0;
                    subtotalValorTipo = 0;
                }

                reporte.append(String.format("%-20s %-15s %10d   $%,12.2f   $%,12.2f\n",
                    tipo, estado, cantidad, valorTotal, valorPromedio));

                totalArticulos += cantidad;
                valorTotalGeneral += valorTotal;
                subtotalTipo += cantidad;
                subtotalValorTipo += valorTotal;
                tipoActual = tipo;
            }

            // Ultimo subtotal
            if (!tipoActual.isEmpty()) {
                reporte.append(String.format("  Subtotal %-12s:          %10d                  $%,12.2f\n",
                    tipoActual, subtotalTipo, subtotalValorTipo));
            }

            // Totales
            reporte.append("===============================================================================\n");
            reporte.append(String.format("TOTAL GENERAL:                       %10d                  $%,12.2f\n",
                totalArticulos, valorTotalGeneral));
            reporte.append("===============================================================================\n\n");

            // Resumen
            reporte.append("RESUMEN:\n");
            reporte.append(String.format("  - Total de articulos en inventario: %d\n", totalArticulos));
            reporte.append(String.format("  - Valor total del inventario: $%,.2f\n", valorTotalGeneral));

            if (totalArticulos > 0) {
                reporte.append(String.format("  - Valor promedio por articulo: $%,.2f\n",
                    valorTotalGeneral / totalArticulos));
            }

            reporte.append("\n===============================================================================\n");

            System.out.println("Reporte de inventario generado: " + totalArticulos + " articulos");

        } catch (SQLException e) {
            System.err.println("Error generando reporte de inventario: " + e.getMessage());
            e.printStackTrace();
            return "ERROR al generar reporte:\n" + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reporte.toString();
    }

    /**
     * Genera reporte de rendimiento de asesores.
     * Muestra estadísticas completas de cada asesor.
     *
     * @return String formateado con el reporte
     */
    public String generarReporteRendimientoAsesores() {
        StringBuilder reporte = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT p.NOMBRE_PERSONA AS ASESOR, " +
                "       COUNT(pr.ID_PRESTAMO) AS PRESTAMOS_GESTIONADOS, " +
                "       NVL(SUM(pr.MONTO), 0) AS MONTO_TOTAL_PRESTADO, " +
                "       NVL(SUM(CASE WHEN pr.ESTADO_PRESTAMO = 'CANCELADO' THEN pr.MONTO ELSE 0 END), 0) AS MONTO_RECUPERADO, " +
                "       NVL(SUM(CASE WHEN pr.ESTADO_PRESTAMO = 'CANCELADO' THEN 1 ELSE 0 END), 0) AS PRESTAMOS_CANCELADOS, " +
                "       NVL(SUM(CASE WHEN pr.ESTADO_PRESTAMO = 'VENCIDO' THEN 1 ELSE 0 END), 0) AS PRESTAMOS_VENCIDOS, " +
                "       ROUND(NVL(SUM(CASE WHEN pr.ESTADO_PRESTAMO = 'CANCELADO' THEN 1 ELSE 0 END) * 100.0 / " +
                "             NULLIF(COUNT(pr.ID_PRESTAMO), 0), 0), 2) AS TASA_RECUPERACION, " +
                "       COUNT(DISTINCT a.ID_ARTICULO) AS ARTICULOS_EVALUADOS, " +
                "       NVL(SUM(pr.INTERES_GENERADO), 0) AS INTERESES_GENERADOS " +
                "FROM ASESOR ases " +
                "JOIN PERSONA p ON ases.ID_PERSONA = p.ID_PERSONA " +
                "LEFT JOIN PRESTAMO pr ON ases.ID_PERSONA = pr.ID_ASESOR " +
                "LEFT JOIN ARTICULO a ON pr.ID_ARTICULO = a.ID_ARTICULO " +
                "GROUP BY p.NOMBRE_PERSONA " +
                "ORDER BY PRESTAMOS_GESTIONADOS DESC, MONTO_TOTAL_PRESTADO DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // Encabezado
            reporte.append("===============================================================================\n");
            reporte.append("          REPORTE DE RENDIMIENTO DE ASESORES - CASA DE EMPENO                 \n");
            reporte.append("===============================================================================\n");
            reporte.append(String.format("Fecha de generacion: %s\n\n", sdfCompleto.format(new Date())));

            // Columnas
            reporte.append(String.format("%-25s %10s %15s %15s %10s\n",
                "Asesor", "Prestamos", "Monto Prestado", "Recuperado", "Tasa %"));
            reporte.append("-------------------------------------------------------------------------------\n");

            // Datos
            int contador = 0;
            int totalPrestamos = 0;
            double totalMontoPrestado = 0;
            double totalMontoRecuperado = 0;
            int totalArticulosEvaluados = 0;
            double totalIntereses = 0;

            while (rs.next()) {
                contador++;
                String asesor = rs.getString("ASESOR");
                int prestamosGestionados = rs.getInt("PRESTAMOS_GESTIONADOS");
                double montoPrestado = rs.getDouble("MONTO_TOTAL_PRESTADO");
                double montoRecuperado = rs.getDouble("MONTO_RECUPERADO");
                int prestamosCancelados = rs.getInt("PRESTAMOS_CANCELADOS");
                int prestamosVencidos = rs.getInt("PRESTAMOS_VENCIDOS");
                double tasaRecuperacion = rs.getDouble("TASA_RECUPERACION");
                int articulosEvaluados = rs.getInt("ARTICULOS_EVALUADOS");
                double interesesGenerados = rs.getDouble("INTERESES_GENERADOS");

                if (asesor.length() > 23) asesor = asesor.substring(0, 23) + "..";

                reporte.append(String.format("%-25s %10d   $%,12.2f   $%,12.2f %9.2f%%\n",
                    asesor, prestamosGestionados, montoPrestado, montoRecuperado, tasaRecuperacion));

                // Detalle adicional
                reporte.append(String.format("  └─ Cancelados: %d | Vencidos: %d | Articulos: %d | Intereses: $%,.2f\n",
                    prestamosCancelados, prestamosVencidos, articulosEvaluados, interesesGenerados));

                totalPrestamos += prestamosGestionados;
                totalMontoPrestado += montoPrestado;
                totalMontoRecuperado += montoRecuperado;
                totalArticulosEvaluados += articulosEvaluados;
                totalIntereses += interesesGenerados;
            }

            // Totales
            reporte.append("===============================================================================\n");
            double tasaGeneralRecuperacion = totalPrestamos > 0 ?
                (totalMontoRecuperado * 100.0 / totalMontoPrestado) : 0;
            reporte.append(String.format("TOTALES:          %10d   $%,12.2f   $%,12.2f %9.2f%%\n",
                totalPrestamos, totalMontoPrestado, totalMontoRecuperado, tasaGeneralRecuperacion));
            reporte.append("===============================================================================\n\n");

            // Resumen y Ranking
            reporte.append("RESUMEN:\n");
            reporte.append(String.format("  - Total de asesores activos: %d\n", contador));
            reporte.append(String.format("  - Total de prestamos gestionados: %d\n", totalPrestamos));
            reporte.append(String.format("  - Monto total prestado: $%,.2f\n", totalMontoPrestado));
            reporte.append(String.format("  - Monto total recuperado: $%,.2f\n", totalMontoRecuperado));
            reporte.append(String.format("  - Tasa global de recuperacion: %.2f%%\n", tasaGeneralRecuperacion));
            reporte.append(String.format("  - Total articulos evaluados: %d\n", totalArticulosEvaluados));
            reporte.append(String.format("  - Total intereses generados: $%,.2f\n", totalIntereses));

            if (contador == 0) {
                reporte.append("\nNo hay asesores registrados en este momento.\n");
            }

            reporte.append("\n===============================================================================\n");

            System.out.println("Reporte de rendimiento de asesores generado: " + contador + " asesores");

        } catch (SQLException e) {
            System.err.println("Error generando reporte de asesores: " + e.getMessage());
            e.printStackTrace();
            return "ERROR al generar reporte:\n" + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reporte.toString();
    }

    /**
     * Genera reporte del estado financiero completo del negocio.
     *
     * @return String formateado con el reporte
     */
    public String generarReporteEstadoFinanciero() {
        StringBuilder reporte = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            // Encabezado
            reporte.append("===============================================================================\n");
            reporte.append("           REPORTE DE ESTADO FINANCIERO - CASA DE EMPENO                      \n");
            reporte.append("===============================================================================\n");
            reporte.append(String.format("Fecha de generacion: %s\n\n", sdfCompleto.format(new Date())));

            // 1. CAPITAL PRESTADO
            String sqlCapital =
                "SELECT NVL(SUM(MONTO), 0) AS TOTAL_CAPITAL, " +
                "       COUNT(*) AS NUM_PRESTAMOS " +
                "FROM PRESTAMO";
            pstmt = conn.prepareStatement(sqlCapital);
            rs = pstmt.executeQuery();

            double totalCapital = 0;
            int numPrestamos = 0;
            if (rs.next()) {
                totalCapital = rs.getDouble("TOTAL_CAPITAL");
                numPrestamos = rs.getInt("NUM_PRESTAMOS");
            }
            rs.close();
            pstmt.close();

            // 2. INTERESES GENERADOS
            String sqlIntereses =
                "SELECT NVL(SUM(INTERES_GENERADO), 0) AS TOTAL_INTERESES " +
                "FROM PRESTAMO";
            pstmt = conn.prepareStatement(sqlIntereses);
            rs = pstmt.executeQuery();

            double totalIntereses = 0;
            if (rs.next()) {
                totalIntereses = rs.getDouble("TOTAL_INTERESES");
            }
            rs.close();
            pstmt.close();

            // 3. MULTAS COBRADAS
            String sqlMultas =
                "SELECT 0 AS TOTAL_MULTAS " +
                "FROM DUAL";
            pstmt = conn.prepareStatement(sqlMultas);
            rs = pstmt.executeQuery();

            double totalMultas = 0;
            if (rs.next()) {
                totalMultas = rs.getDouble("TOTAL_MULTAS");
            }
            rs.close();
            pstmt.close();

            // 4. PRÉSTAMOS POR ESTADO
            String sqlEstados =
                "SELECT ESTADO_PRESTAMO, " +
                "       COUNT(*) AS CANTIDAD, " +
                "       NVL(SUM(MONTO), 0) AS MONTO_TOTAL, " +
                "       NVL(SUM(INTERES_GENERADO), 0) AS INTERESES " +
                "FROM PRESTAMO " +
                "GROUP BY ESTADO_PRESTAMO " +
                "ORDER BY CANTIDAD DESC";
            pstmt = conn.prepareStatement(sqlEstados);
            rs = pstmt.executeQuery();

            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n");
            reporte.append("  SECCIÓN 1: CAPITAL E INGRESOS\n");
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n\n");
            reporte.append(String.format("  Total de prestamos realizados:           %d\n", numPrestamos));
            reporte.append(String.format("  Capital total prestado:                   $%,.2f\n", totalCapital));
            reporte.append(String.format("  Intereses totales generados:              $%,.2f\n", totalIntereses));
            reporte.append(String.format("  Multas totales cobradas:                  $%,.2f\n", totalMultas));
            reporte.append("  ───────────────────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("  INGRESOS TOTALES:                         $%,.2f\n\n",
                totalIntereses + totalMultas));

            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n");
            reporte.append("  SECCIÓN 2: DISTRIBUCIÓN POR ESTADO\n");
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n\n");
            reporte.append(String.format("%-20s %12s %18s %18s\n",
                "Estado", "Cantidad", "Monto Capital", "Intereses"));
            reporte.append("-------------------------------------------------------------------------------\n");

            int prestamosActivos = 0;
            int prestamosVencidos = 0;
            double montoActivos = 0;
            double montoVencidos = 0;

            while (rs.next()) {
                String estado = rs.getString("ESTADO_PRESTAMO");
                int cantidad = rs.getInt("CANTIDAD");
                double monto = rs.getDouble("MONTO_TOTAL");
                double intereses = rs.getDouble("INTERESES");

                reporte.append(String.format("%-20s %12d     $%,14.2f     $%,14.2f\n",
                    estado, cantidad, monto, intereses));

                if ("ACTIVO".equals(estado) || "EN_MORA".equals(estado)) {
                    prestamosActivos += cantidad;
                    montoActivos += monto + intereses;
                } else if ("VENCIDO".equals(estado)) {
                    prestamosVencidos += cantidad;
                    montoVencidos += monto + intereses;
                }
            }
            rs.close();
            pstmt.close();

            reporte.append("\n");

            // 5. PROYECCIÓN DE INGRESOS
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n");
            reporte.append("  SECCIÓN 3: PROYECCIÓN Y RIESGOS\n");
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n\n");
            reporte.append(String.format("  Prestamos activos (ACTIVO + EN_MORA):     %d\n", prestamosActivos));
            reporte.append(String.format("  Monto proyectado a recuperar:             $%,.2f\n", montoActivos));
            reporte.append(String.format("  Prestamos vencidos (riesgo):              %d\n", prestamosVencidos));
            reporte.append(String.format("  Monto en riesgo:                          $%,.2f\n", montoVencidos));

            double porcentajeRiesgo = totalCapital > 0 ? (montoVencidos * 100.0 / totalCapital) : 0;
            reporte.append(String.format("  Porcentaje en riesgo:                     %.2f%%\n\n", porcentajeRiesgo));

            // 6. ARTÍCULOS EN PROPIEDAD DE LA CASA
            String sqlPropiedad =
                "SELECT COUNT(*) AS CANTIDAD, " +
                "       NVL(SUM(VALOR_TASADO), 0) AS VALOR_TOTAL " +
                "FROM ARTICULO " +
                "WHERE ESTADO = 'PROPIEDAD_CASA'";
            pstmt = conn.prepareStatement(sqlPropiedad);
            rs = pstmt.executeQuery();

            int articulosPropiedad = 0;
            double valorPropiedad = 0;
            if (rs.next()) {
                articulosPropiedad = rs.getInt("CANTIDAD");
                valorPropiedad = rs.getDouble("VALOR_TOTAL");
            }
            rs.close();
            pstmt.close();

            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n");
            reporte.append("  SECCIÓN 4: ACTIVOS EN INVENTARIO\n");
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n\n");
            reporte.append(String.format("  Articulos propiedad de la casa:           %d\n", articulosPropiedad));
            reporte.append(String.format("  Valor total del inventario:               $%,.2f\n\n", valorPropiedad));

            // RESUMEN FINAL
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n");
            reporte.append("  RESUMEN EJECUTIVO\n");
            reporte.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            double activosLiquidos = totalIntereses + totalMultas;
            double activosInventario = valorPropiedad;
            double totalActivos = activosLiquidos + activosInventario + montoActivos;

            reporte.append(String.format("  Ingresos generados (Intereses + Multas):  $%,.2f\n", activosLiquidos));
            reporte.append(String.format("  Activos en inventario:                    $%,.2f\n", activosInventario));
            reporte.append(String.format("  Capital a recuperar:                      $%,.2f\n", montoActivos));
            reporte.append("  ───────────────────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("  VALOR TOTAL DE ACTIVOS:                   $%,.2f\n", totalActivos));
            reporte.append(String.format("  Capital en riesgo:                        $%,.2f\n", montoVencidos));
            reporte.append("  ───────────────────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("  POSICIÓN NETA:                            $%,.2f\n",
                totalActivos - montoVencidos));

            reporte.append("\n===============================================================================\n");

            System.out.println("Reporte de estado financiero generado exitosamente");

        } catch (SQLException e) {
            System.err.println("Error generando reporte financiero: " + e.getMessage());
            e.printStackTrace();
            return "ERROR al generar reporte:\n" + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reporte.toString();
    }

    /**
     * Genera reporte de artículos listos para vender (propiedad de la casa).
     *
     * @return String formateado con el reporte
     */
    public String generarReporteArticulosPorVender() {
        StringBuilder reporte = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();

            String sql =
                "SELECT a.ID_ARTICULO, " +
                "       a.TIPO_ARTICULO, " +
                "       a.DESCRIPCION_ARTICULO, " +
                "       a.VALOR_TASADO, " +
                "       a.FECHA_AVALUO, " +
                "       a.FECHA_TRANSFERENCIA, " +
                "       TRUNC(SYSDATE - NVL(a.FECHA_TRANSFERENCIA, a.FECHA_AVALUO)) AS DIAS_INVENTARIO, " +
                "       ROUND(a.VALOR_TASADO * 1.3, 2) AS PRECIO_SUGERIDO_MIN, " +
                "       ROUND(a.VALOR_TASADO * 1.5, 2) AS PRECIO_SUGERIDO_MAX " +
                "FROM ARTICULO a " +
                "WHERE a.ESTADO = 'PROPIEDAD_CASA' " +
                "ORDER BY DIAS_INVENTARIO DESC, VALOR_TASADO DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // Encabezado
            reporte.append("===============================================================================\n");
            reporte.append("        REPORTE DE ARTICULOS PARA VENTA - CASA DE EMPENO                      \n");
            reporte.append("===============================================================================\n");
            reporte.append(String.format("Fecha de generacion: %s\n\n", sdfCompleto.format(new Date())));

            reporte.append("Criterio: Articulos en estado PROPIEDAD_CASA\n");
            reporte.append("Precio sugerido: 130% - 150% del valor tasado (margen de ganancia)\n\n");

            // Columnas
            reporte.append(String.format("%-6s %-18s %-8s %-13s %-13s %-13s\n",
                "ID", "Tipo", "Dias", "Valor Tasado", "Precio Min.", "Precio Max."));
            reporte.append("-------------------------------------------------------------------------------\n");

            // Datos
            int contador = 0;
            double valorTotalInventario = 0;
            double precioVentaMin = 0;
            double precioVentaMax = 0;
            int articulosRotacionLenta = 0; // Más de 90 días

            while (rs.next()) {
                contador++;
                int idArticulo = rs.getInt("ID_ARTICULO");
                String tipo = rs.getString("TIPO_ARTICULO");
                String descripcion = rs.getString("DESCRIPCION_ARTICULO");
                double valorTasado = rs.getDouble("VALOR_TASADO");
                int diasInventario = rs.getInt("DIAS_INVENTARIO");
                double precioSugeridoMin = rs.getDouble("PRECIO_SUGERIDO_MIN");
                double precioSugeridoMax = rs.getDouble("PRECIO_SUGERIDO_MAX");

                if (tipo.length() > 16) tipo = tipo.substring(0, 16) + "..";

                // Indicador de urgencia
                String urgencia = "";
                if (diasInventario > 180) {
                    urgencia = " ⚠️ URGENTE";
                    articulosRotacionLenta++;
                } else if (diasInventario > 90) {
                    urgencia = " ⚡";
                    articulosRotacionLenta++;
                }

                reporte.append(String.format("%-6d %-18s %6d   $%,10.2f   $%,10.2f   $%,10.2f%s\n",
                    idArticulo, tipo, diasInventario, valorTasado,
                    precioSugeridoMin, precioSugeridoMax, urgencia));

                // Descripción detallada
                if (descripcion.length() > 70) descripcion = descripcion.substring(0, 70) + "...";
                reporte.append(String.format("       └─ %s\n", descripcion));

                valorTotalInventario += valorTasado;
                precioVentaMin += precioSugeridoMin;
                precioVentaMax += precioSugeridoMax;
            }

            // Totales
            reporte.append("===============================================================================\n");
            reporte.append(String.format("TOTALES:                         $%,10.2f   $%,10.2f   $%,10.2f\n",
                valorTotalInventario, precioVentaMin, precioVentaMax));
            reporte.append("===============================================================================\n\n");

            // Resumen y Análisis
            reporte.append("RESUMEN Y ANÁLISIS:\n");
            reporte.append(String.format("  - Total de articulos para venta: %d\n", contador));
            reporte.append(String.format("  - Valor total del inventario: $%,.2f\n", valorTotalInventario));
            reporte.append(String.format("  - Precio de venta minimo (130%%): $%,.2f\n", precioVentaMin));
            reporte.append(String.format("  - Precio de venta maximo (150%%): $%,.2f\n", precioVentaMax));

            double gananciaPotencialMin = precioVentaMin - valorTotalInventario;
            double gananciaPotencialMax = precioVentaMax - valorTotalInventario;
            reporte.append(String.format("  - Ganancia potencial minima: $%,.2f (%.0f%%)\n",
                gananciaPotencialMin, (gananciaPotencialMin * 100.0 / valorTotalInventario)));
            reporte.append(String.format("  - Ganancia potencial maxima: $%,.2f (%.0f%%)\n",
                gananciaPotencialMax, (gananciaPotencialMax * 100.0 / valorTotalInventario)));
            reporte.append(String.format("  - Articulos con rotacion lenta (>90 dias): %d\n", articulosRotacionLenta));

            if (contador > 0) {
                reporte.append(String.format("  - Valor promedio por articulo: $%,.2f\n",
                    valorTotalInventario / contador));
            }

            reporte.append("\nSUGERENCIAS:\n");
            if (articulosRotacionLenta > 0) {
                reporte.append("  ⚠️  Considerar descuentos para articulos con rotacion lenta (>90 dias)\n");
                reporte.append("  📢 Promover articulos marcados con urgencia (>180 dias)\n");
            }
            reporte.append("  💰 Precio sugerido incluye margen de 30%-50% sobre valor tasado\n");
            reporte.append("  📊 Ajustar precios segun demanda del mercado y condicion del articulo\n");

            if (contador == 0) {
                reporte.append("\n✓ No hay articulos en inventario para vender en este momento.\n");
            }

            reporte.append("\n===============================================================================\n");

            System.out.println("Reporte de articulos para vender generado: " + contador + " articulos");

        } catch (SQLException e) {
            System.err.println("Error generando reporte de articulos para vender: " + e.getMessage());
            e.printStackTrace();
            return "ERROR al generar reporte:\n" + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reporte.toString();
    }
}
