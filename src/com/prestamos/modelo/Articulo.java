package com.prestamos.modelo;

import java.util.Date;

/**
 * Clase que representa un Artículo en el sistema de préstamos.
 * Los artículos son bienes que los clientes pueden empeñar como garantía.
 *
 * Según el universo del discurso: "valor tasado, fecha de avalúo" y
 * "valor se calcula según precio de mercado vigente"
 *
 * @author Henersson Cobo
 * @version 2.0
 * @since 2025-11-23
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
     * Estado del artículo (OPTIMO, BUENO, REGULAR, DEFECTUOSO, PROPIEDAD_CASA)
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
     * Fecha en que se realizó el avalúo del artículo
     */
    private Date fechaAvaluo;

    /**
     * Precio de mercado base del artículo al momento del avalúo
     */
    private double precioMercadoBase;

    /**
     * Porcentaje del precio de mercado aplicado para calcular el valor tasado
     */
    private double porcentajeTasacion;

    /**
     * Nombre del cliente propietario (campo auxiliar para mostrar en UI)
     */
    private String nombreCliente;

    /**
     * Constructor vacío de la clase Articulo.
     */
    public Articulo() {
    }

    /**
     * Constructor con parámetros básicos de la clase Articulo.
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
        this.fechaAvaluo = new Date(); // Fecha actual por defecto
        this.porcentajeTasacion = 70.0; // 70% por defecto
    }

    /**
     * Constructor completo con todos los parámetros incluyendo avalúo.
     *
     * @param idArticulo identificador único del artículo
     * @param idCliente identificador del cliente propietario
     * @param nombre nombre del artículo
     * @param tipoArticulo tipo o categoría del artículo
     * @param estado estado actual del artículo
     * @param valorTasado valor de tasación del artículo
     * @param descripcion descripción detallada del artículo
     * @param fechaAvaluo fecha del avalúo
     * @param precioMercadoBase precio de mercado base
     * @param porcentajeTasacion porcentaje aplicado para tasación
     */
    public Articulo(int idArticulo, int idCliente, String nombre, String tipoArticulo,
                    String estado, double valorTasado, String descripcion,
                    Date fechaAvaluo, double precioMercadoBase, double porcentajeTasacion) {
        this.idArticulo = idArticulo;
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.tipoArticulo = tipoArticulo;
        this.estado = estado;
        this.valorTasado = valorTasado;
        this.descripcion = descripcion;
        this.fechaAvaluo = fechaAvaluo;
        this.precioMercadoBase = precioMercadoBase;
        this.porcentajeTasacion = porcentajeTasacion;
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
     * Obtiene la fecha del avalúo del artículo.
     *
     * @return fechaAvaluo fecha del avalúo
     */
    public Date getFechaAvaluo() {
        return fechaAvaluo;
    }

    /**
     * Establece la fecha del avalúo del artículo.
     *
     * @param fechaAvaluo fecha del avalúo
     */
    public void setFechaAvaluo(Date fechaAvaluo) {
        this.fechaAvaluo = fechaAvaluo;
    }

    /**
     * Obtiene el precio de mercado base del artículo.
     *
     * @return precioMercadoBase precio de mercado base
     */
    public double getPrecioMercadoBase() {
        return precioMercadoBase;
    }

    /**
     * Establece el precio de mercado base del artículo.
     * Al establecer el precio de mercado, se puede recalcular el valor tasado.
     *
     * @param precioMercadoBase precio de mercado base
     */
    public void setPrecioMercadoBase(double precioMercadoBase) {
        this.precioMercadoBase = precioMercadoBase;
    }

    /**
     * Obtiene el porcentaje de tasación aplicado.
     *
     * @return porcentajeTasacion porcentaje aplicado (ej: 70.0 para 70%)
     */
    public double getPorcentajeTasacion() {
        return porcentajeTasacion;
    }

    /**
     * Establece el porcentaje de tasación.
     *
     * @param porcentajeTasacion porcentaje aplicado (ej: 70.0 para 70%)
     */
    public void setPorcentajeTasacion(double porcentajeTasacion) {
        this.porcentajeTasacion = porcentajeTasacion;
    }

    /**
     * Obtiene el nombre del cliente propietario.
     *
     * @return nombreCliente nombre del cliente propietario
     */
    public String getNombreCliente() {
        return nombreCliente;
    }

    /**
     * Establece el nombre del cliente propietario.
     *
     * @param nombreCliente nombre del cliente propietario
     */
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    /**
     * Calcula el valor tasado basado en el precio de mercado y porcentaje de tasación.
     * Fórmula: VALOR_TASADO = PRECIO_MERCADO_BASE * (PORCENTAJE / 100)
     *
     * @return double valor tasado calculado
     */
    public double calcularValorTasado() {
        if (precioMercadoBase > 0 && porcentajeTasacion > 0) {
            return precioMercadoBase * (porcentajeTasacion / 100.0);
        }
        return valorTasado;
    }

    /**
     * Actualiza el valor tasado basándose en el precio de mercado y estado actual.
     * Aplica porcentajes según el estado del artículo:
     * - OPTIMO: 80%
     * - BUENO: 70%
     * - REGULAR: 60%
     * - DEFECTUOSO: 40%
     */
    public void actualizarValorTasadoPorEstado() {
        if (precioMercadoBase > 0) {
            switch (estado) {
                case "OPTIMO":
                    porcentajeTasacion = 80.0;
                    break;
                case "BUENO":
                    porcentajeTasacion = 70.0;
                    break;
                case "REGULAR":
                    porcentajeTasacion = 60.0;
                    break;
                case "DEFECTUOSO":
                    porcentajeTasacion = 40.0;
                    break;
                case "PROPIEDAD_CASA":
                    porcentajeTasacion = 70.0;
                    break;
                default:
                    porcentajeTasacion = 70.0;
            }
            valorTasado = calcularValorTasado();
        }
    }

    /**
     * Verifica si el avalúo está vigente (menos de 180 días).
     *
     * @return true si el avalúo está vigente, false en caso contrario
     */
    public boolean avaluoVigente() {
        if (fechaAvaluo == null) {
            return false;
        }
        long diasDesdeAvaluo = (new Date().getTime() - fechaAvaluo.getTime()) / (1000 * 60 * 60 * 24);
        return diasDesdeAvaluo <= 180;
    }

    /**
     * Calcula el margen de seguridad (diferencia entre precio de mercado y valor tasado).
     *
     * @return double margen de seguridad
     */
    public double calcularMargenSeguridad() {
        return precioMercadoBase - valorTasado;
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
                ", fechaAvaluo=" + fechaAvaluo +
                ", precioMercadoBase=" + precioMercadoBase +
                ", porcentajeTasacion=" + porcentajeTasacion +
                '}';
    }
}
