package com.prestamos.modelo;

/**
 * Clase que representa un Usuario autenticado en el sistema de préstamos.
 * Contiene la información básica del usuario para la sesión.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class Usuario {

    /**
     * Identificador único del usuario (ID_PERSONA)
     */
    private int idPersona;

    /**
     * Nombre completo del usuario
     */
    private String nombre;

    /**
     * Tipo de persona/rol del usuario (ADMINISTRADOR, ASESOR)
     */
    private String tipoPersona;

    /**
     * Constructor vacío de la clase Usuario.
     */
    public Usuario() {
    }

    /**
     * Constructor con todos los parámetros de la clase Usuario.
     *
     * @param idPersona identificador único del usuario
     * @param nombre nombre completo del usuario
     * @param tipoPersona tipo de persona/rol del usuario
     */
    public Usuario(int idPersona, String nombre, String tipoPersona) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.tipoPersona = tipoPersona;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return idPersona identificador único del usuario
     */
    public int getIdPersona() {
        return idPersona;
    }

    /**
     * Establece el identificador del usuario.
     *
     * @param idPersona identificador único del usuario
     */
    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre nombre completo del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param nombre nombre completo del usuario
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el tipo de persona del usuario.
     *
     * @return tipoPersona tipo de persona/rol del usuario
     */
    public String getTipoPersona() {
        return tipoPersona;
    }

    /**
     * Establece el tipo de persona del usuario.
     *
     * @param tipoPersona tipo de persona/rol del usuario
     */
    public void setTipoPersona(String tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    /**
     * Retorna una representación en cadena del usuario.
     *
     * @return String con todos los atributos del usuario
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "idPersona=" + idPersona +
                ", nombre='" + nombre + '\'' +
                ", tipoPersona='" + tipoPersona + '\'' +
                '}';
    }
}

