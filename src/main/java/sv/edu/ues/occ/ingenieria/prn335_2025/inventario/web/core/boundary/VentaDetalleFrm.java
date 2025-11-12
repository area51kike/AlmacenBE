package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;


@Named("ventaDetalleFrm")
@ViewScoped
public class VentaDetalleFrm extends DefaultFrm<VentaDetalle> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(VentaDetalleFrm.class.getName());

    @Inject
    private VentaDetalleDao ventaDetalleDao;

    @Inject
    private VentaDao ventaDao;

    @Inject
    private ProductoDAO productoDao;

    // Listas para los selectOneMenu
    private List<Venta> ventasDisponibles;
    private List<Producto> productosDisponibles;
    private final List<String> estadosDisponibles = List.of("ENTREGADO", "PENDIENTE", "ANULADO");


    // ---------------------- Inicialización ----------------------

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarDatosFiltros();
        this.nombreBean = "Gestión de Detalle de Venta";
        LOGGER.log(Level.INFO, "VentaDetalleFrm inicializado correctamente");
    }

    private void cargarDatosFiltros() {
        try {
            this.ventasDisponibles = ventaDao != null ? ventaDao.findAll() : new ArrayList<>();
            this.productosDisponibles = productoDao != null ? productoDao.findAll() : new ArrayList<>();

            LOGGER.log(Level.INFO, "Cargadas {0} ventas y {1} productos",
                    new Object[]{ventasDisponibles.size(), productosDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de filtros", e);
            this.ventasDisponibles = new ArrayList<>();
            this.productosDisponibles = new ArrayList<>();
        }
    }


    // ---------------------- Métodos Abstractos ----------------------

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<VentaDetalle> getDao() {
        return ventaDetalleDao;
    }

    @Override
    protected VentaDetalle nuevoRegistro() {
        VentaDetalle vd = new VentaDetalle();
        vd.setId(UUID.randomUUID());
        vd.setCantidad(BigDecimal.ZERO);
        vd.setPrecio(BigDecimal.ZERO);
        vd.setEstado("PENDIENTE"); // Estado por defecto

        // Inicialización de entidades FK para evitar NullPointer en XHTML
        vd.setIdVenta(new Venta());
        vd.setIdProducto(new Producto());

        LOGGER.log(Level.INFO, "Nuevo registro creado con ID: {0}", vd.getId());
        return vd;
    }

    @Override
    protected VentaDetalle buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return ventaDetalleDao.findById((UUID) id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(VentaDetalle r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected VentaDetalle getIdByText(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(id);
            return ventaDetalleDao.findById(uuid);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "ID inválido: {0}", id);
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(VentaDetalle registro) {
        boolean fallo = false;

        // 1. Validar que se seleccionó Venta
        if (registro.getIdVenta() == null || registro.getIdVenta().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar una Venta."));
            fallo = true;
        }

        // 2. Validar que se seleccionó Producto
        if (registro.getIdProducto() == null || registro.getIdProducto().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Producto."));
            fallo = true;
        }

        // 3. Validar Cantidad
        if (registro.getCantidad() == null || registro.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La cantidad debe ser mayor a cero."));
            fallo = true;
        }

        // 4. Validar Precio
        if (registro.getPrecio() == null || registro.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe ingresar un precio válido."));
            fallo = true;
        }

        // 5. Validar Estado
        if (registro.getEstado() == null || registro.getEstado().trim().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un estado."));
            fallo = true;
        }

        return fallo;
    }

    // ---------------------- Manejo de Guardado y Sincronización de FK ----------------------

    private Venta obtenerVentaCompleta(UUID id) {
        if (id == null) return null;
        try {
            return ventaDao.findById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Venta con ID: " + id, e);
            return null;
        }
    }

    private Producto obtenerProductoCompleto(UUID id) {
        if (id == null) return null;
        try {
            return productoDao.findById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Producto con ID: " + id, e);
            return null;
        }
    }

    @Override
    public void btnGuardarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar"));
            return;
        }

        try {
            // Validar campos
            if (esNombreVacio(this.registro)) {
                return;
            }

            // Sincronizar Entidades FK
            UUID idVentaSeleccionada = this.registro.getIdVenta().getId();
            UUID idProductoSeleccionado = this.registro.getIdProducto().getId();

            Venta ventaCompleta = obtenerVentaCompleta(idVentaSeleccionada);
            Producto productoCompleto = obtenerProductoCompleto(idProductoSeleccionado);

            if (ventaCompleta == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró la Venta seleccionada."));
                return;
            }

            if (productoCompleto == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró el Producto seleccionado."));
                return;
            }

            this.registro.setIdVenta(ventaCompleta);
            this.registro.setIdProducto(productoCompleto);

            // Persistir
            getDao().crear(this.registro);

            // Limpieza y Notificación
            LOGGER.log(Level.INFO, "Detalle de venta guardado: {0}", this.registro.getId());
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de venta guardado correctamente"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar detalle de venta", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar",
                            "Ocurrió un error de persistencia: " + e.getMessage()));
        }
    }

    @Override
    public void btnModificarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para modificar"));
            return;
        }

        try {
            if (esNombreVacio(this.registro)) {
                return;
            }

            // Sincronizar Entidades FK
            UUID idVentaSeleccionada = this.registro.getIdVenta().getId();
            UUID idProductoSeleccionado = this.registro.getIdProducto().getId();

            Venta ventaCompleta = obtenerVentaCompleta(idVentaSeleccionada);
            Producto productoCompleto = obtenerProductoCompleto(idProductoSeleccionado);

            if (ventaCompleta == null || productoCompleto == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontraron las entidades relacionadas."));
                return;
            }

            this.registro.setIdVenta(ventaCompleta);
            this.registro.setIdProducto(productoCompleto);

            getDao().modificar(this.registro);

            LOGGER.log(Level.INFO, "Detalle de venta modificado: {0}", this.registro.getId());
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de venta modificado correctamente"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al modificar detalle de venta", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar",
                            "Ocurrió un error: " + e.getMessage()));
        }
    }


    // ---------------------- Getters para JSF ----------------------

    public List<Venta> getVentasDisponibles() {
        if (ventasDisponibles == null) {
            cargarDatosFiltros();
        }
        return ventasDisponibles;
    }

    public List<Producto> getProductosDisponibles() {
        if (productosDisponibles == null) {
            cargarDatosFiltros();
        }
        return productosDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }
}