package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class AlmacenFrm extends DefaultFrm<Almacen> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    TipoAlmacenDAO tipoAlmacenDAO;

    private List<TipoAlmacen> listaTiposAlmacen;
    private Integer idTipoSeleccionado;

    public AlmacenFrm() {
        this.nombreBean = "Almacén";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Almacen> getDao() {
        return almacenDAO;
    }

    @Override
    protected Almacen nuevoRegistro() {
        Almacen almacen = new Almacen();
        almacen.setActivo(true);
        return almacen;
    }

    @Override
    protected Almacen buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer buscado) {
            try {
                Almacen resultado = almacenDAO.findById(buscado);
                return resultado;
            } catch (Exception e) {
                System.err.println(" Error buscando almacén: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Almacen r) {
        String resultado = (r != null && r.getId() != null) ? r.getId().toString() : null;
        return resultado;
    }

    @Override
    protected Almacen getIdByText(String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                Integer buscado = Integer.parseInt(id.trim());
                Almacen resultado = almacenDAO.findById(buscado);
                return resultado;
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(Almacen registro) {
        return false;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null && this.idTipoSeleccionado == null) {
            facesContext.addMessage("frmCrear:cbTipoAlmacen",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error de validación",
                            "Debe seleccionar un tipo de almacén"));
            return;
        }

        if (this.registro != null && this.registro.getIdTipoAlmacen() == null) {
            TipoAlmacen tipo = tipoAlmacenDAO.findById(idTipoSeleccionado);
            this.registro.setIdTipoAlmacen(tipo);
        }
        super.btnGuardarHandler(actionEvent);
        this.idTipoSeleccionado = null;
        this.listaTiposAlmacen = null;
    }

    public List<TipoAlmacen> getListaTiposAlmacen() {
        if (listaTiposAlmacen == null) {
            try {
                listaTiposAlmacen = tipoAlmacenDAO.findAll()
                        .stream()
                        .filter(t -> t.getActivo() != null && t.getActivo())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                listaTiposAlmacen = java.util.Collections.emptyList();
            }
        }
        return listaTiposAlmacen;
    }

    public void setListaTiposAlmacen(List<TipoAlmacen> listaTiposAlmacen) {
        this.listaTiposAlmacen = listaTiposAlmacen;
    }

    public Integer getIdTipoSeleccionado() {
        if (registro != null && registro.getIdTipoAlmacen() != null) {
            Integer id = registro.getIdTipoAlmacen().getId();
            return id;
        }
        System.out.println(" getIdTipoSeleccionado: null");
        return idTipoSeleccionado;
    }

    public void setIdTipoSeleccionado(Integer id) {
        this.idTipoSeleccionado = id;
        if (id != null && registro != null) {
            try {
                TipoAlmacen tipo = tipoAlmacenDAO.findById(id);
                registro.setIdTipoAlmacen(tipo);
            } catch (Exception e) {
            }
        }
    }

    public String prepararNuevo() {
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
        this.idTipoSeleccionado = null;
        return null;
    }
    public String guardar() {
        if (this.idTipoSeleccionado == null) {
            facesContext.addMessage("frmCrear:cbTipoAlmacen",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Debe seleccionar un tipo de almacén"));
            return null;
        }

        if (this.registro.getIdTipoAlmacen() == null) {
            TipoAlmacen tipo = tipoAlmacenDAO.findById(idTipoSeleccionado);
            this.registro.setIdTipoAlmacen(tipo);
        }

        btnGuardarHandler(null);

        this.idTipoSeleccionado = null;
        this.listaTiposAlmacen = null;
        return null;
    }

    public String modificar() {

        if (this.idTipoSeleccionado == null) {
            facesContext.addMessage("frmCrear:cbTipoAlmacen",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Debe seleccionar un tipo de almacén"));
            return null;
        }

        if (this.registro.getIdTipoAlmacen() == null) {
            TipoAlmacen tipo = tipoAlmacenDAO.findById(idTipoSeleccionado);
            this.registro.setIdTipoAlmacen(tipo);
        }
        btnGuardarHandler(null);
        this.idTipoSeleccionado = null;
        this.listaTiposAlmacen = null;
        return null;
    }

    public String eliminar() {
        try {
            if (this.registro != null) {
                btnEliminarHandler(null);
                this.registro = null;
                this.idTipoSeleccionado = null;
                this.estado = ESTADO_CRUD.NADA;
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR ELIMINAR: " + e.getMessage());
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo eliminar el almacén"));
        }
        return null;
    }

    public String cancelar() {
        this.registro = null;
        this.idTipoSeleccionado = null;
        this.estado = ESTADO_CRUD.NADA;
        return null;
    }
}