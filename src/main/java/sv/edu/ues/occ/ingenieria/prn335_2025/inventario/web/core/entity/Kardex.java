package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "kardex", schema = "public")
public class Kardex {
    @Id
    @Column(name = "id_kardex", nullable = false)
    private UUID id;

    // ✅ @JsonbTransient excluye el objeto Producto completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @JsonbTransient
    private Producto idProducto;

    @Column(name = "fecha")
    private OffsetDateTime fecha;

    @Size(max = 25)
    @Column(name = "tipo_movimiento", length = 25)
    private String tipoMovimiento;

    @Column(name = "cantidad", precision = 8, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio", precision = 8, scale = 2)
    private BigDecimal precio;

    @Column(name = "cantidad_actual", precision = 8, scale = 2)
    private BigDecimal cantidadActual;

    @Column(name = "precio_actual", precision = 8, scale = 2)
    private BigDecimal precioActual;

    // ✅ @JsonbTransient excluye el objeto CompraDetalle completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra_detalle")
    @JsonbTransient
    private CompraDetalle idCompraDetalle;

    // ✅ @JsonbTransient excluye el objeto VentaDetalle completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta_detalle")
    @JsonbTransient
    private VentaDetalle idVentaDetalle;

    @Lob
    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // ✅ @JsonbTransient excluye el objeto Almacen completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen")
    @JsonbTransient
    private Almacen idAlmacen;

    // ==================== GETTERS Y SETTERS ====================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(BigDecimal cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public BigDecimal getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(BigDecimal precioActual) {
        this.precioActual = precioActual;
    }

    public CompraDetalle getIdCompraDetalle() {
        return idCompraDetalle;
    }

    public void setIdCompraDetalle(CompraDetalle idCompraDetalle) {
        this.idCompraDetalle = idCompraDetalle;
    }

    public VentaDetalle getIdVentaDetalle() {
        return idVentaDetalle;
    }

    public void setIdVentaDetalle(VentaDetalle idVentaDetalle) {
        this.idVentaDetalle = idVentaDetalle;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Almacen getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(Almacen idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    /**
     * Obtiene el ID del producto (UUID).
     * Este campo SÍ se incluirá en el JSON.
     */
    public UUID getIdProductoUUID() {
        return idProducto != null ? idProducto.getId() : null;
    }

    /**
     * Establece el producto usando solo su ID.
     */
    public void setIdProductoUUID(UUID idProductoUUID) {
        if (idProductoUUID != null) {
            this.idProducto = new Producto();
            this.idProducto.setId(idProductoUUID);
        } else {
            this.idProducto = null;
        }
    }

    /**
     * Obtiene el nombre del producto (para referencia).
     * Este campo SÍ se incluirá en el JSON.
     */
    public String getNombreProducto() {
        return idProducto != null ? idProducto.getNombreProducto() : null;
    }

    /**
     * Obtiene el ID del almacén (Integer).
     * Este campo SÍ se incluirá en el JSON.
     */
    public Integer getIdAlmacenInt() {
        return idAlmacen != null ? idAlmacen.getId() : null;
    }

    /**
     * Establece el almacén usando solo su ID.
     */
    public void setIdAlmacenInt(Integer idAlmacenInt) {
        if (idAlmacenInt != null) {
            this.idAlmacen = new Almacen();
            this.idAlmacen.setId(idAlmacenInt);
        } else {
            this.idAlmacen = null;
        }
    }

    /**
     * Obtiene las observaciones del almacén (para referencia).
     * Este campo SÍ se incluirá en el JSON.
     */
    public String getObservacionesAlmacen() {
        return idAlmacen != null ? idAlmacen.getObservaciones() : null;
    }

    /**
     * Obtiene el ID del detalle de compra (UUID).
     * Este campo SÍ se incluirá en el JSON.
     */
    public UUID getIdCompraDetalleUUID() {
        return idCompraDetalle != null ? idCompraDetalle.getId() : null;
    }

    /**
     * Establece el detalle de compra usando solo su ID.
     */
    public void setIdCompraDetalleUUID(UUID idCompraDetalleUUID) {
        if (idCompraDetalleUUID != null) {
            this.idCompraDetalle = new CompraDetalle();
            this.idCompraDetalle.setId(idCompraDetalleUUID);
        } else {
            this.idCompraDetalle = null;
        }
    }

    /**
     * Obtiene el ID del detalle de venta (UUID).
     * Este campo SÍ se incluirá en el JSON.
     */
    public UUID getIdVentaDetalleUUID() {
        return idVentaDetalle != null ? idVentaDetalle.getId() : null;
    }

    /**
     * Establece el detalle de venta usando solo su ID.
     */
    public void setIdVentaDetalleUUID(UUID idVentaDetalleUUID) {
        if (idVentaDetalleUUID != null) {
            this.idVentaDetalle = new VentaDetalle();
            this.idVentaDetalle.setId(idVentaDetalleUUID);
        } else {
            this.idVentaDetalle = null;
        }
    }

    /**
     * Calcula el valor total del movimiento (cantidad * precio).
     * Este campo SÍ se incluirá en el JSON.
     */
    public BigDecimal getValorTotal() {
        if (cantidad != null && precio != null) {
            return cantidad.multiply(precio);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el valor actual del inventario (cantidadActual * precioActual).
     * Este campo SÍ se incluirá en el JSON.
     */
    public BigDecimal getValorActual() {
        if (cantidadActual != null && precioActual != null) {
            return cantidadActual.multiply(precioActual);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Indica si es un movimiento de entrada (ENTRADA, COMPRA, AJUSTE_ENTRADA, etc.).
     * Este campo SÍ se incluirá en el JSON.
     */
    public Boolean esMovimientoEntrada() {
        if (tipoMovimiento == null) {
            return null;
        }
        String tipo = tipoMovimiento.toUpperCase();
        return tipo.contains("ENTRADA") || tipo.contains("COMPRA") || tipo.equals("INGRESO");
    }

    /**
     * Indica si es un movimiento de salida (SALIDA, VENTA, AJUSTE_SALIDA, etc.).
     * Este campo SÍ se incluirá en el JSON.
     */
    public Boolean esMovimientoSalida() {
        if (tipoMovimiento == null) {
            return null;
        }
        String tipo = tipoMovimiento.toUpperCase();
        return tipo.contains("SALIDA") || tipo.contains("VENTA") || tipo.equals("EGRESO");
    }

    // ==================== MÉTODOS UTILITARIOS ====================

    @Override
    public String toString() {
        return "Kardex{" +
                "id=" + id +
                ", idProducto=" + getIdProductoUUID() +
                ", fecha=" + fecha +
                ", tipoMovimiento='" + tipoMovimiento + '\'' +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", cantidadActual=" + cantidadActual +
                ", precioActual=" + precioActual +
                ", idAlmacen=" + getIdAlmacenInt() +
                ", idCompraDetalle=" + getIdCompraDetalleUUID() +
                ", idVentaDetalle=" + getIdVentaDetalleUUID() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kardex kardex = (Kardex) o;
        return id != null && id.equals(kardex.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}