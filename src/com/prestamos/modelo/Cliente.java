package com.prestamos.modelo;

import java.util.Date;

/**
 * Clase que representa un Cliente en el sistema de préstamos.
 * Extiende de Persona y añade atributos específicos del cliente.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class Cliente extends Persona {

    /**
     * Identificador único del cliente
     */
    private int idCliente;

    /**
     * Fecha de registro del cliente en el sistema
     */
    private Date fechaRegistro;

    /**
     * Calificación crediticia del cliente
     */
    private double calificacion;

    /**
     * Estado del cliente (S=Activo, N=Inactivo)
     */
    private char activo;

    /**
     * Constructor vacío de la clase Cliente.
     */
    public Cliente() {
        super();
    }

    /**
     * Constructor con todos los parámetros de la clase Cliente.
     *
     * @param idPersona identificador único de la persona
     * @param nombrePersona nombre completo de la persona
     * @param tipoId tipo de identificación
     * @param tipoPersona tipo de persona en el sistema
     * @param idCliente identificador único del cliente
     * @param fechaRegistro fecha de registro del cliente
     * @param calificacion calificación crediticia del cliente
     * @param activo estado del cliente (S/N)
     */
    public Cliente(int idPersona, String nombrePersona, String tipoId, String tipoPersona,
                   int idCliente, Date fechaRegistro, double calificacion, char activo) {
        super(idPersona, nombrePersona, tipoId, tipoPersona);
        this.idCliente = idCliente;
        this.fechaRegistro = fechaRegistro;
        this.calificacion = calificacion;
        this.activo = activo;
    }

    /**
     * Obtiene el identificador del cliente.
     *
     * @return idCliente identificador único del cliente
     */
    public int getIdCliente() {
        return idCliente;
    }

    /**
     * Establece el identificador del cliente.
     *
     * @param idCliente identificador único del cliente
     */
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    /**
     * Obtiene la fecha de registro del cliente.
     *
     * @return fechaRegistro fecha de registro del cliente
     */
    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * Establece la fecha de registro del cliente.
     *
     * @param fechaRegistro fecha de registro del cliente
     */
    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Obtiene la calificación crediticia del cliente.
     *
     * @return calificacion calificación crediticia del cliente
     */
    public double getCalificacion() {
        return calificacion;
    }

    /**
     * Establece la calificación crediticia del cliente.
     *
     * @param calificacion calificación crediticia del cliente
     */
    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    /**
     * Obtiene el estado del cliente.
     *
     * @return activo estado del cliente (S/N)
     */
    public char getActivo() {
        return activo;
    }

    /**
     * Establece el estado del cliente.
     *
     * @param activo estado del cliente (S/N)
     */
    public void setActivo(char activo) {
        this.activo = activo;
    }

    /**
     * Verifica si el cliente está activo en el sistema.
     *
     * @return true si el cliente está activo (activo == 'S'), false en caso contrario
     */
    public boolean esActivo() {
        return activo == 'S';
    }

    /**
     * Retorna una representación en cadena del cliente.
     * Incluye los atributos de Persona y los propios del Cliente.
     *
     * @return String con todos los atributos del cliente
     */
    @Override
    public String toString() {
        return "Cliente{" +
                super.toString() +
                ", idCliente=" + idCliente +
                ", fechaRegistro=" + fechaRegistro +
                ", calificacion=" + calificacion +
                ", activo=" + activo +
                '}';
    }
}

