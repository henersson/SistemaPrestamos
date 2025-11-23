package com.prestamos.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.prestamos.config.ConexionOracle;

/**
 * Clase DAO para obtener información del diccionario de datos de Oracle.
 * Proporciona métodos para consultar metadatos de la base de datos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class DiccionarioDatosDAO {

    /**
     * Obtiene la lista de todas las tablas del usuario.
     *
     * @return List con los nombres de las tablas
     */
    public List<String> obtenerTablas() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> tablas = new ArrayList<>();

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Obteniendo lista de tablas...");

            String sql = "SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME"));
            }

            System.out.println("  ✓ Tablas encontradas: " + tablas.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener tablas: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return tablas;
    }

    /**
     * Obtiene los detalles de una tabla específica.
     * Incluye columnas, tipos de datos, restricciones y número de registros.
     *
     * @param nombreTabla nombre de la tabla a consultar
     * @return String con los detalles formateados de la tabla
     */
    public String obtenerDetallesTabla(String nombreTabla) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder detalles = new StringBuilder();

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Obteniendo detalles de tabla: " + nombreTabla);

            // Encabezado
            detalles.append("╔════════════════════════════════════════════════════════════════════════════════╗\n");
            detalles.append("║                         DETALLES DE LA TABLA                                 ║\n");
            detalles.append("╚════════════════════════════════════════════════════════════════════════════════╝\n\n");
            detalles.append("Tabla: ").append(nombreTabla).append("\n\n");

            // Obtener columnas
            String sqlColumnas = "SELECT COLUMN_NAME, DATA_TYPE, " +
                                "CASE WHEN NULLABLE='Y' THEN 'NULL' ELSE 'NOT NULL' END AS NULLABLE, " +
                                "DATA_LENGTH " +
                                "FROM USER_TAB_COLUMNS " +
                                "WHERE TABLE_NAME = ? " +
                                "ORDER BY COLUMN_ID";

            pstmt = conn.prepareStatement(sqlColumnas);
            pstmt.setString(1, nombreTabla);
            rs = pstmt.executeQuery();

            // Tabla de columnas
            detalles.append("ESTRUCTURA DE COLUMNAS:\n");
            detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
            detalles.append(String.format("%-30s %-20s %-10s %-10s\n",
                "COLUMNA", "TIPO", "LONGITUD", "NULLABLE"));
            detalles.append("════════════════════════════════════════════════════════════════════════════════\n");

            int numColumnas = 0;
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                int dataLength = rs.getInt("DATA_LENGTH");
                String nullable = rs.getString("NULLABLE");

                detalles.append(String.format("%-30s %-20s %-10d %-10s\n",
                    columnName, dataType, dataLength, nullable));
                numColumnas++;
            }

            detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
            detalles.append("Total de columnas: ").append(numColumnas).append("\n\n");

            rs.close();
            pstmt.close();

            // Obtener número de registros
            String sqlCount = "SELECT COUNT(*) AS TOTAL FROM " + nombreTabla;
            pstmt = conn.prepareStatement(sqlCount);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("TOTAL");
                detalles.append("ESTADÍSTICAS:\n");
                detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
                detalles.append("Número de registros: ").append(total).append("\n");
                detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
            }

            System.out.println("  ✓ Detalles de tabla obtenidos");

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener detalles de tabla: " + e.getMessage());
            detalles.append("ERROR: No se pudieron obtener los detalles de la tabla\n");
            detalles.append("Mensaje: ").append(e.getMessage()).append("\n");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return detalles.toString();
    }

    /**
     * Obtiene la lista de paquetes PL/SQL del usuario.
     *
     * @return List con los nombres de los paquetes
     */
    public List<String> obtenerPaquetes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> paquetes = new ArrayList<>();

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Obteniendo lista de paquetes...");

            String sql = "SELECT DISTINCT OBJECT_NAME FROM USER_OBJECTS " +
                        "WHERE OBJECT_TYPE = 'PACKAGE' " +
                        "ORDER BY OBJECT_NAME";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                paquetes.add(rs.getString("OBJECT_NAME"));
            }

            System.out.println("  ✓ Paquetes encontrados: " + paquetes.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener paquetes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return paquetes;
    }

    /**
     * Obtiene los detalles de un paquete PL/SQL específico.
     *
     * @param nombrePaquete nombre del paquete a consultar
     * @return String con los detalles formateados del paquete
     */
    public String obtenerDetallesPaquete(String nombrePaquete) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder detalles = new StringBuilder();

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Obteniendo detalles de paquete: " + nombrePaquete);

            // Encabezado
            detalles.append("╔════════════════════════════════════════════════════════════════════════════════╗\n");
            detalles.append("║                       DETALLES DEL PAQUETE PL/SQL                           ║\n");
            detalles.append("╚════════════════════════════════════════════════════════════════════════════════╝\n\n");
            detalles.append("Paquete: ").append(nombrePaquete).append("\n\n");

            // Obtener estado del paquete
            String sqlEstado = "SELECT STATUS FROM USER_OBJECTS " +
                              "WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'PACKAGE'";

            pstmt = conn.prepareStatement(sqlEstado);
            pstmt.setString(1, nombrePaquete);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("STATUS");
                detalles.append("Estado: ").append(status).append("\n\n");
            }

            rs.close();
            pstmt.close();

            // Obtener procedimientos del paquete
            String sqlProcs = "SELECT OBJECT_NAME, PROCEDURE_NAME, OBJECT_TYPE " +
                             "FROM USER_PROCEDURES " +
                             "WHERE OBJECT_NAME = ?";

            pstmt = conn.prepareStatement(sqlProcs);
            pstmt.setString(1, nombrePaquete);
            rs = pstmt.executeQuery();

            detalles.append("PROCEDIMIENTOS Y FUNCIONES:\n");
            detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
            detalles.append(String.format("%-50s %-20s\n", "NOMBRE", "TIPO"));
            detalles.append("════════════════════════════════════════════════════════════════════════════════\n");

            int count = 0;
            while (rs.next()) {
                String procName = rs.getString("PROCEDURE_NAME");
                String objType = rs.getString("OBJECT_TYPE");

                if (procName != null) {
                    detalles.append(String.format("%-50s %-20s\n", procName, objType));
                    count++;
                }
            }

            detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
            detalles.append("Total de procedimientos/funciones: ").append(count).append("\n");

            System.out.println("  ✓ Detalles de paquete obtenidos");

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener detalles de paquete: " + e.getMessage());
            detalles.append("ERROR: No se pudieron obtener los detalles del paquete\n");
            detalles.append("Mensaje: ").append(e.getMessage()).append("\n");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return detalles.toString();
    }

    /**
     * Obtiene la lista de triggers del usuario.
     *
     * @return List con los nombres de los triggers
     */
    public List<String> obtenerTriggers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> triggers = new ArrayList<>();

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Obteniendo lista de triggers...");

            String sql = "SELECT TRIGGER_NAME FROM USER_TRIGGERS ORDER BY TRIGGER_NAME";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                triggers.add(rs.getString("TRIGGER_NAME"));
            }

            System.out.println("  ✓ Triggers encontrados: " + triggers.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener triggers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return triggers;
    }

    /**
     * Obtiene los detalles de un trigger específico.
     *
     * @param nombreTrigger nombre del trigger a consultar
     * @return String con los detalles formateados del trigger
     */
    public String obtenerDetallesTrigger(String nombreTrigger) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder detalles = new StringBuilder();

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Obteniendo detalles de trigger: " + nombreTrigger);

            // Encabezado
            detalles.append("╔════════════════════════════════════════════════════════════════════════════════╗\n");
            detalles.append("║                          DETALLES DEL TRIGGER                                ║\n");
            detalles.append("╚════════════════════════════════════════════════════════════════════════════════╝\n\n");
            detalles.append("Trigger: ").append(nombreTrigger).append("\n\n");

            // Obtener detalles del trigger
            String sql = "SELECT TRIGGER_TYPE, TRIGGERING_EVENT, TABLE_NAME, STATUS " +
                        "FROM USER_TRIGGERS " +
                        "WHERE TRIGGER_NAME = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombreTrigger);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String triggerType = rs.getString("TRIGGER_TYPE");
                String triggeringEvent = rs.getString("TRIGGERING_EVENT");
                String tableName = rs.getString("TABLE_NAME");
                String status = rs.getString("STATUS");

                detalles.append("INFORMACIÓN DEL TRIGGER:\n");
                detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
                detalles.append(String.format("%-25s : %s\n", "Tipo", triggerType));
                detalles.append(String.format("%-25s : %s\n", "Evento disparador", triggeringEvent));
                detalles.append(String.format("%-25s : %s\n", "Tabla asociada", tableName));
                detalles.append(String.format("%-25s : %s\n", "Estado", status));
                detalles.append("════════════════════════════════════════════════════════════════════════════════\n");
            } else {
                detalles.append("No se encontró información del trigger.\n");
            }

            System.out.println("  ✓ Detalles de trigger obtenidos");

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener detalles de trigger: " + e.getMessage());
            detalles.append("ERROR: No se pudieron obtener los detalles del trigger\n");
            detalles.append("Mensaje: ").append(e.getMessage()).append("\n");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return detalles.toString();
    }
}

