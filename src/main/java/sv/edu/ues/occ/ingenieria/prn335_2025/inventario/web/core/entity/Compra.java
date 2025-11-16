package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "compra")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra", nullable = false)
    private Long id;

    // Relación ManyToOne con Proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "fecha")
    private OffsetDateTime fecha;

    @Size(max = 10)
    @Column(name = "estado", length = 10)
    private String estado;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    // Métodos de conveniencia para trabajar con idProveedor
    public Integer getIdProveedor() {
        return proveedor != null ? proveedor.getId() : null;
    }

    public void setIdProveedor(Integer idProveedor) {
        if (idProveedor != null) {
            // Crear una instancia de Proveedor solo con el ID
            // JPA la tratará como una referencia (proxy)
            this.proveedor = new Proveedor();
            this.proveedor.setId(idProveedor);
        } else {
            this.proveedor = null;
        }
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
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

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", idProveedor=" + getIdProveedor() +
                ", fecha=" + fecha +
                ", estado='" + estado + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}