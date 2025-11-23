package com.prestamos.dao;

import java.sql.*;
import java.util.*;
import com.prestamos.modelo.Cliente;
import com.prestamos.config.ConexionOracle;

/**
 * Clase DAO (Data Access Object) para gestionar operaciones CRUD de Cliente.
 * Implementa todas las operaciones de acceso a datos para la entidad Cliente.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class ClienteDAO {

    /**
     * Inserta un nuevo cliente en la base de datos.
     * Realiza dos inserciones: una en PERSONA y otra en CLIENTE usando transacciones.
     *
     * @param cliente objeto Cliente con los datos a insertar
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    public boolean insertarCliente(Cliente cliente) {
        Connection conn = null;
        PreparedStatement pstmtPersona = null;
        PreparedStatement pstmtCliente = null;

        try {
            // Obtener conexión
            conn = ConexionOracle.getConexion();
            System.out.println("→ Iniciando inserción de cliente: " + cliente.getNombrePersona());

            // Desactivar auto-commit para manejar transacciones
            conn.setAutoCommit(false);
            System.out.println("  ✓ Transacción iniciada (auto-commit desactivado)");

            // Preparar INSERT para PERSONA
            String sqlPersona = "INSERT INTO PERSONA (ID_PERSONA, NOMBRE_PERSONA, TIPOID, TIPO_PERSONA) " +
                               "VALUES (?, ?, ?, 'CLIENTE')";
            pstmtPersona = conn.prepareStatement(sqlPersona);
            pstmtPersona.setInt(1, cliente.getIdPersona());
            pstmtPersona.setString(2, cliente.getNombrePersona());
            pstmtPersona.setString(3, cliente.getTipoId());

            int filasPersona = pstmtPersona.executeUpdate();
            System.out.println("  ✓ INSERT en PERSONA: " + filasPersona + " fila(s) insertada(s)");

            // Preparar INSERT para CLIENTE
            String sqlCliente = "INSERT INTO CLIENTE (ID_PERSONA, ID_CLIENTE, FECHA_REGISTRO, CALIFICACION, ACTIVO) " +
                               "VALUES (?, ?, SYSDATE, ?, ?)";
            pstmtCliente = conn.prepareStatement(sqlCliente);
            pstmtCliente.setInt(1, cliente.getIdPersona());
            pstmtCliente.setInt(2, cliente.getIdCliente());
            pstmtCliente.setDouble(3, cliente.getCalificacion());
            pstmtCliente.setString(4, String.valueOf(cliente.getActivo()));

            int filasCliente = pstmtCliente.executeUpdate();
            System.out.println("  ✓ INSERT en CLIENTE: " + filasCliente + " fila(s) insertada(s)");

            // Si ambos fueron exitosos, confirmar transacción
            if (filasPersona > 0 && filasCliente > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Cliente insertado exitosamente con ID: " + cliente.getIdPersona());
                return true;
            } else {
                conn.rollback();
                System.err.println("  ✗ Rollback: No se insertaron todas las filas");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al insertar cliente");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());

            // Hacer rollback en caso de error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("  ✓ Rollback ejecutado");
                }
            } catch (SQLException ex) {
                System.err.println("  ✗ Error en rollback: " + ex.getMessage());
            }
            return false;

        } finally {
            // Cerrar recursos
            try {
                if (pstmtPersona != null) pstmtPersona.close();
                if (pstmtCliente != null) pstmtCliente.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Restaurar auto-commit
                }
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene un cliente por su ID de persona.
     * Realiza un JOIN entre PERSONA y CLIENTE.
     *
     * @param idPersona identificador único de la persona
     * @return objeto Cliente con los datos encontrados, null si no existe
     */
    public Cliente obtenerClientePorId(int idPersona) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Cliente cliente = null;

        try {
            // Obtener conexión
            conn = ConexionOracle.getConexion();
            System.out.println("→ Buscando cliente con ID: " + idPersona);

            // Preparar consulta con JOIN
            String sql = "SELECT p.ID_PERSONA, p.NOMBRE_PERSONA, p.TIPOID, p.TIPO_PERSONA, " +
                        "c.ID_CLIENTE, c.FECHA_REGISTRO, c.CALIFICACION, c.ACTIVO " +
                        "FROM PERSONA p " +
                        "JOIN CLIENTE c ON p.ID_PERSONA = c.ID_PERSONA " +
                        "WHERE p.ID_PERSONA = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPersona);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Crear objeto Cliente con los datos del ResultSet
                cliente = new Cliente(
                    rs.getInt("ID_PERSONA"),
                    rs.getString("NOMBRE_PERSONA"),
                    rs.getString("TIPOID"),
                    rs.getString("TIPO_PERSONA"),
                    rs.getInt("ID_CLIENTE"),
                    rs.getDate("FECHA_REGISTRO"),
                    rs.getDouble("CALIFICACION"),
                    rs.getString("ACTIVO").charAt(0)
                );
                System.out.println("  ✓ Cliente encontrado: " + cliente.getNombrePersona());
            } else {
                System.out.println("  ⚠ No se encontró cliente con ID: " + idPersona);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener cliente por ID");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());

        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return cliente;
    }

    /**
     * Lista todos los clientes registrados en el sistema.
     * Realiza un JOIN entre PERSONA y CLIENTE.
     *
     * @return List de objetos Cliente (vacía si no hay datos)
     */
    public List<Cliente> listarTodosClientes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Cliente> listaClientes = new ArrayList<>();

        try {
            // Obtener conexión
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando todos los clientes");

            // Preparar consulta con JOIN (sin WHERE)
            String sql = "SELECT p.ID_PERSONA, p.NOMBRE_PERSONA, p.TIPOID, p.TIPO_PERSONA, " +
                        "c.ID_CLIENTE, c.FECHA_REGISTRO, c.CALIFICACION, c.ACTIVO " +
                        "FROM PERSONA p " +
                        "JOIN CLIENTE c ON p.ID_PERSONA = c.ID_PERSONA " +
                        "ORDER BY p.ID_PERSONA";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // Recorrer ResultSet y agregar cada cliente a la lista
            int contador = 0;
            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getInt("ID_PERSONA"),
                    rs.getString("NOMBRE_PERSONA"),
                    rs.getString("TIPOID"),
                    rs.getString("TIPO_PERSONA"),
                    rs.getInt("ID_CLIENTE"),
                    rs.getDate("FECHA_REGISTRO"),
                    rs.getDouble("CALIFICACION"),
                    rs.getString("ACTIVO").charAt(0)
                );
                listaClientes.add(cliente);
                contador++;
            }

            System.out.println("  ✓ Se encontraron " + contador + " cliente(s)");

        } catch (SQLException e) {
            System.err.println("✗ Error al listar clientes");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());

        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return listaClientes;
    }

    /**
     * Actualiza los datos de un cliente existente.
     * Actualiza tanto PERSONA como CLIENTE usando transacciones.
     *
     * @param cliente objeto Cliente con los datos actualizados
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean actualizarCliente(Cliente cliente) {
        Connection conn = null;
        PreparedStatement pstmtPersona = null;
        PreparedStatement pstmtCliente = null;

        try {
            // Obtener conexión
            conn = ConexionOracle.getConexion();
            System.out.println("→ Actualizando cliente con ID: " + cliente.getIdPersona());

            // Desactivar auto-commit para manejar transacciones
            conn.setAutoCommit(false);
            System.out.println("  ✓ Transacción iniciada (auto-commit desactivado)");

            // Preparar UPDATE para PERSONA
            String sqlPersona = "UPDATE PERSONA SET NOMBRE_PERSONA = ?, TIPOID = ? " +
                               "WHERE ID_PERSONA = ?";
            pstmtPersona = conn.prepareStatement(sqlPersona);
            pstmtPersona.setString(1, cliente.getNombrePersona());
            pstmtPersona.setString(2, cliente.getTipoId());
            pstmtPersona.setInt(3, cliente.getIdPersona());

            int filasPersona = pstmtPersona.executeUpdate();
            System.out.println("  ✓ UPDATE en PERSONA: " + filasPersona + " fila(s) actualizada(s)");

            // Preparar UPDATE para CLIENTE
            String sqlCliente = "UPDATE CLIENTE SET CALIFICACION = ?, ACTIVO = ? " +
                               "WHERE ID_PERSONA = ?";
            pstmtCliente = conn.prepareStatement(sqlCliente);
            pstmtCliente.setDouble(1, cliente.getCalificacion());
            pstmtCliente.setString(2, String.valueOf(cliente.getActivo()));
            pstmtCliente.setInt(3, cliente.getIdPersona());

            int filasCliente = pstmtCliente.executeUpdate();
            System.out.println("  ✓ UPDATE en CLIENTE: " + filasCliente + " fila(s) actualizada(s)");

            // Si ambos fueron exitosos, confirmar transacción
            if (filasPersona > 0 && filasCliente > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Cliente actualizado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("  ✗ Rollback: No se actualizaron todas las filas");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar cliente");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());

            // Hacer rollback en caso de error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("  ✓ Rollback ejecutado");
                }
            } catch (SQLException ex) {
                System.err.println("  ✗ Error en rollback: " + ex.getMessage());
            }
            return false;

        } finally {
            // Cerrar recursos
            try {
                if (pstmtPersona != null) pstmtPersona.close();
                if (pstmtCliente != null) pstmtCliente.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Restaurar auto-commit
                }
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Elimina un cliente de la base de datos.
     * Solo elimina de PERSONA, la cascada elimina automáticamente de CLIENTE.
     *
     * @param idPersona identificador único de la persona a eliminar
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    public boolean eliminarCliente(int idPersona) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Obtener conexión
            conn = ConexionOracle.getConexion();
            System.out.println("→ Eliminando cliente con ID: " + idPersona);

            // Preparar DELETE
            String sql = "DELETE FROM PERSONA WHERE ID_PERSONA = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPersona);

            int filasEliminadas = pstmt.executeUpdate();

            if (filasEliminadas > 0) {
                System.out.println("  ✓ DELETE en PERSONA: " + filasEliminadas + " fila(s) eliminada(s)");
                System.out.println("  ✓ Cascada eliminó registros relacionados en CLIENTE");
                System.out.println("✓ Cliente eliminado exitosamente");
                return true;
            } else {
                System.out.println("  ⚠ No se encontró cliente con ID: " + idPersona);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al eliminar cliente");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());
            return false;

        } finally {
            // Cerrar recursos
            try {
                if (pstmt != null) pstmt.close();
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}

