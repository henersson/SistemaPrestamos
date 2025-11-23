package com.prestamos.dao;

import java.sql.*;
import java.util.*;
import com.prestamos.modelo.Prestamo;
import com.prestamos.config.ConexionOracle;

/**
 * Clase DAO (Data Access Object) para gestionar operaciones de Préstamo.
 * Implementa todas las operaciones CRUD y de negocio para préstamos.
 *
 * @author Henersson Cobo
 * @version 1.0
 * @since 2025-11-23
 */
public class PrestamoDAO {

    /**
     * Crea un nuevo préstamo en la base de datos.
     * Realiza validaciones de negocio antes de crear el préstamo.
     *
     * @param prestamo objeto Prestamo con los datos a insertar
     * @return true si la creación fue exitosa, false en caso contrario
     */
    public boolean crearPrestamo(Prestamo prestamo) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Iniciando creación de préstamo");
            conn.setAutoCommit(false);

            // VALIDACIÓN 1: Verificar que el artículo no sea defectuoso
            String sqlCheckArticulo = "SELECT ESTADO, VALOR_TASADO FROM ARTICULO WHERE ID_ARTICULO = ?";
            pstmtCheck = conn.prepareStatement(sqlCheckArticulo);
            pstmtCheck.setInt(1, prestamo.getIdArticulo());
            rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                String estadoArticulo = rs.getString("ESTADO");
                double valorTasado = rs.getDouble("VALOR_TASADO");

                if ("DEFECTUOSO".equals(estadoArticulo)) {
                    System.err.println("✗ ERROR: El artículo está en estado DEFECTUOSO");
                    conn.rollback();
                    return false;
                }

                // VALIDACIÓN 2: Monto no debe exceder el valor del artículo
                if (prestamo.getMonto() > valorTasado) {
                    System.err.println("✗ ERROR: El monto ($" + prestamo.getMonto() +
                                     ") excede el valor tasado del artículo ($" + valorTasado + ")");
                    conn.rollback();
                    return false;
                }
            } else {
                System.err.println("✗ ERROR: El artículo no existe");
                conn.rollback();
                return false;
            }
            rs.close();
            pstmtCheck.close();

            // VALIDACIÓN 3: Verificar calificación del cliente
            String sqlCheckCliente = "SELECT CALIFICACION, ACTIVO FROM CLIENTE WHERE ID_PERSONA = ?";
            pstmtCheck = conn.prepareStatement(sqlCheckCliente);
            pstmtCheck.setInt(1, prestamo.getIdCliente());
            rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                double calificacion = rs.getDouble("CALIFICACION");
                String activo = rs.getString("ACTIVO");

                if (!"S".equals(activo)) {
                    System.err.println("✗ ERROR: El cliente no está activo");
                    conn.rollback();
                    return false;
                }

                if (calificacion < 5.0) {
                    System.err.println("✗ ERROR: El cliente tiene calificación insuficiente: " + calificacion);
                    conn.rollback();
                    return false;
                }
            } else {
                System.err.println("✗ ERROR: El cliente no existe");
                conn.rollback();
                return false;
            }
            rs.close();
            pstmtCheck.close();

            // Insertar préstamo (los triggers calcularán interés y validarán artículo)
            String sqlInsert = "INSERT INTO PRESTAMO (ID_PRESTAMO, ID_ASESOR, ID_CLIENTE, ID_ARTICULO, " +
                             "MONTO, FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES, ESTADO_PRESTAMO) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmtInsert = conn.prepareStatement(sqlInsert);
            pstmtInsert.setInt(1, prestamo.getIdPrestamo());
            pstmtInsert.setInt(2, prestamo.getIdAsesor());
            pstmtInsert.setInt(3, prestamo.getIdCliente());
            pstmtInsert.setInt(4, prestamo.getIdArticulo());
            pstmtInsert.setDouble(5, prestamo.getMonto());

            if (prestamo.getFechaPrestamo() != null) {
                pstmtInsert.setDate(6, new java.sql.Date(prestamo.getFechaPrestamo().getTime()));
            } else {
                pstmtInsert.setDate(6, new java.sql.Date(System.currentTimeMillis()));
            }

            pstmtInsert.setDate(7, new java.sql.Date(prestamo.getFechaVencimiento().getTime()));
            pstmtInsert.setDouble(8, prestamo.getTasaInteres());
            pstmtInsert.setString(9, "ACTIVO");

            int filas = pstmtInsert.executeUpdate();
            System.out.println("  ✓ INSERT en PRESTAMO: " + filas + " fila(s) insertada(s)");

            if (filas > 0) {
                conn.commit();
                System.out.println("  ✓ Transacción confirmada (COMMIT)");
                System.out.println("✓ Préstamo creado exitosamente con ID: " + prestamo.getIdPrestamo());
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al crear préstamo");
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
                if (pstmtInsert != null) pstmtInsert.close();
                if (conn != null) conn.setAutoCommit(true);
                System.out.println("  → Recursos liberados\n");
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene un préstamo por su ID con información completa.
     *
     * @param idPrestamo ID del préstamo a buscar
     * @return objeto Prestamo con los datos completos
     */
    public Prestamo obtenerPrestamoPorId(int idPrestamo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Prestamo prestamo = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Buscando préstamo con ID: " + idPrestamo);

            String sql = "SELECT p.ID_PRESTAMO, p.ID_ASESOR, p.ID_CLIENTE, p.ID_ARTICULO, " +
                        "p.MONTO, p.INTERES_GENERADO, 0 AS MULTA, " +
                        "p.FECHA_PRESTAMO, p.FECHA_VENCIMIENTO, p.TASA_INTERES, p.ESTADO_PRESTAMO " +
                        "FROM PRESTAMO p WHERE p.ID_PRESTAMO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPrestamo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));

                java.sql.Date fechaPrestamo = rs.getDate("FECHA_PRESTAMO");
                if (fechaPrestamo != null) {
                    prestamo.setFechaPrestamo(new java.util.Date(fechaPrestamo.getTime()));
                }

                java.sql.Date fechaVencimiento = rs.getDate("FECHA_VENCIMIENTO");
                if (fechaVencimiento != null) {
                    prestamo.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
                }

                System.out.println("✓ Préstamo encontrado - Estado: " + prestamo.getEstadoPrestamo());
            } else {
                System.out.println("  ⚠ No se encontró préstamo con ID: " + idPrestamo);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener préstamo");
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

        return prestamo;
    }

    /**
     * Lista todos los préstamos de un cliente específico.
     *
     * @param idCliente ID del cliente
     * @return lista de préstamos del cliente
     */
    public List<Prestamo> listarPrestamosPorCliente(int idCliente) {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando préstamos del cliente ID: " + idCliente);

            String sql = "SELECT ID_PRESTAMO, ID_ASESOR, ID_CLIENTE, ID_ARTICULO, " +
                        "MONTO, INTERES_GENERADO, NVL(MULTA, 0) AS MULTA, " +
                        "FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES, ESTADO_PRESTAMO " +
                        "FROM PRESTAMO WHERE ID_CLIENTE = ? ORDER BY FECHA_PRESTAMO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));

                java.sql.Date fechaPrestamo = rs.getDate("FECHA_PRESTAMO");
                if (fechaPrestamo != null) {
                    prestamo.setFechaPrestamo(new java.util.Date(fechaPrestamo.getTime()));
                }

                java.sql.Date fechaVencimiento = rs.getDate("FECHA_VENCIMIENTO");
                if (fechaVencimiento != null) {
                    prestamo.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
                }

                prestamos.add(prestamo);
            }

            System.out.println("✓ Préstamos del cliente: " + prestamos.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al listar préstamos por cliente");
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

        return prestamos;
    }

    /**
     * Lista préstamos filtrados por estado.
     *
     * @param estado Estado del préstamo (ACTIVO, CANCELADO, EN_MORA, VENCIDO, ARTICULO_TRANSFERIDO)
     * @return lista de préstamos con el estado especificado
     */
    public List<Prestamo> listarPrestamosPorEstado(String estado) {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando préstamos con estado: " + estado);

            String sql = "SELECT ID_PRESTAMO, ID_ASESOR, ID_CLIENTE, ID_ARTICULO, " +
                        "MONTO, INTERES_GENERADO, NVL(MULTA, 0) AS MULTA, " +
                        "FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES, ESTADO_PRESTAMO " +
                        "FROM PRESTAMO WHERE ESTADO_PRESTAMO = ? ORDER BY FECHA_VENCIMIENTO";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, estado);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));

                java.sql.Date fechaPrestamo = rs.getDate("FECHA_PRESTAMO");
                if (fechaPrestamo != null) {
                    prestamo.setFechaPrestamo(new java.util.Date(fechaPrestamo.getTime()));
                }

                java.sql.Date fechaVencimiento = rs.getDate("FECHA_VENCIMIENTO");
                if (fechaVencimiento != null) {
                    prestamo.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
                }

                prestamos.add(prestamo);
            }

            System.out.println("✓ Préstamos encontrados: " + prestamos.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al listar préstamos por estado");
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

        return prestamos;
    }

    /**
     * Lista todos los préstamos vencidos.
     *
     * @return lista de préstamos vencidos
     */
    public List<Prestamo> listarPrestamosVencidos() {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando préstamos vencidos");

            String sql = "SELECT ID_PRESTAMO, ID_ASESOR, ID_CLIENTE, ID_ARTICULO, " +
                        "MONTO, INTERES_GENERADO, NVL(MULTA, 0) AS MULTA, " +
                        "FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES, ESTADO_PRESTAMO, " +
                        "TRUNC(SYSDATE - FECHA_VENCIMIENTO) AS DIAS_VENCIDO " +
                        "FROM PRESTAMO " +
                        "WHERE ESTADO_PRESTAMO = 'VENCIDO' " +
                        "ORDER BY FECHA_VENCIMIENTO";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));

                java.sql.Date fechaPrestamo = rs.getDate("FECHA_PRESTAMO");
                if (fechaPrestamo != null) {
                    prestamo.setFechaPrestamo(new java.util.Date(fechaPrestamo.getTime()));
                }

                java.sql.Date fechaVencimiento = rs.getDate("FECHA_VENCIMIENTO");
                if (fechaVencimiento != null) {
                    prestamo.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
                }

                prestamos.add(prestamo);
            }

            System.out.println("✓ Préstamos vencidos: " + prestamos.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al listar préstamos vencidos");
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

        return prestamos;
    }

    /**
     * Registra un pago para un préstamo.
     * Si el pago cubre la deuda total, cambia el estado a CANCELADO.
     *
     * @param idPrestamo ID del préstamo
     * @param montoPago Monto del pago realizado
     * @return true si el registro fue exitoso, false en caso contrario
     */
    public boolean registrarPago(int idPrestamo, double montoPago) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Registrando pago para préstamo ID: " + idPrestamo);
            System.out.println("  Monto del pago: $" + montoPago);

            conn.setAutoCommit(false);

            // Obtener deuda total
            String sqlCheck = "SELECT MONTO, INTERES_GENERADO, NVL(MULTA, 0) AS MULTA, ESTADO_PRESTAMO " +
                            "FROM PRESTAMO WHERE ID_PRESTAMO = ?";

            pstmtCheck = conn.prepareStatement(sqlCheck);
            pstmtCheck.setInt(1, idPrestamo);
            rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                double monto = rs.getDouble("MONTO");
                double interes = rs.getDouble("INTERES_GENERADO");
                double multa = rs.getDouble("MULTA");
                String estado = rs.getString("ESTADO_PRESTAMO");
                double deudaTotal = monto + interes + multa;

                System.out.println("  Deuda total: $" + deudaTotal);

                if ("CANCELADO".equals(estado)) {
                    System.err.println("✗ ERROR: El préstamo ya está cancelado");
                    conn.rollback();
                    return false;
                }

                // Si el pago cubre la deuda total, cancelar préstamo
                String nuevoEstado = (montoPago >= deudaTotal) ? "CANCELADO" : estado;

                String sqlUpdate = "UPDATE PRESTAMO SET ESTADO_PRESTAMO = ? WHERE ID_PRESTAMO = ?";
                pstmtUpdate = conn.prepareStatement(sqlUpdate);
                pstmtUpdate.setString(1, nuevoEstado);
                pstmtUpdate.setInt(2, idPrestamo);

                int filas = pstmtUpdate.executeUpdate();

                if (filas > 0) {
                    conn.commit();
                    System.out.println("  ✓ Pago registrado - Nuevo estado: " + nuevoEstado);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } else {
                System.err.println("✗ ERROR: Préstamo no encontrado");
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al registrar pago");
            System.err.println("  Mensaje: " + e.getMessage());

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("  ✗ Error en rollback: " + ex.getMessage());
            }
            return false;

        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtCheck != null) pstmtCheck.close();
                if (pstmtUpdate != null) pstmtUpdate.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Aplica multa manualmente a un préstamo en mora.
     *
     * @param idPrestamo ID del préstamo
     * @return true si se aplicó la multa, false en caso contrario
     */
    public boolean aplicarMulta(int idPrestamo) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Aplicando multa a préstamo ID: " + idPrestamo);

            // Cambiar estado a EN_MORA para que el trigger aplique la multa
            String sql = "{CALL BEGIN UPDATE PRESTAMO SET ESTADO_PRESTAMO = 'EN_MORA' " +
                        "WHERE ID_PRESTAMO = ? AND ESTADO_PRESTAMO != 'CANCELADO'; END;}";

            cstmt = conn.prepareCall(sql);
            cstmt.setInt(1, idPrestamo);
            cstmt.execute();

            System.out.println("✓ Multa aplicada exitosamente");
            return true;

        } catch (SQLException e) {
            System.err.println("✗ Error al aplicar multa");
            System.err.println("  Mensaje: " + e.getMessage());
            return false;

        } finally {
            try {
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Cancela un préstamo (marca como CANCELADO).
     *
     * @param idPrestamo ID del préstamo a cancelar
     * @return true si la cancelación fue exitosa, false en caso contrario
     */
    public boolean cancelarPrestamo(int idPrestamo) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Cancelando préstamo ID: " + idPrestamo);

            conn.setAutoCommit(false);

            String sql = "UPDATE PRESTAMO SET ESTADO_PRESTAMO = 'CANCELADO' " +
                        "WHERE ID_PRESTAMO = ? AND ESTADO_PRESTAMO != 'CANCELADO'";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPrestamo);

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                conn.commit();
                System.out.println("✓ Préstamo cancelado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("  ⚠ No se pudo cancelar el préstamo");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al cancelar préstamo");
            System.err.println("  Mensaje: " + e.getMessage());

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("  ✗ Error en rollback: " + ex.getMessage());
            }
            return false;

        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Lista todos los préstamos del sistema.
     *
     * @return lista completa de préstamos
     */
    public List<Prestamo> listarTodosPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando todos los préstamos");

            String sql = "SELECT ID_PRESTAMO, ID_ASESOR, ID_CLIENTE, ID_ARTICULO, " +
                        "MONTO, INTERES_GENERADO, NVL(MULTA, 0) AS MULTA, " +
                        "FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES, ESTADO_PRESTAMO " +
                        "FROM PRESTAMO ORDER BY FECHA_PRESTAMO DESC";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));

                java.sql.Date fechaPrestamo = rs.getDate("FECHA_PRESTAMO");
                if (fechaPrestamo != null) {
                    prestamo.setFechaPrestamo(new java.util.Date(fechaPrestamo.getTime()));
                }

                java.sql.Date fechaVencimiento = rs.getDate("FECHA_VENCIMIENTO");
                if (fechaVencimiento != null) {
                    prestamo.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
                }

                prestamos.add(prestamo);
            }

            System.out.println("✓ Total de préstamos: " + prestamos.size());

        } catch (SQLException e) {
            System.err.println("✗ Error al listar préstamos");
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

        return prestamos;
    }
}
