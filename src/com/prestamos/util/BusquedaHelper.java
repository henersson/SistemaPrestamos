package com.prestamos.util;

import com.prestamos.config.ConexionOracle;
import com.prestamos.modelo.Cliente;
import com.prestamos.modelo.Articulo;
import com.prestamos.modelo.Prestamo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase helper para búsquedas avanzadas en el sistema.
 * Proporciona métodos de búsqueda flexibles con múltiples criterios.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class BusquedaHelper {

    /**
     * Tipos de búsqueda soportados para clientes.
     */
    public enum TipoBusquedaCliente {
        POR_ID,
        POR_NOMBRE,
        POR_TIPO_DOCUMENTO,
        POR_CALIFICACION_MINIMA,
        POR_CALIFICACION_RANGO
    }

    /**
     * Tipos de búsqueda soportados para artículos.
     */
    public enum TipoBusquedaArticulo {
        POR_ID,
        POR_TIPO,
        POR_DESCRIPCION,
        POR_ESTADO,
        POR_VALOR_RANGO
    }

    /**
     * Tipos de búsqueda soportados para préstamos.
     */
    public enum TipoBusquedaPrestamo {
        POR_ID,
        POR_CLIENTE,
        POR_ESTADO,
        POR_RANGO_FECHAS,
        POR_MONTO_RANGO
    }

    /**
     * Busca clientes según el criterio especificado.
     *
     * @param criterio valor de búsqueda
     * @param tipo tipo de búsqueda a realizar
     * @return lista de clientes que coinciden
     */
    public static List<Cliente> buscarClientes(String criterio, TipoBusquedaCliente tipo) {
        List<Cliente> resultados = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            String sql = construirSQLBusquedaClientes(tipo);

            if (sql == null) {
                System.err.println("Tipo de búsqueda no válido");
                return resultados;
            }

            pstmt = conn.prepareStatement(sql);
            asignarParametrosBusquedaClientes(pstmt, criterio, tipo);

            System.out.println("→ Ejecutando búsqueda de clientes: " + tipo + " = " + criterio);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Cliente cliente = mapearCliente(rs);
                resultados.add(cliente);
            }

            System.out.println("✓ Clientes encontrados: " + resultados.size());

        } catch (SQLException e) {
            System.err.println("✗ Error en búsqueda de clientes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, pstmt);
        }

        return resultados;
    }

    /**
     * Busca artículos según el criterio especificado.
     *
     * @param criterio valor de búsqueda
     * @param tipo tipo de búsqueda a realizar
     * @return lista de artículos que coinciden
     */
    public static List<Articulo> buscarArticulos(String criterio, TipoBusquedaArticulo tipo) {
        List<Articulo> resultados = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            String sql = construirSQLBusquedaArticulos(tipo);

            if (sql == null) {
                System.err.println("Tipo de búsqueda no válido");
                return resultados;
            }

            pstmt = conn.prepareStatement(sql);
            asignarParametrosBusquedaArticulos(pstmt, criterio, tipo);

            System.out.println("→ Ejecutando búsqueda de artículos: " + tipo + " = " + criterio);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Articulo articulo = mapearArticulo(rs);
                resultados.add(articulo);
            }

            System.out.println("✓ Artículos encontrados: " + resultados.size());

        } catch (SQLException e) {
            System.err.println("✗ Error en búsqueda de artículos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, pstmt);
        }

        return resultados;
    }

    /**
     * Busca préstamos según el criterio especificado.
     *
     * @param criterio valor de búsqueda
     * @param tipo tipo de búsqueda a realizar
     * @return lista de préstamos que coinciden
     */
    public static List<Prestamo> buscarPrestamos(String criterio, TipoBusquedaPrestamo tipo) {
        List<Prestamo> resultados = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.getConexion();
            String sql = construirSQLBusquedaPrestamos(tipo);

            if (sql == null) {
                System.err.println("Tipo de búsqueda no válido");
                return resultados;
            }

            pstmt = conn.prepareStatement(sql);
            asignarParametrosBusquedaPrestamos(pstmt, criterio, tipo);

            System.out.println("→ Ejecutando búsqueda de préstamos: " + tipo + " = " + criterio);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Prestamo prestamo = mapearPrestamo(rs);
                resultados.add(prestamo);
            }

            System.out.println("✓ Préstamos encontrados: " + resultados.size());

        } catch (SQLException e) {
            System.err.println("✗ Error en búsqueda de préstamos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, pstmt);
        }

        return resultados;
    }

    // ==================== MÉTODOS PRIVADOS DE CONSTRUCCIÓN SQL ====================

    private static String construirSQLBusquedaClientes(TipoBusquedaCliente tipo) {
        String baseSQL =
            "SELECT c.ID_PERSONA, c.ID_CLIENTE, p.NOMBRE_PERSONA, p.TIPOID, " +
            "c.CALIFICACION, c.FECHA_REGISTRO, c.ACTIVO " +
            "FROM CLIENTE c " +
            "JOIN PERSONA p ON c.ID_PERSONA = p.ID_PERSONA ";

        switch (tipo) {
            case POR_ID:
                return baseSQL + "WHERE c.ID_CLIENTE = ? ORDER BY c.ID_CLIENTE";

            case POR_NOMBRE:
                return baseSQL + "WHERE UPPER(p.NOMBRE_PERSONA) LIKE UPPER(?) ORDER BY p.NOMBRE_PERSONA";

            case POR_TIPO_DOCUMENTO:
                return baseSQL + "WHERE p.TIPOID = ? ORDER BY p.NOMBRE_PERSONA";

            case POR_CALIFICACION_MINIMA:
                return baseSQL + "WHERE c.CALIFICACION >= ? ORDER BY c.CALIFICACION DESC";

            case POR_CALIFICACION_RANGO:
                return baseSQL + "WHERE c.CALIFICACION BETWEEN ? AND ? ORDER BY c.CALIFICACION DESC";

            default:
                return null;
        }
    }

    private static String construirSQLBusquedaArticulos(TipoBusquedaArticulo tipo) {
        String baseSQL =
            "SELECT ID_ARTICULO, TIPO_ARTICULO, DESCRIPCION_ARTICULO, VALOR_TASADO, " +
            "ESTADO, PRECIO_MERCADO_BASE, PORCENTAJE_TASACION, FECHA_AVALUO " +
            "FROM ARTICULO ";

        switch (tipo) {
            case POR_ID:
                return baseSQL + "WHERE ID_ARTICULO = ?";

            case POR_TIPO:
                return baseSQL + "WHERE TIPO_ARTICULO = ? ORDER BY FECHA_AVALUO DESC";

            case POR_DESCRIPCION:
                return baseSQL + "WHERE UPPER(DESCRIPCION_ARTICULO) LIKE UPPER(?) ORDER BY FECHA_AVALUO DESC";

            case POR_ESTADO:
                return baseSQL + "WHERE ESTADO = ? ORDER BY VALOR_TASADO DESC";

            case POR_VALOR_RANGO:
                return baseSQL + "WHERE VALOR_TASADO BETWEEN ? AND ? ORDER BY VALOR_TASADO DESC";

            default:
                return null;
        }
    }

    private static String construirSQLBusquedaPrestamos(TipoBusquedaPrestamo tipo) {
        String baseSQL =
            "SELECT ID_PRESTAMO, ID_CLIENTE, ID_ARTICULO, ID_ASESOR, MONTO, " +
            "TASA_INTERES, INTERES_GENERADO, FECHA_PRESTAMO, FECHA_VENCIMIENTO, " +
            "ESTADO_PRESTAMO, 0 AS MULTA " +
            "FROM PRESTAMO ";

        switch (tipo) {
            case POR_ID:
                return baseSQL + "WHERE ID_PRESTAMO = ?";

            case POR_CLIENTE:
                return baseSQL + "WHERE ID_CLIENTE = ? ORDER BY FECHA_PRESTAMO DESC";

            case POR_ESTADO:
                return baseSQL + "WHERE ESTADO_PRESTAMO = ? ORDER BY FECHA_PRESTAMO DESC";

            case POR_RANGO_FECHAS:
                return baseSQL + "WHERE FECHA_PRESTAMO BETWEEN ? AND ? ORDER BY FECHA_PRESTAMO DESC";

            case POR_MONTO_RANGO:
                return baseSQL + "WHERE MONTO BETWEEN ? AND ? ORDER BY MONTO DESC";

            default:
                return null;
        }
    }

    // ==================== ASIGNACIÓN DE PARÁMETROS ====================

    private static void asignarParametrosBusquedaClientes(PreparedStatement pstmt, String criterio,
                                                          TipoBusquedaCliente tipo) throws SQLException {
        switch (tipo) {
            case POR_ID:
                pstmt.setInt(1, Integer.parseInt(criterio));
                break;

            case POR_NOMBRE:
                pstmt.setString(1, "%" + criterio + "%");
                break;

            case POR_TIPO_DOCUMENTO:
                pstmt.setString(1, criterio);
                break;

            case POR_CALIFICACION_MINIMA:
                pstmt.setDouble(1, Double.parseDouble(criterio));
                break;

            case POR_CALIFICACION_RANGO:
                String[] rango = criterio.split(",");
                pstmt.setDouble(1, Double.parseDouble(rango[0].trim()));
                pstmt.setDouble(2, Double.parseDouble(rango[1].trim()));
                break;
        }
    }

    private static void asignarParametrosBusquedaArticulos(PreparedStatement pstmt, String criterio,
                                                           TipoBusquedaArticulo tipo) throws SQLException {
        switch (tipo) {
            case POR_ID:
                pstmt.setInt(1, Integer.parseInt(criterio));
                break;

            case POR_TIPO:
            case POR_ESTADO:
                pstmt.setString(1, criterio);
                break;

            case POR_DESCRIPCION:
                pstmt.setString(1, "%" + criterio + "%");
                break;

            case POR_VALOR_RANGO:
                String[] rango = criterio.split(",");
                pstmt.setDouble(1, Double.parseDouble(rango[0].trim()));
                pstmt.setDouble(2, Double.parseDouble(rango[1].trim()));
                break;
        }
    }

    private static void asignarParametrosBusquedaPrestamos(PreparedStatement pstmt, String criterio,
                                                           TipoBusquedaPrestamo tipo) throws SQLException {
        switch (tipo) {
            case POR_ID:
            case POR_CLIENTE:
                pstmt.setInt(1, Integer.parseInt(criterio));
                break;

            case POR_ESTADO:
                pstmt.setString(1, criterio);
                break;

            case POR_RANGO_FECHAS:
                String[] fechas = criterio.split(",");
                pstmt.setDate(1, Date.valueOf(fechas[0].trim()));
                pstmt.setDate(2, Date.valueOf(fechas[1].trim()));
                break;

            case POR_MONTO_RANGO:
                String[] montos = criterio.split(",");
                pstmt.setDouble(1, Double.parseDouble(montos[0].trim()));
                pstmt.setDouble(2, Double.parseDouble(montos[1].trim()));
                break;
        }
    }

    // ==================== MAPEO DE RESULTADOS ====================

    private static Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdPersona(rs.getInt("ID_PERSONA"));
        cliente.setIdCliente(rs.getInt("ID_CLIENTE"));
        cliente.setNombrePersona(rs.getString("NOMBRE_PERSONA"));
        cliente.setTipoId(rs.getString("TIPOID"));
        cliente.setCalificacion(rs.getDouble("CALIFICACION"));

        Date fechaRegistro = rs.getDate("FECHA_REGISTRO");
        if (fechaRegistro != null) {
            cliente.setFechaRegistro(new java.util.Date(fechaRegistro.getTime()));
        }

        String activo = rs.getString("ACTIVO");
        cliente.setActivo(activo != null ? activo.charAt(0) : 'S');

        return cliente;
    }

    private static Articulo mapearArticulo(ResultSet rs) throws SQLException {
        Articulo articulo = new Articulo();
        articulo.setIdArticulo(rs.getInt("ID_ARTICULO"));
        articulo.setTipoArticulo(rs.getString("TIPO_ARTICULO"));
        articulo.setDescripcion(rs.getString("DESCRIPCION_ARTICULO"));
        articulo.setValorTasado(rs.getDouble("VALOR_TASADO"));
        articulo.setEstado(rs.getString("ESTADO"));
        articulo.setPrecioMercadoBase(rs.getDouble("PRECIO_MERCADO_BASE"));
        articulo.setPorcentajeTasacion(rs.getDouble("PORCENTAJE_TASACION"));

        Date fechaAvaluo = rs.getDate("FECHA_AVALUO");
        if (fechaAvaluo != null) {
            articulo.setFechaAvaluo(new java.util.Date(fechaAvaluo.getTime()));
        }

        return articulo;
    }

    private static Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getInt("ID_PRESTAMO"));
        prestamo.setIdCliente(rs.getInt("ID_CLIENTE"));
        prestamo.setIdArticulo(rs.getInt("ID_ARTICULO"));
        prestamo.setIdAsesor(rs.getInt("ID_ASESOR"));
        prestamo.setMonto(rs.getDouble("MONTO"));
        prestamo.setTasaInteres(rs.getDouble("TASA_INTERES"));
        prestamo.setInteresGenerado(rs.getDouble("INTERES_GENERADO"));
        prestamo.setEstadoPrestamo(rs.getString("ESTADO_PRESTAMO"));

        Date fechaPrestamo = rs.getDate("FECHA_PRESTAMO");
        if (fechaPrestamo != null) {
            prestamo.setFechaPrestamo(new java.util.Date(fechaPrestamo.getTime()));
        }

        Date fechaVencimiento = rs.getDate("FECHA_VENCIMIENTO");
        if (fechaVencimiento != null) {
            prestamo.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
        }

        return prestamo;
    }

    // ==================== UTILIDADES ====================

    /**
     * Cierra los recursos de base de datos de forma segura.
     */
    private static void cerrarRecursos(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar recursos: " + e.getMessage());
        }
    }

    /**
     * Resalta texto en una tabla (para implementación futura en UI).
     *
     * @param textoOriginal texto original
     * @param criterio criterio de búsqueda
     * @return texto con marcadores de resaltado
     */
    public static String resaltarTexto(String textoOriginal, String criterio) {
        if (textoOriginal == null || criterio == null || criterio.isEmpty()) {
            return textoOriginal;
        }

        // Retorna el texto con marcadores que la UI puede interpretar
        String criterioUpper = criterio.toUpperCase();
        String textoUpper = textoOriginal.toUpperCase();

        if (textoUpper.contains(criterioUpper)) {
            return "[HIGHLIGHT]" + textoOriginal + "[/HIGHLIGHT]";
        }

        return textoOriginal;
    }
}
