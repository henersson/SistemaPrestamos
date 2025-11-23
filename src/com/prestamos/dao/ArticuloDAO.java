package com.prestamos.dao;

import java.sql.*;
import java.util.*;
import com.prestamos.modelo.Articulo;
import com.prestamos.config.ConexionOracle;

/**
 * Clase DAO (Data Access Object) para gestionar operaciones CRUD de Artículo.
 * Implementa todas las operaciones de acceso a datos para la entidad Artículo.
 *
 * @author Henersson Cobo
 * @version 1.0
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
            if (articulo.getEstado() != null && articulo.getEstado().equals("DEFECTUOSO")) {
                System.err.println("✗ ERROR: No se pueden registrar artículos defectuosos");
                return false;
            }

            conn = ConexionOracle.getConexion();
            System.out.println("→ Iniciando inserción de artículo: " + articulo.getNombre());

            conn.setAutoCommit(false);

            String sql = "INSERT INTO ARTICULO (ID_ARTICULO, TIPO_ARTICULO, DESCRIPCION_ARTICULO, " +
                        "VALOR_TASADO, ESTADO, PRECIO_MERCADO_BASE, PORCENTAJE_TASACION, FECHA_AVALUO) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, articulo.getIdArticulo());
            pstmt.setString(2, articulo.getTipoArticulo());
            pstmt.setString(3, articulo.getDescripcion());
            pstmt.setDouble(4, articulo.getValorTasado());
            pstmt.setString(5, articulo.getEstado());
            pstmt.setDouble(6, articulo.getPrecioMercadoBase());
            pstmt.setDouble(7, articulo.getPorcentajeTasacion());

            if (articulo.getFechaAvaluo() != null) {
                pstmt.setDate(8, new java.sql.Date(articulo.getFechaAvaluo().getTime()));
            } else {
                pstmt.setDate(8, new java.sql.Date(System.currentTimeMillis()));
            }

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

            String sql = "SELECT ID_ARTICULO, TIPO_ARTICULO, DESCRIPCION_ARTICULO, " +
                        "VALOR_TASADO, ESTADO, PRECIO_MERCADO_BASE, PORCENTAJE_TASACION, " +
                        "FECHA_AVALUO, ID_CLIENTE_ORIGINAL, FECHA_TRANSFERENCIA " +
                        "FROM ARTICULO WHERE ID_ARTICULO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idArticulo);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION_ARTICULO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setPrecioMercadoBase(rs.getDouble("PRECIO_MERCADO_BASE"));
                articulo.setPorcentajeTasacion(rs.getDouble("PORCENTAJE_TASACION"));

                java.sql.Date fechaAvaluo = rs.getDate("FECHA_AVALUO");
                if (fechaAvaluo != null) {
                    articulo.setFechaAvaluo(new java.util.Date(fechaAvaluo.getTime()));
                }

                System.out.println("✓ Artículo encontrado: " + articulo.getTipoArticulo());
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

            String sql = "SELECT ID_ARTICULO, TIPO_ARTICULO, DESCRIPCION_ARTICULO, " +
                        "VALOR_TASADO, ESTADO, PRECIO_MERCADO_BASE, PORCENTAJE_TASACION, " +
                        "FECHA_AVALUO FROM ARTICULO ORDER BY ID_ARTICULO DESC";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION_ARTICULO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setPrecioMercadoBase(rs.getDouble("PRECIO_MERCADO_BASE"));
                articulo.setPorcentajeTasacion(rs.getDouble("PORCENTAJE_TASACION"));

                java.sql.Date fechaAvaluo = rs.getDate("FECHA_AVALUO");
                if (fechaAvaluo != null) {
                    articulo.setFechaAvaluo(new java.util.Date(fechaAvaluo.getTime()));
                }

                articulos.add(articulo);
            }

            System.out.println("✓ Total de artículos encontrados: " + articulos.size());

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
     * Lista artículos disponibles (óptimos o buenos) para ser empeñados.
     *
     * @return lista de artículos disponibles
     */
    public List<Articulo> listarArticulosDisponibles() {
        List<Articulo> articulos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando artículos disponibles");

            String sql = "SELECT ID_ARTICULO, TIPO_ARTICULO, DESCRIPCION_ARTICULO, " +
                        "VALOR_TASADO, ESTADO, PRECIO_MERCADO_BASE, PORCENTAJE_TASACION, " +
                        "FECHA_AVALUO FROM ARTICULO " +
                        "WHERE ESTADO IN ('OPTIMO', 'BUENO') " +
                        "AND ID_ARTICULO NOT IN (" +
                        "  SELECT ID_ARTICULO FROM PRESTAMO " +
                        "  WHERE ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA')" +
                        ") ORDER BY VALOR_TASADO DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION_ARTICULO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setPrecioMercadoBase(rs.getDouble("PRECIO_MERCADO_BASE"));
                articulo.setPorcentajeTasacion(rs.getDouble("PORCENTAJE_TASACION"));

                java.sql.Date fechaAvaluo = rs.getDate("FECHA_AVALUO");
                if (fechaAvaluo != null) {
                    articulo.setFechaAvaluo(new java.util.Date(fechaAvaluo.getTime()));
                }

                articulos.add(articulo);
            }

            System.out.println("✓ Artículos disponibles: " + articulos.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al listar artículos disponibles");
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

            String sql = "UPDATE ARTICULO SET " +
                        "TIPO_ARTICULO = ?, DESCRIPCION_ARTICULO = ?, " +
                        "VALOR_TASADO = ?, ESTADO = ?, " +
                        "PRECIO_MERCADO_BASE = ?, PORCENTAJE_TASACION = ?, " +
                        "FECHA_AVALUO = ? " +
                        "WHERE ID_ARTICULO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, articulo.getTipoArticulo());
            pstmt.setString(2, articulo.getDescripcion());
            pstmt.setDouble(3, articulo.getValorTasado());
            pstmt.setString(4, articulo.getEstado());
            pstmt.setDouble(5, articulo.getPrecioMercadoBase());
            pstmt.setDouble(6, articulo.getPorcentajeTasacion());

            if (articulo.getFechaAvaluo() != null) {
                pstmt.setDate(7, new java.sql.Date(articulo.getFechaAvaluo().getTime()));
            } else {
                pstmt.setDate(7, new java.sql.Date(System.currentTimeMillis()));
            }

            pstmt.setInt(8, articulo.getIdArticulo());

            int filas = pstmt.executeUpdate();
            System.out.println("  ✓ UPDATE en ARTICULO: " + filas + " fila(s) actualizada(s)");

            if (filas > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Artículo actualizado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("  ✗ Rollback: No se actualizó el artículo");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar artículo");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());

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
     * Elimina un artículo de la base de datos con validaciones.
     * No permite eliminar si el artículo está en un préstamo activo.
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

            // VALIDACIÓN: Verificar que no esté en préstamo activo
            String sqlCheck = "SELECT COUNT(*) AS TOTAL FROM PRESTAMO " +
                            "WHERE ID_ARTICULO = ? AND ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA')";

            pstmtCheck = conn.prepareStatement(sqlCheck);
            pstmtCheck.setInt(1, idArticulo);
            rs = pstmtCheck.executeQuery();

            if (rs.next() && rs.getInt("TOTAL") > 0) {
                System.err.println("✗ ERROR: No se puede eliminar. El artículo está en préstamo activo");
                conn.rollback();
                return false;
            }
            rs.close();
            pstmtCheck.close();

            // Proceder con la eliminación
            String sqlDelete = "DELETE FROM ARTICULO WHERE ID_ARTICULO = ?";
            pstmtDelete = conn.prepareStatement(sqlDelete);
            pstmtDelete.setInt(1, idArticulo);

            int filas = pstmtDelete.executeUpdate();
            System.out.println("  ✓ DELETE en ARTICULO: " + filas + " fila(s) eliminada(s)");

            if (filas > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Artículo eliminado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("  ⚠ No se encontró artículo para eliminar");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al eliminar artículo");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());

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
     * Lista todos los artículos de un cliente específico.
     *
     * @param idCliente ID del cliente
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

            String sql = "SELECT a.ID_ARTICULO, a.TIPO_ARTICULO, a.DESCRIPCION_ARTICULO, " +
                        "a.VALOR_TASADO, a.ESTADO, a.PRECIO_MERCADO_BASE, a.PORCENTAJE_TASACION, " +
                        "a.FECHA_AVALUO " +
                        "FROM ARTICULO a " +
                        "JOIN PRESTAMO p ON a.ID_ARTICULO = p.ID_ARTICULO " +
                        "WHERE p.ID_CLIENTE = ? " +
                        "ORDER BY a.FECHA_AVALUO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION_ARTICULO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setPrecioMercadoBase(rs.getDouble("PRECIO_MERCADO_BASE"));
                articulo.setPorcentajeTasacion(rs.getDouble("PORCENTAJE_TASACION"));

                java.sql.Date fechaAvaluo = rs.getDate("FECHA_AVALUO");
                if (fechaAvaluo != null) {
                    articulo.setFechaAvaluo(new java.util.Date(fechaAvaluo.getTime()));
                }

                articulos.add(articulo);
            }

            System.out.println("✓ Artículos del cliente: " + articulos.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al listar artículos por cliente");
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
     * Lista artículos filtrados por estado.
     *
     * @param estado Estado del artículo (OPTIMO, BUENO, REGULAR, DEFECTUOSO, PROPIEDAD_CASA)
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

            String sql = "SELECT ID_ARTICULO, TIPO_ARTICULO, DESCRIPCION_ARTICULO, " +
                        "VALOR_TASADO, ESTADO, PRECIO_MERCADO_BASE, PORCENTAJE_TASACION, " +
                        "FECHA_AVALUO FROM ARTICULO " +
                        "WHERE ESTADO = ? ORDER BY FECHA_AVALUO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, estado);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
                articulo.setDescripcion(rs.getString("DESCRIPCION_ARTICULO"));
                articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
                articulo.setEstado(rs.getString("ESTADO"));
                articulo.setPrecioMercadoBase(rs.getDouble("PRECIO_MERCADO_BASE"));
                articulo.setPorcentajeTasacion(rs.getDouble("PORCENTAJE_TASACION"));

                java.sql.Date fechaAvaluo = rs.getDate("FECHA_AVALUO");
                if (fechaAvaluo != null) {
                    articulo.setFechaAvaluo(new java.util.Date(fechaAvaluo.getTime()));
                }

                articulos.add(articulo);
            }

            System.out.println("✓ Artículos encontrados: " + articulos.size());

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
}

