package com.prestamos.modelo;

/**
 * Clase que representa un Artículo en el sistema de préstamos.
 * Los artículos son bienes que los clientes pueden empeñar como garantía.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class Articulo {

    /**
     * Identificador único del artículo
     */
    private int idArticulo;

    /**
     * Identificador del cliente propietario del artículo
     */
    private int idCliente;

    /**
     * Nombre del artículo
     */
    private String nombre;

    /**
     * Tipo o categoría del artículo (JOYA, ELECTRONICO, VEHICULO, etc.)
     */
    private String tipoArticulo;

    /**
     * Estado del artículo (DISPONIBLE, EMPEÑADO, VENDIDO, etc.)
     */
    private String estado;

    /**
     * Valor de tasación del artículo
     */
    private double valorTasado;

    /**
     * Descripción detallada del artículo
     */
    private String descripcion;

    /**
     * Constructor vacío de la clase Articulo.
     */
    public Articulo() {
    }

    /**
     * Constructor con todos los parámetros de la clase Articulo.
     *
     * @param idArticulo identificador único del artículo
     * @param idCliente identificador del cliente propietario
     * @param nombre nombre del artículo
     * @param tipoArticulo tipo o categoría del artículo
     * @param estado estado actual del artículo
     * @param valorTasado valor de tasación del artículo
     * @param descripcion descripción detallada del artículo
     */
    public Articulo(int idArticulo, int idCliente, String nombre, String tipoArticulo,
                    String estado, double valorTasado, String descripcion) {
        this.idArticulo = idArticulo;
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.tipoArticulo = tipoArticulo;
        this.estado = estado;
        this.valorTasado = valorTasado;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el identificador del artículo.
     *
     * @return idArticulo identificador único del artículo
     */
    public int getIdArticulo() {
        return idArticulo;
    }

    /**
     * Establece el identificador del artículo.
     *
     * @param idArticulo identificador único del artículo
     */
    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }

    /**
     * Obtiene el identificador del cliente propietario.
     *
     * @return idCliente identificador del cliente
     */
    public int getIdCliente() {
        return idCliente;
    }

    /**
     * Establece el identificador del cliente propietario.
     *
     * @param idCliente identificador del cliente
     */
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    /**
     * Obtiene el nombre del artículo.
     *
     * @return nombre nombre del artículo
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del artículo.
     *
     * @param nombre nombre del artículo
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el tipo del artículo.
     *
     * @return tipoArticulo tipo o categoría del artículo
     */
    public String getTipoArticulo() {
        return tipoArticulo;
    }

    /**
     * Establece el tipo del artículo.
     *
     * @param tipoArticulo tipo o categoría del artículo
     */
    public void setTipoArticulo(String tipoArticulo) {
        this.tipoArticulo = tipoArticulo;
    }

    /**
     * Obtiene el estado del artículo.
     *
     * @return estado estado actual del artículo
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece el estado del artículo.
     *
     * @param estado estado actual del artículo
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el valor tasado del artículo.
     *
     * @return valorTasado valor de tasación del artículo
     */
    public double getValorTasado() {
        return valorTasado;
    }

    /**
     * Establece el valor tasado del artículo.
     *
     * @param valorTasado valor de tasación del artículo
     */
    public void setValorTasado(double valorTasado) {
        this.valorTasado = valorTasado;
    }

    /**
     * Obtiene la descripción del artículo.
     *
     * @return descripcion descripción detallada del artículo
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción del artículo.
     *
     * @param descripcion descripción detallada del artículo
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Verifica si el artículo está disponible.
     * Un artículo está disponible cuando su estado es "DISPONIBLE".
     *
     * @return true si el artículo está disponible, false en caso contrario
     */
    public boolean estaDisponible() {
        return estado != null && estado.equals("DISPONIBLE");
    }

    /**
     * Verifica si el artículo está empeñado.
     * Un artículo está empeñado cuando su estado es "EMPEÑADO".
     *
     * @return true si el artículo está empeñado, false en caso contrario
     */
    public boolean estaEmpenado() {
        return estado != null && estado.equals("EMPEÑADO");
    }

    /**
     * Retorna una representación en cadena del artículo.
     *
     * @return String con todos los atributos del artículo
     */
    @Override
    public String toString() {
        return "Articulo{" +
                "idArticulo=" + idArticulo +
                ", idCliente=" + idCliente +
                ", nombre='" + nombre + '\'' +
                ", tipoArticulo='" + tipoArticulo + '\'' +
                ", estado='" + estado + '\'' +
                ", valorTasado=" + valorTasado +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}

