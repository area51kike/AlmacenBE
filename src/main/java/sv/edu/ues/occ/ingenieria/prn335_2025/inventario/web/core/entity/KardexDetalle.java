package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "kardex_detalle", schema = "public")
public class KardexDetalle {
    @Id
    @Column(name = "id_kardex_detalle", nullable = false)
    private UUID id;

    // ✅ @JsonbTransient excluye el objeto Kardex completo del JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kardex")
    @JsonbTransient
    private Kardex idKardex;

    @Lob
    @Column(name = "lote")
    private String lote;

    @Column(name = "activo")
    private Boolean activo;

    // ==================== GETTERS Y SETTERS ====================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Kardex getIdKardex() {
        return idKardex;
    }

    public void setIdKardex(Kardex idKardex) {
        this.idKardex = idKardex;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    /**
     * Obtiene el ID del kardex (UUID).
     * Este campo SÍ se incluirá en el JSON.
     */
    public UUID getIdKardexUUID() {
        return idKardex != null ? idKardex.getId() : null;
    }

    /**
     * Establece el kardex usando solo su ID.
     * Útil para recibir JSON del cliente con {"idKardexUUID": "uuid-string"}
     */
    public void setIdKardexUUID(UUID idKardexUUID) {
        if (idKardexUUID != null) {
            this.idKardex = new Kardex();
            this.idKardex.setId(idKardexUUID);
        } else {
            this.idKardex = null;
        }
    }

    /**
     * Obtiene el tipo de movimiento del kardex padre (para referencia).
     * Este campo SÍ se incluirá en el JSON.
     */
    public String getTipoMovimientoKardex() {
        return idKardex != null ? idKardex.getTipoMovimiento() : null;
    }

    /**
     * Obtiene la fecha del movimiento del kardex padre (para referencia).
     * Este campo SÍ se incluirá en el JSON.
     */
    public java.time.OffsetDateTime getFechaKardex() {
        return idKardex != null ? idKardex.getFecha() : null;
    }

    /**
     * Obtiene el ID del producto del kardex padre (para referencia).
     * Este campo SÍ se incluirá en el JSON.
     */
    public UUID getIdProductoKardex() {
        return idKardex != null ? idKardex.getIdProductoUUID() : null;
    }

    /**
     * Obtiene el nombre del producto del kardex padre (para referencia).
     * Este campo SÍ se incluirá en el JSON.
     */
    public String getNombreProductoKardex() {
        return idKardex != null ? idKardex.getNombreProducto() : null;
    }

    /**
     * Indica si el detalle está activo.
     * Este método facilita las validaciones.
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    // ==================== MÉTODOS UTILITARIOS ====================

    @Override
    public String toString() {
        return "KardexDetalle{" +
                "id=" + id +
                ", idKardex=" + getIdKardexUUID() +
                ", lote='" + lote + '\'' +
                ", activo=" + activo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KardexDetalle that = (KardexDetalle) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}