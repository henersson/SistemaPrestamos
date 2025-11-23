package com.prestamos.modelo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Clase que representa un Préstamo en el sistema de préstamos.
 * Contiene toda la información relacionada con un préstamo otorgado a un cliente.
 *
 * Según el universo del discurso: "Cada préstamo se asocia a un cliente,
 * a un artículo y al asesor"
 *
 * @author Henersson Cobo
 * @version 2.0
 * @since 2025-11-23
 */
public class Prestamo {

    /**
     * Identificador único del préstamo
     */
    private int idPrestamo;

    /**
     * Identificador del asesor que gestiona el préstamo
     */
    private int idAsesor;

    /**
     * Identificador del cliente que recibe el préstamo
     */
    private int idCliente;

    /**
     * Identificador del artículo asociado como garantía del préstamo
     */
    private int idArticulo;

    /**
     * Estado actual del préstamo (ACTIVO, PAGADO, VENCIDO, etc.)
     */
    private String estadoPrestamo;

    /**
     * Monto principal del préstamo
     */
    private double monto;

    /**
     * Interés generado por el préstamo
     */
    private double interesGenerado;

    /**
     * Fecha en que se otorgó el préstamo
     */
    private Date fechaPrestamo;

    /**
     * Fecha límite para el pago del préstamo
     */
    private Date fechaVencimiento;

    /**
     * Tasa de interés aplicada al préstamo
     */
    private double tasaInteres;

    /**
     * Multa aplicada al préstamo por mora o incumplimiento
     */
    private double multa;

    /**
     * Constructor vacío de la clase Prestamo.
     */
    public Prestamo() {
    }

    /**
     * Constructor con todos los parámetros de la clase Prestamo.
     *
     * @param idPrestamo identificador único del préstamo
     * @param idAsesor identificador del asesor que gestiona el préstamo
     * @param idCliente identificador del cliente que recibe el préstamo
     * @param idArticulo identificador del artículo asociado como garantía
     * @param estadoPrestamo estado actual del préstamo
     * @param monto monto principal del préstamo
     * @param interesGenerado interés generado por el préstamo
     * @param fechaPrestamo fecha en que se otorgó el préstamo
     * @param fechaVencimiento fecha límite para el pago
     * @param tasaInteres tasa de interés aplicada
     */
    public Prestamo(int idPrestamo, int idAsesor, int idCliente, int idArticulo,
                    String estadoPrestamo, double monto, double interesGenerado,
                    Date fechaPrestamo, Date fechaVencimiento, double tasaInteres) {
        this.idPrestamo = idPrestamo;
        this.idAsesor = idAsesor;
        this.idCliente = idCliente;
        this.idArticulo = idArticulo;
        this.estadoPrestamo = estadoPrestamo;
        this.monto = monto;
        this.interesGenerado = interesGenerado;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.tasaInteres = tasaInteres;
        this.multa = 0.0;
    }

    /**
     * Obtiene el identificador del préstamo.
     *
     * @return idPrestamo identificador único del préstamo
     */
    public int getIdPrestamo() {
        return idPrestamo;
    }

    /**
     * Establece el identificador del préstamo.
     *
     * @param idPrestamo identificador único del préstamo
     */
    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    /**
     * Obtiene el identificador del asesor.
     *
     * @return idAsesor identificador del asesor
     */
    public int getIdAsesor() {
        return idAsesor;
    }

    /**
     * Establece el identificador del asesor.
     *
     * @param idAsesor identificador del asesor
     */
    public void setIdAsesor(int idAsesor) {
        this.idAsesor = idAsesor;
    }

    /**
     * Obtiene el identificador del cliente.
     *
     * @return idCliente identificador del cliente
     */
    public int getIdCliente() {
        return idCliente;
    }

    /**
     * Establece el identificador del cliente.
     *
     * @param idCliente identificador del cliente
     */
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    /**
     * Obtiene el identificador del artículo asociado como garantía.
     *
     * @return idArticulo identificador del artículo
     */
    public int getIdArticulo() {
        return idArticulo;
    }

    /**
     * Establece el identificador del artículo asociado como garantía.
     * El artículo no puede estar en estado defectuoso.
     *
     * @param idArticulo identificador del artículo
     */
    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }

    /**
     * Obtiene el estado del préstamo.
     *
     * @return estadoPrestamo estado actual del préstamo
     */
    public String getEstadoPrestamo() {
        return estadoPrestamo;
    }

    /**
     * Establece el estado del préstamo.
     *
     * @param estadoPrestamo estado actual del préstamo
     */
    public void setEstadoPrestamo(String estadoPrestamo) {
        this.estadoPrestamo = estadoPrestamo;
    }

    /**
     * Obtiene el monto del préstamo.
     *
     * @return monto monto principal del préstamo
     */
    public double getMonto() {
        return monto;
    }

    /**
     * Establece el monto del préstamo.
     *
     * @param monto monto principal del préstamo
     */
    public void setMonto(double monto) {
        this.monto = monto;
    }

    /**
     * Obtiene el interés generado.
     *
     * @return interesGenerado interés generado por el préstamo
     */
    public double getInteresGenerado() {
        return interesGenerado;
    }

    /**
     * Establece el interés generado.
     *
     * @param interesGenerado interés generado por el préstamo
     */
    public void setInteresGenerado(double interesGenerado) {
        this.interesGenerado = interesGenerado;
    }

    /**
     * Obtiene la fecha del préstamo.
     *
     * @return fechaPrestamo fecha en que se otorgó el préstamo
     */
    public Date getFechaPrestamo() {
        return fechaPrestamo;
    }

    /**
     * Establece la fecha del préstamo.
     *
     * @param fechaPrestamo fecha en que se otorgó el préstamo
     */
    public void setFechaPrestamo(Date fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    /**
     * Obtiene la fecha de vencimiento.
     *
     * @return fechaVencimiento fecha límite para el pago
     */
    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    /**
     * Establece la fecha de vencimiento.
     *
     * @param fechaVencimiento fecha límite para el pago
     */
    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * Obtiene la tasa de interés.
     *
     * @return tasaInteres tasa de interés aplicada
     */
    public double getTasaInteres() {
        return tasaInteres;
    }

    /**
     * Establece la tasa de interés.
     *
     * @param tasaInteres tasa de interés aplicada
     */
    public void setTasaInteres(double tasaInteres) {
        this.tasaInteres = tasaInteres;
    }

    /**
     * Obtiene la multa aplicada al préstamo.
     *
     * @return multa multa aplicada por mora o incumplimiento
     */
    public double getMulta() {
        return multa;
    }

    /**
     * Establece la multa aplicada al préstamo.
     *
     * @param multa multa aplicada por mora o incumplimiento
     */
    public void setMulta(double multa) {
        this.multa = multa;
    }

    /**
     * Calcula el total a pagar del préstamo.
     * El total incluye el monto principal más el interés generado.
     *
     * @return double total a pagar (monto + interesGenerado)
     */
    public double calcularTotalAPagar() {
        return monto + interesGenerado + multa;
    }

    /**
     * Verifica si el préstamo está vencido.
     * Compara la fecha de vencimiento con la fecha actual.
     *
     * @return true si el préstamo está vencido, false en caso contrario
     */
    public boolean estaVencido() {
        if (fechaVencimiento == null) {
            return false;
        }
        Date fechaActual = new Date();
        return fechaActual.after(fechaVencimiento);
    }

    /**
     * Calcula los días transcurridos desde el vencimiento del préstamo.
     * Si el préstamo no está vencido, retorna 0.
     *
     * @return long número de días vencidos (0 si no está vencido)
     */
    public long getDiasVencido() {
        if (!estaVencido() || fechaVencimiento == null) {
            return 0;
        }

        Date fechaActual = new Date();
        long diferenciaMilisegundos = fechaActual.getTime() - fechaVencimiento.getTime();
        return TimeUnit.MILLISECONDS.toDays(diferenciaMilisegundos);
    }

    /**
     * Retorna una representación en cadena del préstamo.
     *
     * @return String con todos los atributos del préstamo
     */
    @Override
    public String toString() {
        return "Prestamo{" +
                "idPrestamo=" + idPrestamo +
                ", idAsesor=" + idAsesor +
                ", idCliente=" + idCliente +
                ", idArticulo=" + idArticulo +
                ", estadoPrestamo='" + estadoPrestamo + '\'' +
                ", monto=" + monto +
                ", interesGenerado=" + interesGenerado +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaVencimiento=" + fechaVencimiento +
                ", tasaInteres=" + tasaInteres +
                ", multa=" + multa +
                '}';
    }
}
