package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.UnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.UnidadMedida;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoUnidadMedida;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class UnidadMedidaFrm extends DefaultFrm<UnidadMedida> implements Serializable {

    @Inject
    UnidadMedidaDAO unidadMedidaDAO;

    @Inject
    TipoUnidadMedidaDAO tipoUnidadMedidaDAO;

    private List<TipoUnidadMedida> tiposUnidadMedida;
    private Integer idTipoSeleccionado;

    public UnidadMedidaFrm() {
        this.nombreBean = "Unidad de Medida";
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<UnidadMedida> getDao() {
        return unidadMedidaDAO;
    }

    @Override
    protected UnidadMedida nuevoRegistro() {
        UnidadMedida unidadMedida = new UnidadMedida();
        unidadMedida.setActivo(true);
        return unidadMedida;
    }

    @Override
    protected UnidadMedida buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer) {
            try {
                UnidadMedida resultado = unidadMedidaDAO.findById(id);
                return resultado;
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(UnidadMedida r) {
        String resultado = (r != null && r.getId() != null) ? r.getId().toString() : null;
        return resultado;
    }

    @Override
    protected UnidadMedida getIdByText(String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                Integer buscado = Integer.parseInt(id);
                UnidadMedida resultado = unidadMedidaDAO.findById(buscado);
                return resultado;
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(UnidadMedida registro) {
        // UnidadMedida no tiene campo "nombre", así que no validamos nombre
        // En su lugar validamos que tenga un tipo de unidad asignado
        return registro.getIdTipoUnidadMedida() == null;
    }

    public List<TipoUnidadMedida> getTiposUnidadMedida() {
        if (tiposUnidadMedida == null) {
            try {
                tiposUnidadMedida = tipoUnidadMedidaDAO.findAll()
                        .stream()
                        .filter(t -> t.getActivo() != null && t.getActivo())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                tiposUnidadMedida = java.util.Collections.emptyList();
            }
        }
        return tiposUnidadMedida;
    }

    public String prepararNuevo() {
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
        return null;
    }

    public String guardar() {
        try {
            if (this.registro != null) {

                if (this.idTipoSeleccionado == null) {
                    FacesContext.getCurrentInstance().addMessage("frmCrear:cbTipoUnidad",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Error de validación", "Debe seleccionar un tipo de unidad de medida"));
                    return null;
                }

                // Buscar el tipo sin validar el nombre aquí
                TipoUnidadMedida tipo = tipoUnidadMedidaDAO.findById(idTipoSeleccionado);
                if (tipo == null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Error", "Tipo de unidad no encontrado"));
                    return null;
                }

                // Asignar directamente
                this.registro.setIdTipoUnidadMedida(tipo);

                if (this.estado == ESTADO_CRUD.CREAR) {
                    unidadMedidaDAO.crear(this.registro);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Éxito", "Registro creado correctamente"));
                } else if (this.estado == ESTADO_CRUD.MODIFICAR) {
                    unidadMedidaDAO.modificar(this.registro);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Éxito", "Registro modificado correctamente"));
                }

                this.registro = null;
                this.idTipoSeleccionado = null;
                this.estado = ESTADO_CRUD.NADA;
                this.tiposUnidadMedida = null;
                inicializarRegistros();
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo guardar: " + e.getMessage()));
        }
        return null;
    }

    public String eliminar() {
        try {
            if (this.registro != null) {
                unidadMedidaDAO.eliminar(this.registro);
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String cancelar() {
        this.registro = null;
        this.idTipoSeleccionado = null;
        this.estado = ESTADO_CRUD.NADA;
        return null;
    }

    public Integer getIdTipoSeleccionado() {
        if (registro != null && registro.getIdTipoUnidadMedida() != null) {
            Integer id = registro.getIdTipoUnidadMedida().getId();
            return id;
        }
        return idTipoSeleccionado;
    }

    public void setIdTipoSeleccionado(Integer id) {
        this.idTipoSeleccionado = id;
        if (id != null && registro != null) {
            try {
                TipoUnidadMedida tipo = tipoUnidadMedidaDAO.findById(id);
                registro.setIdTipoUnidadMedida(tipo);
            } catch (Exception e) {
            }
        }
    }
}