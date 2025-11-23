package com.prestamos.dao;

import java.sql.*;
import java.util.*;
import com.prestamos.modelo.Prestamo;
import com.prestamos.config.ConexionOracle;
import com.prestamos.util.ValidacionesNegocio;

/**
 * Clase DAO (Data Access Object) para gestionar operaciones de Préstamo.
 * Implementa todas las operaciones CRUD y de negocio para préstamos.
 *
 * @author Sistema de Préstamos
 * @version 2.0
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

                if ("DEFECTUOSO".equalsIgnoreCase(estadoArticulo)) {
                    System.err.println("✗ ERROR: El artículo está en estado DEFECTUOSO");
                    conn.rollback();
                    return false;
                }

                // VALIDACIÓN 2: Verificar que el monto no exceda el 80% del valor tasado
                if (!ValidacionesNegocio.validarMontoPrestamo(prestamo.getMonto(), valorTasado)) {
                    conn.rollback();
                    return false;
                }
            } else {
                System.err.println("✗ ERROR: No se encontró el artículo especificado");
                conn.rollback();
                return false;
            }
            rs.close();
            pstmtCheck.close();

            // VALIDACIÓN 3: Verificar calificación del cliente
            String sqlCheckCliente = "SELECT c.CALIFICACION FROM CLIENTE c WHERE c.ID_CLIENTE = ?";
            pstmtCheck = conn.prepareStatement(sqlCheckCliente);
            pstmtCheck.setInt(1, prestamo.getIdCliente());
            rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                double calificacion = rs.getDouble("CALIFICACION");
                if (calificacion < 3.0) {
                    System.err.println("✗ ERROR: El cliente requiere calificación mínima de 3.0. Actual: " + calificacion);
                    conn.rollback();
                    return false;
                }
            }
            rs.close();
            pstmtCheck.close();

            // Calcular tasa de interés según el plazo
            double tasaInteres = ValidacionesNegocio.calcularTasaInteres(
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento()
            );
            double interesGenerado = ValidacionesNegocio.calcularInteresGenerado(
                prestamo.getMonto(),
                tasaInteres
            );

            // Insertar préstamo
            String sqlInsert = "INSERT INTO PRESTAMO (ID_PRESTAMO, ID_CLIENTE, ID_ARTICULO, ID_ASESOR, " +
                              "ESTADO_PRESTAMO, MONTO, INTERES_GENERADO, FECHA_PRESTAMO, FECHA_VENCIMIENTO, " +
                              "TASA_INTERES, MULTA) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmtInsert = conn.prepareStatement(sqlInsert);
            pstmtInsert.setInt(1, prestamo.getIdPrestamo());
            pstmtInsert.setInt(2, prestamo.getIdCliente());
            pstmtInsert.setInt(3, prestamo.getIdArticulo());
            pstmtInsert.setInt(4, prestamo.getIdAsesor());
            pstmtInsert.setString(5, "ACTIVO");
            pstmtInsert.setDouble(6, prestamo.getMonto());
            pstmtInsert.setDouble(7, interesGenerado);
            pstmtInsert.setDate(8, new java.sql.Date(prestamo.getFechaPrestamo().getTime()));
            pstmtInsert.setDate(9, new java.sql.Date(prestamo.getFechaVencimiento().getTime()));
            pstmtInsert.setDouble(10, tasaInteres);
            pstmtInsert.setDouble(11, 0.0); // Sin multa inicial

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
     * @return objeto Prestamo con los datos, null si no se encuentra
     */
    public Prestamo obtenerPrestamoPorId(int idPrestamo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Prestamo prestamo = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Buscando préstamo con ID: " + idPrestamo);

            String sql = "SELECT p.ID_PRESTAMO, p.ID_CLIENTE, p.ID_ARTICULO, p.ID_ASESOR, " +
                        "p.ESTADO_PRESTAMO, p.MONTO, p.INTERES_GENERADO, p.FECHA_PRESTAMO, " +
                        "p.FECHA_VENCIMIENTO, p.TASA_INTERES, p.MULTA " +
                        "FROM PRESTAMO p WHERE p.ID_PRESTAMO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPrestamo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setFechaPrestamo(rs.getDate("FECHA_PRESTAMO"));
                prestamo.setFechaVencimiento(rs.getDate("FECHA_VENCIMIENTO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));
                prestamo.setMulta(rs.getDouble("MULTA"));

                System.out.println("✓ Préstamo encontrado. Estado: " + prestamo.getEstadoPrestamo());
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

            String sql = "SELECT ID_PRESTAMO, ID_CLIENTE, ID_ASESOR, " +
                        "ESTADO_PRESTAMO, MONTO, INTERES_GENERADO, FECHA_PRESTAMO, " +
                        "FECHA_VENCIMIENTO, TASA_INTERES " +
                        "FROM PRESTAMO WHERE ID_CLIENTE = ? ORDER BY FECHA_PRESTAMO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                prestamo.setFechaPrestamo(rs.getDate("FECHA_PRESTAMO"));
                prestamo.setFechaVencimiento(rs.getDate("FECHA_VENCIMIENTO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));

                prestamos.add(prestamo);
            }

            System.out.println("✓ Se encontraron " + prestamos.size() + " préstamos");

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
     * Lista todos los préstamos de la base de datos.
     *
     * @return lista de todos los préstamos
     */
    public List<Prestamo> listarTodosPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Listando todos los préstamos");

            String sql = "SELECT ID_PRESTAMO, ID_CLIENTE, ID_ASESOR, ESTADO_PRESTAMO, MONTO, INTERES_GENERADO, FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES FROM PRESTAMO ORDER BY FECHA_PRESTAMO DESC";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                // prestamo.setMulta(0.0); // No existe la columna MULTA
                prestamo.setFechaPrestamo(rs.getDate("FECHA_PRESTAMO"));
                prestamo.setFechaVencimiento(rs.getDate("FECHA_VENCIMIENTO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));
                prestamos.add(prestamo);
            }

            System.out.println("✓ Se encontraron " + prestamos.size() + " préstamos");

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

    /**
     * Lista todos los préstamos filtrados por estado.
     *
     * @param estado estado del préstamo a filtrar
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

            String sql = "SELECT ID_PRESTAMO, ID_CLIENTE, ID_ASESOR, ESTADO_PRESTAMO, MONTO, INTERES_GENERADO, FECHA_PRESTAMO, FECHA_VENCIMIENTO, TASA_INTERES FROM PRESTAMO WHERE ESTADO_PRESTAMO = ? ORDER BY FECHA_PRESTAMO DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, estado);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
                prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
                prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
                prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));
                prestamo.setMonto(rs.getDouble("MONTO"));
                prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
                // prestamo.setMulta(0.0); // No existe la columna MULTA
                prestamo.setFechaPrestamo(rs.getDate("FECHA_PRESTAMO"));
                prestamo.setFechaVencimiento(rs.getDate("FECHA_VENCIMIENTO"));
                prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));
                prestamos.add(prestamo);
            }

            System.out.println("✓ Se encontraron " + prestamos.size() + " préstamos con estado " + estado);

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
     * Aplica una multa a un préstamo por mora.
     *
     * @param idPrestamo ID del préstamo
     * @param montoMulta monto de la multa a aplicar
     * @return true si se aplicó correctamente, false en caso contrario
     */
    public boolean aplicarMulta(int idPrestamo, double montoMulta) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Aplicando multa al préstamo ID: " + idPrestamo);

            conn.setAutoCommit(false);

            String sql = "UPDATE PRESTAMO SET MULTA = MULTA + ?, ESTADO_PRESTAMO = 'EN_MORA' " +
                        "WHERE ID_PRESTAMO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, montoMulta);
            pstmt.setInt(2, idPrestamo);

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                conn.commit();
                System.out.println("✓ Multa aplicada exitosamente: $" + montoMulta);
                return true;
            } else {
                conn.rollback();
                System.err.println("✗ No se encontró el préstamo");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al aplicar multa");
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
     * Actualiza el estado de un préstamo.
     *
     * @param idPrestamo ID del préstamo
     * @param nuevoEstado nuevo estado del préstamo
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarEstadoPrestamo(int idPrestamo, String nuevoEstado) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Actualizando estado del préstamo ID: " + idPrestamo + " a: " + nuevoEstado);

            conn.setAutoCommit(false);

            String sql = "UPDATE PRESTAMO SET ESTADO_PRESTAMO = ? WHERE ID_PRESTAMO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idPrestamo);

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                conn.commit();
                System.out.println("✓ Estado actualizado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("✗ No se encontró el préstamo");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar estado");
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
     * Registra un pago para un préstamo.
     *
     * @param idPrestamo ID del préstamo
     * @param montoPago monto del pago a registrar
     * @return true si se registró correctamente, false en caso contrario
     */
    public boolean registrarPago(int idPrestamo, double montoPago) {
        Connection conn = null;
        PreparedStatement pstmtConsulta = null;
        PreparedStatement pstmtActualiza = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Registrando pago de $" + montoPago + " para préstamo ID: " + idPrestamo);

            conn.setAutoCommit(false);

            // Obtener información del préstamo actual
            String sqlConsulta = "SELECT MONTO, INTERES_GENERADO, MULTA FROM PRESTAMO WHERE ID_PRESTAMO = ?";
            pstmtConsulta = conn.prepareStatement(sqlConsulta);
            pstmtConsulta.setInt(1, idPrestamo);
            rs = pstmtConsulta.executeQuery();

            if (rs.next()) {
                double monto = rs.getDouble("MONTO");
                double interes = rs.getDouble("INTERES_GENERADO");
                double multa = rs.getDouble("MULTA");
                double totalDeuda = monto + interes + multa;
                double pagoRestante = montoPago;

                // Descontar primero multa
                double pagoMulta = Math.min(multa, pagoRestante);
                multa -= pagoMulta;
                pagoRestante -= pagoMulta;

                // Luego interés
                double pagoInteres = Math.min(interes, pagoRestante);
                interes -= pagoInteres;
                pagoRestante -= pagoInteres;

                // Finalmente monto
                double pagoMonto = Math.min(monto, pagoRestante);
                monto -= pagoMonto;
                pagoRestante -= pagoMonto;

                if (monto <= 0 && interes <= 0 && multa <= 0) {
                    // Pago completo - cambiar estado a CANCELADO y dejar todo en 0
                    String sqlActualiza = "UPDATE PRESTAMO SET ESTADO_PRESTAMO = 'CANCELADO', MONTO = 0, INTERES_GENERADO = 0, MULTA = 0 WHERE ID_PRESTAMO = ?";
                    pstmtActualiza = conn.prepareStatement(sqlActualiza);
                    pstmtActualiza.setInt(1, idPrestamo);
                    pstmtActualiza.executeUpdate();
                    conn.commit();
                    System.out.println("✓ Pago completo registrado. Préstamo cancelado.");
                    return true;
                } else {
                    // Pago parcial - actualizar los valores restantes
                    String sqlActualiza = "UPDATE PRESTAMO SET MONTO = ?, INTERES_GENERADO = ?, MULTA = ? WHERE ID_PRESTAMO = ?";
                    pstmtActualiza = conn.prepareStatement(sqlActualiza);
                    pstmtActualiza.setDouble(1, monto);
                    pstmtActualiza.setDouble(2, interes);
                    pstmtActualiza.setDouble(3, multa);
                    pstmtActualiza.setInt(4, idPrestamo);
                    pstmtActualiza.executeUpdate();
                    conn.commit();
                    System.out.println("✓ Pago parcial registrado: $" + montoPago);
                    System.out.println("  Deuda restante: $" + (monto + interes + multa));
                    return true;
                }
            } else {
                System.err.println("✗ No se encontró el préstamo");
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
                if (pstmtConsulta != null) rs.close();
                if (pstmtActualiza != null) pstmtActualiza.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("  ✗ Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Cancela un préstamo cambiando su estado a CANCELADO.
     *
     * @param idPrestamo ID del préstamo a cancelar
     * @return true si se canceló correctamente, false en caso contrario
     */
    public boolean cancelarPrestamo(int idPrestamo) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionOracle.getConexion();
            System.out.println("→ Cancelando préstamo ID: " + idPrestamo);

            conn.setAutoCommit(false);

            String sql = "UPDATE PRESTAMO SET ESTADO_PRESTAMO = 'CANCELADO' WHERE ID_PRESTAMO = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPrestamo);

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                conn.commit();
                System.out.println("✓ Préstamo cancelado exitosamente");
                return true;
            } else {
                conn.rollback();
                System.err.println("✗ No se encontró el préstamo");
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
     * Obtiene el siguiente ID disponible para un nuevo préstamo.
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

            String sql = "SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 AS SIGUIENTE_ID FROM PRESTAMO";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                siguienteId = rs.getInt("SIGUIENTE_ID");
            }

            System.out.println("→ Siguiente ID de préstamo disponible: " + siguienteId);

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
