package com.prestamos.modelo;

/**
 * Clase que representa una Persona en el sistema de préstamos.
 * Es la clase base para Cliente, Asesor y Administrador.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class Persona {

    /**
     * Identificador único de la persona
     */
    private int idPersona;

    /**
     * Nombre completo de la persona
     */
    private String nombrePersona;

    /**
     * Tipo de identificación (DNI, CE, RUC, etc.)
     */
    private String tipoId;

    /**
     * Tipo de persona (CLIENTE, ASESOR, ADMINISTRADOR)
     */
    private String tipoPersona;

    /**
     * Constructor vacío de la clase Persona.
     */
    public Persona() {
    }

    /**
     * Constructor con todos los parámetros de la clase Persona.
     *
     * @param idPersona identificador único de la persona
     * @param nombrePersona nombre completo de la persona
     * @param tipoId tipo de identificación
     * @param tipoPersona tipo de persona en el sistema
     */
    public Persona(int idPersona, String nombrePersona, String tipoId, String tipoPersona) {
        this.idPersona = idPersona;
        this.nombrePersona = nombrePersona;
        this.tipoId = tipoId;
        this.tipoPersona = tipoPersona;
    }

    /**
     * Obtiene el identificador de la persona.
     *
     * @return idPersona identificador único de la persona
     */
    public int getIdPersona() {
        return idPersona;
    }

    /**
     * Establece el identificador de la persona.
     *
     * @param idPersona identificador único de la persona
     */
    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    /**
     * Obtiene el nombre de la persona.
     *
     * @return nombrePersona nombre completo de la persona
     */
    public String getNombrePersona() {
        return nombrePersona;
    }

    /**
     * Establece el nombre de la persona.
     *
     * @param nombrePersona nombre completo de la persona
     */
    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    /**
     * Obtiene el tipo de identificación.
     *
     * @return tipoId tipo de identificación
     */
    public String getTipoId() {
        return tipoId;
    }

    /**
     * Establece el tipo de identificación.
     *
     * @param tipoId tipo de identificación
     */
    public void setTipoId(String tipoId) {
        this.tipoId = tipoId;
    }

    /**
     * Obtiene el tipo de persona.
     *
     * @return tipoPersona tipo de persona en el sistema
     */
    public String getTipoPersona() {
        return tipoPersona;
    }

    /**
     * Establece el tipo de persona.
     *
     * @param tipoPersona tipo de persona en el sistema
     */
    public void setTipoPersona(String tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    /**
     * Retorna una representación en cadena de la persona.
     *
     * @return String con todos los atributos de la persona
     */
    @Override
    public String toString() {
        return "Persona{" +
                "idPersona=" + idPersona +
                ", nombrePersona='" + nombrePersona + '\'' +
                ", tipoId='" + tipoId + '\'' +
                ", tipoPersona='" + tipoPersona + '\'' +
                '}';
    }
}

