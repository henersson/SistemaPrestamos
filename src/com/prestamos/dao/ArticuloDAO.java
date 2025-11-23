package com.prestamos.dao;

import java.sql.*;
import java.util.*;
import com.prestamos.modelo.Articulo;
import com.prestamos.config.ConexionOracle;

/**
 * Clase DAO (Data Access Object) para gestionar operaciones CRUD de Artículo.
 * Implementa todas las operaciones de acceso a datos para la entidad Artículo.
 *
 * @author Sistema de Préstamos
 * @version 2.0
 * @since 2025-11-23
 */
public class ArticuloDAO {

    /**
     * Inserta un nuevo artículo en la base de datos.
     * Valida que el artículo no sea defectuoso antes de insertarlo.
     *
     * @param articulo objeto Artículo con los datos a insertar
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    public boolean insertarArticulo(Articulo articulo) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // VALIDACIÓN: No permitir insertar artículos defectuosos
            if (articulo.getEstado() != null && articulo.getEstado().equalsIgnoreCase("DEFECTUOSO")) {
                System.err.println("✗ ERROR: No se pueden registrar artículos defectuosos");
                return false;
            }

            conn = ConexionOracle.getConexion();
            System.out.println("→ Iniciando inserción de artículo ID: " + articulo.getIdArticulo());

            conn.setAutoCommit(false);

            // Esquema completo: ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION
            String sql = "INSERT INTO ARTICULO (ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, articulo.getIdArticulo());
            pstmt.setInt(2, articulo.getIdCliente());
            pstmt.setString(3, articulo.getNombre()); // Guardar el nombre real del artículo
            pstmt.setString(4, articulo.getTipoArticulo());
            pstmt.setString(5, articulo.getEstado());
            pstmt.setDouble(6, articulo.getValorTasado());
            pstmt.setString(7, articulo.getDescripcion());

            System.out.println("  → SQL: " + sql);
            System.out.println("  → Parámetros: ID=" + articulo.getIdArticulo() +
                             ", ID_CLIENTE=" + articulo.getIdCliente() +
                             ", TIPO=" + articulo.getTipoArticulo() +
                             ", ESTADO=" + articulo.getEstado() +
                             ", VALOR=" + articulo.getValorTasado());

            int filas = pstmt.executeUpdate();
            System.out.println("  ✓ INSERT en ARTICULO: " + filas + " fila(s) insertada(s)");

            if (filas > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Artículo insertado exitosamente con ID: " + articulo.getIdArticulo());
                return true;
            } else {
                conn.rollback();
                System.err.println("  ✗ Rollback: No se insertó el artículo");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al insertar artículo");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            e.printStackTrace();

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
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.setAutoCommit(true);
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene un artículo por su ID con información completa.
     *
     * @param idArticulo ID del artículo a buscar
     * @return objeto Articulo con los datos, null si no se encuentra
     */
    public Articulo obtenerArticuloPorId(int idArticulo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Articulo articulo = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Buscando artículo con ID: " + idArticulo);

            // Esquema real
            String sql = "SELECT ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION " +
                        "FROM ARTICULO WHERE ID_ARTICULO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idArticulo);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setIdCliente(rs.getInt("ID_CLIENTE"));
                articulo.setNombre(rs.getString("NOMBRE"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION"));

                System.out.println("✓ Artículo encontrado: " + articulo.getNombre());
            } else {
                System.out.println("  ⚠ No se encontró artículo con ID: " + idArticulo);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener artículo");
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return articulo;
    }

    /**
     * Lista todos los artículos de la base de datos.
     *
     * @return lista de objetos Articulo
     */
    public List<Articulo> listarTodosArticulos() {
        List<Articulo> articulos = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando todos los artículos");

            String sql = "SELECT ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION " +
                        "FROM ARTICULO ORDER BY ID_ARTICULO DESC";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setIdCliente(rs.getInt("ID_CLIENTE"));
                articulo.setNombre(rs.getString("NOMBRE"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION"));

                articulos.add(articulo);
            }

            System.out.println("✓ Se encontraron " + articulos.size() + " artículos");

        } catch (SQLException e) {
            System.err.println("✗ Error al listar artículos");
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return articulos;
    }

    /**
     * Lista solo los artículos disponibles para préstamos.
     *
     * @return lista de artículos disponibles
     */
    public List<Articulo> listarArticulosDisponibles() {
        List<Articulo> articulos = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando artículos disponibles");

            String sql = "SELECT ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION " +
                        "FROM ARTICULO WHERE ESTADO = 'DISPONIBLE' ORDER BY ID_ARTICULO DESC";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setIdCliente(rs.getInt("ID_CLIENTE"));
                articulo.setNombre(rs.getString("NOMBRE"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION"));

                articulos.add(articulo);
            }

            System.out.println("✓ Se encontraron " + articulos.size() + " artículos disponibles");

        } catch (SQLException e) {
            System.err.println("✗ Error al listar artículos disponibles");
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return articulos;
    }

    /**
     * Lista todos los artículos de un cliente específico.
     *
     * @param idCliente ID del cliente propietario
     * @return lista de artículos del cliente
     */
    public List<Articulo> listarArticulosPorCliente(int idCliente) {
        List<Articulo> articulos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando artículos del cliente ID: " + idCliente);

            String sql = "SELECT ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION " +
                        "FROM ARTICULO WHERE ID_CLIENTE = ? ORDER BY ID_ARTICULO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setIdCliente(rs.getInt("ID_CLIENTE"));
                articulo.setNombre(rs.getString("NOMBRE"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION"));

                articulos.add(articulo);
            }

            System.out.println("✓ Se encontraron " + articulos.size() + " artículos del cliente");

        } catch (SQLException e) {
            System.err.println("✗ Error al listar artículos del cliente");
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return articulos;
    }

    /**
     * Actualiza los datos de un artículo existente.
     *
     * @param articulo objeto Articulo con los datos actualizados
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean actualizarArticulo(Articulo articulo) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Actualizando artículo ID: " + articulo.getIdArticulo());

            conn.setAutoCommit(false);

            String sql = "UPDATE ARTICULO SET NOMBRE = ?, TIPO_ARTICULO = ?, ESTADO = ?, " +
                        "VALOR_TASADO = ?, DESCRIPCION = ? WHERE ID_ARTICULO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, articulo.getNombre());
            pstmt.setString(2, articulo.getTipoArticulo());
            pstmt.setString(3, articulo.getEstado());
            pstmt.setDouble(4, articulo.getValorTasado());
            pstmt.setString(5, articulo.getDescripcion());
            pstmt.setInt(6, articulo.getIdArticulo());

            int filas = pstmt.executeUpdate();
            System.out.println("  ✓ UPDATE: " + filas + " fila(s) actualizada(s)");

            if (filas > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Artículo actualizado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("  ✗ Rollback: No se actualizó ningún artículo");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar artículo");
            System.err.println("  Mensaje: " + e.getMessage());

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
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.setAutoCommit(true);
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Elimina un artículo de la base de datos.
     * Validación: no se puede eliminar si está asociado a un préstamo activo.
     *
     * @param idArticulo ID del artículo a eliminar
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    public boolean eliminarArticulo(int idArticulo) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtDelete = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Eliminando artículo ID: " + idArticulo);

            conn.setAutoCommit(false);

            // Verificar si el artículo está en un préstamo activo
            String sqlCheck = "SELECT COUNT(*) FROM PRESTAMO WHERE ID_ARTICULO = ? AND ESTADO_PRESTAMO = 'ACTIVO'";
            pstmtCheck = conn.prepareStatement(sqlCheck);
            pstmtCheck.setInt(1, idArticulo);
            rs = pstmtCheck.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.err.println("✗ ERROR: No se puede eliminar. El artículo está en un préstamo activo");
                conn.rollback();
                return false;
            }

            // Proceder con la eliminación
            String sqlDelete = "DELETE FROM ARTICULO WHERE ID_ARTICULO = ?";
            pstmtDelete = conn.prepareStatement(sqlDelete);
            pstmtDelete.setInt(1, idArticulo);

            int filas = pstmtDelete.executeUpdate();
            System.out.println("  ✓ DELETE: " + filas + " fila(s) eliminada(s)");

            if (filas > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Artículo eliminado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("  ✗ Rollback: No se encontró el artículo a eliminar");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al eliminar artículo");
            System.err.println("  Mensaje: " + e.getMessage());

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
            try {
                if (rs != null) rs.close();
                if (pstmtCheck != null) pstmtCheck.close();
                if (pstmtDelete != null) pstmtDelete.close();
                if (conn != null) conn.setAutoCommit(true);
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Lista artículos filtrados por estado.
     *
     * @param estado el estado de los artículos a buscar
     * @return lista de artículos con el estado especificado
     */
    public List<Articulo> listarArticulosPorEstado(String estado) {
        List<Articulo> articulos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando artículos con estado: " + estado);

            String sql = "SELECT ID_ARTICULO, ID_CLIENTE, NOMBRE, TIPO_ARTICULO, ESTADO, VALOR_TASADO, DESCRIPCION " +
                        "FROM ARTICULO WHERE ESTADO = ? ORDER BY ID_ARTICULO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, estado);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setIdCliente(rs.getInt("ID_CLIENTE"));
                articulo.setNombre(rs.getString("NOMBRE"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION"));

                articulos.add(articulo);
            }

            System.out.println("✓ Se encontraron " + articulos.size() + " artículos");

        } catch (SQLException e) {
            System.err.println("✗ Error al listar artículos por estado");
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return articulos;
    }

    /**
     * Obtiene el siguiente ID disponible para un nuevo artículo.
     *
     * @return el siguiente ID disponible
     */
    public int obtenerSiguienteId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int siguienteId = 1;

        try {
            conn = ConexionOracle.getConexion();

            String sql = "SELECT NVL(MAX(ID_ARTICULO), 0) + 1 AS SIGUIENTE_ID FROM ARTICULO";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                siguienteId = rs.getInt("SIGUIENTE_ID");
            }

            System.out.println("→ Siguiente ID disponible: " + siguienteId);

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener siguiente ID");
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }

        return siguienteId;
    }
}
