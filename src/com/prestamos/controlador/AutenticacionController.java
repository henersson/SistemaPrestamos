package com.prestamos.controlador;

import java.sql.*;
import com.prestamos.modelo.Usuario;
import com.prestamos.config.ConexionOracle;

/**
 * Controlador para gestionar la autenticación de usuarios en el sistema.
 * Valida credenciales contra la base de datos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class AutenticacionController {

    /**
     * Valida las credenciales de un usuario contra la base de datos.
     * Solo permite acceso a usuarios de tipo ADMINISTRADOR o ASESOR.
     *
     * @param usuario código de usuario (ID_PERSONA)
     * @param password contraseña (no utilizada en esta implementación básica)
     * @return objeto Usuario si las credenciales son válidas, null en caso contrario
     */
    public Usuario validarUsuario(String usuario, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuarioAutenticado = null;

        try {
            // Convertir usuario a int
            int idPersona;
            try {
                idPersona = Integer.parseInt(usuario);
                System.out.println("→ Validando usuario con ID: " + idPersona);
            } catch (NumberFormatException e) {
                System.err.println("✗ Error: El usuario debe ser un número válido");
                System.err.println("  Valor ingresado: " + usuario);
                return null;
            }

            // Obtener conexión
            conn = ConexionOracle.getConexion();

            // Preparar consulta SQL
            String sql = "SELECT p.ID_PERSONA, p.NOMBRE_PERSONA, p.TIPO_PERSONA " +
                        "FROM PERSONA p " +
                        "WHERE p.ID_PERSONA = ? " +
                        "AND p.TIPO_PERSONA IN ('ADMINISTRADOR', 'ASESOR')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPersona);

            rs = pstmt.executeQuery();

            // Si encuentra el registro, crear objeto Usuario
            if (rs.next()) {
                usuarioAutenticado = new Usuario(
                    rs.getInt("ID_PERSONA"),
                    rs.getString("NOMBRE_PERSONA"),
                    rs.getString("TIPO_PERSONA")
                );
                System.out.println("  ✓ Usuario autenticado: " + usuarioAutenticado.getNombre());
                System.out.println("  ✓ Tipo: " + usuarioAutenticado.getTipoPersona());
            } else {
                System.out.println("  ✗ Usuario no encontrado o no tiene permisos de acceso");
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al validar usuario");
            System.err.println("  Mensaje: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getErrorCode());
            usuarioAutenticado = null;

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

        return usuarioAutenticado;
    }
}

