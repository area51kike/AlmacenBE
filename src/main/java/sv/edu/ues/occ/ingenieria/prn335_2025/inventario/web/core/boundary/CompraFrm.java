package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.EstadoCompra;

@Named("compraFrm")
@ViewScoped
public class CompraFrm extends DefaultFrm<Compra> implements Serializable {

    @Inject
    private CompraDao compraDao;

    @Inject
    private ProveedorDAO proveedorDao;

    private List<Proveedor> proveedoresDisponibles;
    private List<EstadoCompra> estadosDisponibles;

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Compra> getDao() {
        return compraDao;
    }

    @Override
    protected Compra nuevoRegistro() {
        Compra nuevaCompra = new Compra();
        nuevaCompra.setFecha(OffsetDateTime.now());
        return nuevaCompra;
    }

    @Override
    protected Compra buscarRegistroPorId(Object id) {
        if (id instanceof Long) {
            try {
                return compraDao.findById((Long) id);
            } catch (Exception e) {
                Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, "Error buscando Compra por ID", e);
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Compra r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected Compra getIdByText(String id) {
        try {
            return buscarRegistroPorId(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(Compra registro) {
        return registro == null || registro.getIdProveedor() == null;
    }

    @PostConstruct
    public void init() {
        super.inicializar();
        this.estadosDisponibles = Arrays.asList(EstadoCompra.values());
        this.proveedoresDisponibles = proveedorDao.findAll();
    }

    public List<Proveedor> getProveedoresDisponibles() {
        return proveedoresDisponibles;
    }

    public List<EstadoCompra> getEstadosDisponibles() {
        return estadosDisponibles;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Atención", "Debe seleccionar un proveedor"));
                    return;
                }

                if (registro.getEstado() == null || registro.getEstado().isBlank()) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Atención", "Debe seleccionar un estado"));
                    return;
                }

                if (this.registro.getIdProveedor() != null) {
                    Proveedor proveedorEntity = proveedorDao.findById(this.registro.getIdProveedor());
                    if (proveedorEntity == null) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Error", "El proveedor seleccionado no existe."));
                        return;
                    }
                    this.registro.setProveedor(proveedorEntity);
                }

                if (this.estado == ESTADO_CRUD.CREAR) {
                    getDao().crear(this.registro);
                } else if (this.estado == ESTADO_CRUD.MODIFICAR) {
                    getDao().modificar(this.registro);
                }

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Registro guardado correctamente"));

            } catch (Exception e) {
                Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, "Error al guardar compra", e);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al guardar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro para guardar"));
        }
    }
}
