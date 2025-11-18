package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.KardexDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Kardex;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.KardexDetalle;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named("kardexDetalleFrm")
@SessionScoped
public class KardexDetalleFrm extends DefaultFrm<KardexDetalle> implements Serializable {

    @EJB
    private KardexDetalleDAO kardexDetalleDAO;

    @EJB
    private KardexDAO kardexDAO;

    // Auxiliar para la relación con Kardex (String para evitar error de conversión)
    private String idKardexSeleccionado;
    private List<Kardex> listaKardex;

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        this.nombreBean = "kardexDetalleFrm";
        this.listaKardex = kardexDAO.findAll();
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected KardexDetalleDAO getDao() {
        return kardexDetalleDAO;
    }

    @Override
    protected KardexDetalle nuevoRegistro() {
        KardexDetalle nuevo = new KardexDetalle();
        nuevo.setId(UUID.randomUUID());
        nuevo.setActivo(true);
        this.idKardexSeleccionado=null;// valor por defecto
        return nuevo;
    }

    @Override
    protected KardexDetalle buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return kardexDetalleDAO.findById((UUID) id);
        } else if (id instanceof String) {
            try {
                UUID uuid = UUID.fromString((String) id);
                return kardexDetalleDAO.findById(uuid);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Error al convertir String a UUID: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(KardexDetalle r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected KardexDetalle getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(id);
                return buscarRegistroPorId(uuid);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Error al convertir ID string a UUID: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void selectionHandler(SelectEvent<KardexDetalle> event) {
        super.selectionHandler(event);
        if (registro != null && registro.getIdKardex() != null) {
            this.idKardexSeleccionado = registro.getIdKardex().getId().toString();
        }
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación: asegurarse que se seleccionó un Kardex
                if (idKardexSeleccionado == null || idKardexSeleccionado.isEmpty()) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                                    "Debe seleccionar un Kardex asociado"));
                    return;
                }

                // Asignar Kardex al detalle
                UUID uuid = UUID.fromString(idKardexSeleccionado);
                Kardex kardex = kardexDAO.findById(uuid);
                registro.setIdKardex(kardex);

                // Crear registro
                getDao().crear(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                "Detalle de Kardex creado correctamente"));
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        }
    }
    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación: asegurarse que se seleccionó un Kardex
                if (idKardexSeleccionado == null || idKardexSeleccionado.isEmpty()) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                                    "Debe seleccionar un Kardex asociado"));
                    return;
                }

                // Asignar Kardex al detalle
                UUID uuid = UUID.fromString(idKardexSeleccionado);
                Kardex kardex = kardexDAO.findById(uuid);
                registro.setIdKardex(kardex);

                // Modificar registro
                getDao().modificar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                "Detalle de Kardex actualizado correctamente"));
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }



    // Getters y Setters auxiliares
    public String getIdKardexSeleccionado() {
        return idKardexSeleccionado;
    }

    public void setIdKardexSeleccionado(String idKardexSeleccionado) {
        this.idKardexSeleccionado = idKardexSeleccionado;
    }

    public List<Kardex> getListaKardex() {
        return listaKardex;
    }

    public void setListaKardex(List<Kardex> listaKardex) {
        this.listaKardex = listaKardex;
    }
}
