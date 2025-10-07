package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "tipo_almacen", schema = "public")
public class TipoAlmacen implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ AGREGADO
    @Column(name = "id_tipo_almacen", nullable = false)
    private Integer id;

    @Size(max = 155)
    @Column(name = "nombre", length = 155)
    private String nombre;

    @Column(name = "activo")
    private Boolean activo;

    @Lob
    @Column(name = "obsevaciones")  // ✅ CORREGIDO el nombre
    private String observaciones;
    public TipoAlmacen() {
        this.activo = true; // o false, según lo necesites
    }
    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {  // ✅ CORREGIDO
        return observaciones;
    }

    public void setObservaciones(String observaciones) {  // ✅ CORREGIDO
        this.observaciones = observaciones;
    }
}