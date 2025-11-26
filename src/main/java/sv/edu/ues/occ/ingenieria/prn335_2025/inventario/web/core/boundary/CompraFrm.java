package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Proveedor;

@Named("compraFrm")
@ViewScoped
public class CompraFrm extends DefaultFrm<Compra> implements Serializable {
    @Inject
    CompraDetalleDAO compraDetalleDAO;

    @Inject
    private CompraDAO compraDao;

    @Inject
    private ProveedorDAO proveedorDao;

    @Inject
    private NotificadorKardex notificadorKardex;

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

    protected void crearEntidad(Compra entidad) throws Exception {
        // Validaciones de campos obligatorios
        if (entidad.getFecha() == null) {
            throw new Exception("La fecha es obligatoria");
        }

        if (entidad.getIdProveedor() == null) {
            throw new Exception("Debe seleccionar un proveedor");
        }

        if (entidad.getEstado() == null || entidad.getEstado().isBlank()) {
            throw new Exception("Debe seleccionar un estado");
        }

        // Verifica si el proveedor existe
        Proveedor proveedorEntity = proveedorDao.findById(entidad.getIdProveedor());
        if (proveedorEntity == null) {
            throw new Exception("El proveedor seleccionado no existe.");
        }

        // El DAO se encargará de asignar el proveedor y generar el ID
        compraDao.crear(entidad);
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
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro para guardar"));
            return;
        }

        try {
            // Validación del proveedor
            if (esNombreVacio(this.registro)) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "Debe seleccionar un proveedor"));
                return;
            }

            // Validación del estado
            if (registro.getEstado() == null || registro.getEstado().isBlank()) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "Debe seleccionar un estado"));
                return;
            }

            // Validación de la fecha
            if (registro.getFecha() == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "La fecha es obligatoria"));
                return;
            }

            // Ejecuta la operación según el estado
            if (this.estado == ESTADO_CRUD.CREAR) {
                crearEntidad(this.registro);

                // Notificar creación de compra
                notificadorKardex.notificarCambio("RELOAD_TABLE");

            } else if (this.estado == ESTADO_CRUD.MODIFICAR) {
                // Validar que el proveedor exista antes de modificar
                compraDao.validarProveedor(this.registro.getIdProveedor());

                // Cargar el proveedor completo
                Proveedor proveedor = proveedorDao.findById(this.registro.getIdProveedor());
                this.registro.setProveedor(proveedor);

                getDao().modificar(this.registro);

                // Notificar modificación de compra
                notificadorKardex.notificarCambio("RELOAD_TABLE");
            }

            // Limpiar y recargar después de guardar
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Registro guardado correctamente"));

        } catch (Exception e) {
            Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, "Error al guardar compra", e);

            // Obtener la causa raíz del error
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            String mensajeDetallado = "Error: " + e.getMessage() +
                    "\nCausa raíz: " + causa.getClass().getName() +
                    " - " + causa.getMessage();

            Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, mensajeDetallado, causa);

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar", causa.getMessage()));
        }
    }

    // ✅ NUEVO: Método para cerrar compra
    public void btnCerrarCompraHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro seleccionado"));
            return;
        }

        try {
            // Cambiar estado a PAGADA
            this.registro.setEstado("PAGADA");

            // Llamar al modificar para guardar el cambio
            getDao().modificar(this.registro);

            // Notificar cambio
            notificadorKardex.notificarCambio("RELOAD_TABLE");

            // Limpiar y recargar
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Compra cerrada correctamente"));

        } catch (Exception e) {
            Logger.getLogger(CompraFrm.class.getName()).log(Level.SEVERE, "Error al cerrar compra", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo cerrar la compra: " + e.getMessage()));
        }
    }
    public BigDecimal getTotalCompra(Compra compra) {
        if (compra != null && compra.getId() != null) {
            return compraDetalleDAO.obtenerTotalCompra(compra.getId());
        }
        return BigDecimal.ZERO;
    }
}