package com.prestamos.modelo;

import java.util.Date;

/**
 * Clase que representa un Asesor en el sistema de préstamos.
 * Extiende de Persona y añade atributos específicos del asesor.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class Asesor extends Persona {

    /**
     * Identificador único del asesor
     */
    private int idAsesor;

    /**
     * Identificador del administrador que supervisa al asesor
     */
    private int idAdministrador;

    /**
     * Fecha de contratación del asesor
     */
    private Date fechaContratacion;

    /**
     * Estado del asesor (S=Activo, N=Inactivo)
     */
    private char activo;

    /**
     * Especialidad o área de expertise del asesor
     */
    private String especialidad;

    /**
     * Constructor vacío de la clase Asesor.
     */
    public Asesor() {
        super();
    }

    /**
     * Constructor con todos los parámetros de la clase Asesor.
     *
     * @param idPersona identificador único de la persona
     * @param nombrePersona nombre completo de la persona
     * @param tipoId tipo de identificación
     * @param tipoPersona tipo de persona en el sistema
     * @param idAsesor identificador único del asesor
     * @param idAdministrador identificador del administrador supervisor
     * @param fechaContratacion fecha de contratación del asesor
     * @param activo estado del asesor (S/N)
     * @param especialidad especialidad del asesor
     */
    public Asesor(int idPersona, String nombrePersona, String tipoId, String tipoPersona,
                  int idAsesor, int idAdministrador, Date fechaContratacion,
                  char activo, String especialidad) {
        super(idPersona, nombrePersona, tipoId, tipoPersona);
        this.idAsesor = idAsesor;
        this.idAdministrador = idAdministrador;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
        this.especialidad = especialidad;
    }

    /**
     * Obtiene el identificador del asesor.
     *
     * @return idAsesor identificador único del asesor
     */
    public int getIdAsesor() {
        return idAsesor;
    }

    /**
     * Establece el identificador del asesor.
     *
     * @param idAsesor identificador único del asesor
     */
    public void setIdAsesor(int idAsesor) {
        this.idAsesor = idAsesor;
    }

    /**
     * Obtiene el identificador del administrador supervisor.
     *
     * @return idAdministrador identificador del administrador
     */
    public int getIdAdministrador() {
        return idAdministrador;
    }

    /**
     * Establece el identificador del administrador supervisor.
     *
     * @param idAdministrador identificador del administrador
     */
    public void setIdAdministrador(int idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    /**
     * Obtiene la fecha de contratación del asesor.
     *
     * @return fechaContratacion fecha de contratación
     */
    public Date getFechaContratacion() {
        return fechaContratacion;
    }

    /**
     * Establece la fecha de contratación del asesor.
     *
     * @param fechaContratacion fecha de contratación
     */
    public void setFechaContratacion(Date fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    /**
     * Obtiene el estado del asesor.
     *
     * @return activo estado del asesor (S/N)
     */
    public char getActivo() {
        return activo;
    }

    /**
     * Establece el estado del asesor.
     *
     * @param activo estado del asesor (S/N)
     */
    public void setActivo(char activo) {
        this.activo = activo;
    }

    /**
     * Obtiene la especialidad del asesor.
     *
     * @return especialidad especialidad o área de expertise
     */
    public String getEspecialidad() {
        return especialidad;
    }

    /**
     * Establece la especialidad del asesor.
     *
     * @param especialidad especialidad o área de expertise
     */
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    /**
     * Verifica si el asesor está activo en el sistema.
     *
     * @return true si el asesor está activo (activo == 'S'), false en caso contrario
     */
    public boolean esActivo() {
        return activo == 'S';
    }

    /**
     * Retorna una representación en cadena del asesor.
     * Incluye los atributos de Persona y los propios del Asesor.
     *
     * @return String con todos los atributos del asesor
     */
    @Override
    public String toString() {
        return "Asesor{" +
                super.toString() +
                ", idAsesor=" + idAsesor +
                ", idAdministrador=" + idAdministrador +
                ", fechaContratacion=" + fechaContratacion +
                ", activo=" + activo +
                ", especialidad='" + especialidad + '\'' +
                '}';
    }
}

