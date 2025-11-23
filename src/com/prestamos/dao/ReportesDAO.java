package com.prestamos.dao;

import com.prestamos.config.ConexionOracle;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DAO para la generación de reportes del sistema de préstamos.
 * Proporciona métodos para generar diferentes tipos de reportes conectados a Oracle.
 *
 * @author Henersson Cobo, Jorge Mera, Fabian Ome
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
}
