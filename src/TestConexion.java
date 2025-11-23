import com.prestamos.config.ConexionOracle;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Clase de prueba para verificar la conexión a la base de datos Oracle
 * y mostrar estadísticas de las tablas principales del sistema.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class TestConexion {

    /**
     * Método principal para probar la conexión a la base de datos.
     * Ejecuta consultas COUNT(*) en las tablas del sistema y muestra los resultados.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        try {
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║   TEST DE CONEXIÓN - SISTEMA DE PRÉSTAMOS   ║");
            System.out.println("╚════════════════════════════════════════════════╝\n");

            // Establecer conexión
            System.out.println("→ Intentando conectar a la base de datos...\n");
            conn = ConexionOracle.getConexion();

            // Mostrar información de la base de datos
            System.out.println(ConexionOracle.getInfoBD());

            // Verificar estado de conexión
            if (ConexionOracle.estaConectada()) {
                System.out.println("\n╔════════════════════════════════════════════════╗");
                System.out.println("║     CONTEO DE REGISTROS EN LAS TABLAS        ║");
                System.out.println("╚════════════════════════════════════════════════╝\n");

                // Array con los nombres de las tablas a consultar
                String[] tablas = {
                    "PERSONA",
                    "CLIENTE",
                    "ASESOR",
                    "ADMINISTRADOR",
                    "PRESTAMO",
                    "ARTICULO",
                    "TASA"
                };

                // Crear statement para ejecutar consultas
                stmt = conn.createStatement();

                // Consultar cada tabla
                for (String tabla : tablas) {
                    try {
                        String sql = "SELECT COUNT(*) AS TOTAL FROM " + tabla;
                        ResultSet rs = stmt.executeQuery(sql);

                        if (rs.next()) {
                            int total = rs.getInt("TOTAL");
                            System.out.printf("  %-20s : %5d registros\n", tabla, total);
                        }

                        rs.close();

                    } catch (SQLException e) {
                        System.out.printf("  %-20s : ERROR - %s\n", tabla, e.getMessage());
                    }
                }

                System.out.println("\n════════════════════════════════════════════════\n");
                System.out.println("✓ Prueba de conexión completada exitosamente");

            } else {
                System.err.println("\n✗ La conexión no está activa");
            }

        } catch (SQLException e) {
            System.err.println("\n╔════════════════════════════════════════════════╗");
            System.err.println("║              ERROR DE CONEXIÓN                 ║");
            System.err.println("╚════════════════════════════════════════════════╝\n");
            System.err.println("✗ No se pudo conectar a la base de datos");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());
            System.err.println("\nStack trace completo:");
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("\n✗ Error inesperado durante la prueba");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("\nStack trace completo:");
            e.printStackTrace();

        } finally {
            // Cerrar statement si está abierto
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                    System.out.println("→ Statement cerrado correctamente");
                }
            } catch (SQLException e) {
                System.err.println("✗ Error al cerrar statement: " + e.getMessage());
            }

            // Cerrar conexión
            System.out.println("→ Cerrando conexión...\n");
            ConexionOracle.cerrarConexion();

            System.out.println("════════════════════════════════════════════════");
            System.out.println("           Fin del test de conexión");
            System.out.println("════════════════════════════════════════════════\n");
        }
    }
}

