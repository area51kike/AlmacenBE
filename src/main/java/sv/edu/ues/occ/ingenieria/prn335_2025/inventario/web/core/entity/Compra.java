package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.converter.IntegerToLongConverter; // <-- Importación necesaria

@Entity
@Access(AccessType.FIELD)
@Table(name = "compra", schema = "public")
public class Compra implements Serializable {

    @Id
    @Column(name = "id_compra", nullable = false)
    private Long id;


    // 2. CLAVE FORÁNEA PRIMITIVA (Integer)
    // El ID del proveedor en la DB es INTEGER. Lo mapeamos como Integer.
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    // 3. RELACIÓN DE ENTIDAD
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor", insertable = false, updatable = false)

    private Proveedor proveedor;


    // 4. OTROS CAMPOS DE COMPRA
    @Convert(converter = sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.converter.OffsetDateTimeJpaConverter.class)
    @Column(name = "fecha")
    private OffsetDateTime fecha;

    @Size(max = 10)
    @Column(name = "estado", length = 10)
    private String estado;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // --- CONSTRUCTORES, GETTERS Y SETTERS ---

    // ... (El resto de getters, setters y constructores) ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
        // Al establecer la entidad, actualizamos el ID primitivo para consistencia.
        this.idProveedor = (proveedor != null) ? proveedor.getId() : null;
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

}