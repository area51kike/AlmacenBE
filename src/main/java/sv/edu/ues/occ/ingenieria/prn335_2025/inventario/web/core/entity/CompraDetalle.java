package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "compra_detalle", schema = "public")
public class CompraDetalle {
    @Id
    @Column(name = "id_compra_detalle", nullable = false)
    private UUID id;

    // ✅ @JsonbTransient excluye el objeto Compra completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra")
    @JsonbTransient
    private Compra idCompra;

    // ✅ @JsonbTransient excluye el objeto Producto completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @JsonbTransient
    private Producto idProducto;

    @Column(name = "cantidad", precision = 8, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio", precision = 8, scale = 2)
    private BigDecimal precio;

    @Size(max = 10)
    @Column(name = "estado", length = 10)
    private String estado;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // ==================== GETTERS Y SETTERS ====================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Compra getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Compra idCompra) {
        this.idCompra = idCompra;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    /**
     * Obtiene el ID de la compra (Long).
     * Este campo SÍ se incluirá en el JSON.
     */
    public Long getIdCompraLong() {
        return idCompra != null ? idCompra.getId() : null;
    }

    /**
     * Establece la compra usando solo su ID.
     * Útil para recibir JSON del cliente con {"idCompraLong": 1}
     */
    public void setIdCompraLong(Long idCompraLong) {
        if (idCompraLong != null) {
            this.idCompra = new Compra();
            this.idCompra.setId(idCompraLong);
        } else {
            this.idCompra = null;
        }
    }

    /**
     * Obtiene el ID del producto (UUID).
     * Este campo SÍ se incluirá en el JSON.
     */
    public UUID getIdProductoUUID() {
        return idProducto != null ? idProducto.getId() : null;
    }

    /**
     * Establece el producto usando solo su ID.
     * Útil para recibir JSON del cliente con {"idProductoUUID": "uuid-string"}
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
     * Calcula el subtotal (cantidad * precio).
     * Este campo SÍ se incluirá en el JSON.
     */
    public BigDecimal getSubtotal() {
        if (cantidad != null && precio != null) {
            return cantidad.multiply(precio);
        }
        return BigDecimal.ZERO;
    }

    // ==================== MÉTODOS UTILITARIOS ====================

    @Override
    public String toString() {
        return "CompraDetalle{" +
                "id=" + id +
                ", idCompra=" + getIdCompraLong() +
                ", idProducto=" + getIdProductoUUID() +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", estado='" + estado + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompraDetalle that = (CompraDetalle) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}