package com.prestamos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase Singleton para gestionar la conexión a la base de datos Oracle 21c XE.
 * Proporciona métodos para establecer, verificar y cerrar la conexión a la base de datos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class ConexionOracle {

    // Constantes de configuración de la base de datos
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private static final String USUARIO = "USR_PRESTAMOS";
    private static final String PASSWORD = "Prestamos2025";
    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    // Instancia única de conexión (Singleton)
    private static Connection conexion = null;

    /**
     * Constructor privado para evitar instanciación externa.
     * Implementa el patrón Singleton.
     */
    private ConexionOracle() {
        // Constructor privado
    }

    /**
     * Obtiene una conexión activa a la base de datos Oracle.
     * Si no existe una conexión, crea una nueva.
     * Si existe una conexión cerrada, crea una nueva.
     *
     * @return Connection objeto de conexión activo a la base de datos
     * @throws SQLException si ocurre un error al conectar con la base de datos
     */
    public static Connection getConexion() throws SQLException {
        try {
            // Verificar si la conexión existe y está activa
            if (conexion == null || conexion.isClosed()) {
                // Cargar el driver de Oracle
                Class.forName(DRIVER);
                System.out.println("✓ Driver Oracle cargado correctamente");

                // Establecer la conexión
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("✓ Conexión establecida exitosamente con Oracle 21c XE");
                System.out.println("  URL: " + URL);
                System.out.println("  Usuario: " + USUARIO);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Error: No se pudo cargar el driver de Oracle");
            System.err.println("  Mensaje: " + e.getMessage());
            throw new SQLException("Driver de Oracle no encontrado", e);
        } catch (SQLException e) {
            System.err.println("✗ Error al establecer conexión con la base de datos");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código de error: " + e.getErrorCode());
            throw e;
        }

        return conexion;
    }

    /**
     * Cierra la conexión activa con la base de datos.
     * Si no existe conexión o ya está cerrada, no realiza ninguna acción.
     */
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada correctamente");
                conexion = null;
            } else {
                System.out.println("⚠ No hay conexión activa para cerrar");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al cerrar la conexión");
            System.err.println("  Mensaje: " + e.getMessage());
        }
    }

    /**
     * Verifica si existe una conexión activa con la base de datos.
     *
     * @return true si la conexión está activa, false en caso contrario
     */
    public static boolean estaConectada() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                // Verificar que la conexión sea válida con un timeout de 2 segundos
                return conexion.isValid(2);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al verificar estado de conexión");
            System.err.println("  Mensaje: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene información detallada sobre la conexión y la base de datos.
     * Incluye: URL, usuario, estado de conexión, producto, versión y driver.
     *
     * @return String con la información de la base de datos formateada
     */
    public static String getInfoBD() {
        StringBuilder info = new StringBuilder();
        info.append("\n========================================\n");
        info.append("   INFORMACIÓN DE LA BASE DE DATOS\n");
        info.append("========================================\n");

        try {
            info.append("URL: ").append(URL).append("\n");
            info.append("Usuario: ").append(USUARIO).append("\n");
            info.append("Estado: ");

            if (estaConectada()) {
                info.append("CONECTADA ✓\n");

                // Obtener metadatos de la base de datos
                var metaData = conexion.getMetaData();
                info.append("Producto: ").append(metaData.getDatabaseProductName()).append("\n");
                info.append("Versión: ").append(metaData.getDatabaseProductVersion()).append("\n");
                info.append("Driver: ").append(metaData.getDriverName()).append("\n");
                info.append("Versión Driver: ").append(metaData.getDriverVersion()).append("\n");
            } else {
                info.append("DESCONECTADA ✗\n");
            }
        } catch (SQLException e) {
            info.append("\n✗ Error al obtener información de la BD\n");
            info.append("  Mensaje: ").append(e.getMessage()).append("\n");
        }

        info.append("========================================\n");
        return info.toString();
    }
}

