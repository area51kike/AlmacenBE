package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Importaciones de Control/DAO (Asume que existen)
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDetalleDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.VentaDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

// Importaciones de Entidad
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.VentaDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;


@Named("ventaDetalleFrm")
@ViewScoped
public class VentaDetalleFrm extends DefaultFrm<VentaDetalle> implements Serializable {

    @Inject
    private VentaDetalleDao ventaDetalleDao;

    @Inject
    private VentaDao ventaDao; // Para cargar la lista de Ventas

    @Inject
    private ProductoDAO productoDao; // Para cargar la lista de Productos

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
    }

    private void cargarDatosFiltros() {
        this.ventasDisponibles = ventaDao.findAll();
        this.productosDisponibles = productoDao.findAll();
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
        vd.setCantidad(BigDecimal.ZERO.setScale(2));
        vd.setPrecio(BigDecimal.ZERO.setScale(2));

        // Inicialización de entidades FK (CRÍTICO para evitar NullPointer en XHTML)
        vd.setIdVenta(new Venta());
        vd.setIdProducto(new Producto());

        return vd;
    }

    @Override
    protected VentaDetalle buscarRegistroPorId(Object id) {
        return null; // Manejado por LazyDataModel
    }

    @Override
    protected String getIdAsText(VentaDetalle r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected VentaDetalle getIdByText(String id) {
        if (id == null || id.isEmpty()) { return null; }
        try {
            UUID uuid = UUID.fromString(id);
            return null; // En un caso real, implementarías la búsqueda por UUID aquí.
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(VentaDetalle registro) {
        boolean fallo = false;

        if (registro.getIdVenta() == null || registro.getIdVenta().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar una Venta."));
            fallo = true;
        }

        if (registro.getIdProducto() == null || registro.getIdProducto().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Producto."));
            fallo = true;
        }

        if (registro.getCantidad() == null || registro.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La cantidad debe ser mayor a cero."));
            fallo = true;
        }

        if (registro.getPrecio() == null || registro.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe ingresar un precio válido."));
            fallo = true;
        }

        return fallo;
    }

    // ---------------------- Manejo de Guardado y Sincronización de FK ----------------------

    private Venta obtenerVentaCompleta(UUID id) {
        // Asume que VentaDao tiene findById(UUID id)
        return ventaDao.findById(id);
    }

    private Producto obtenerProductoCompleto(UUID id) {
        // Asume que ProductoDao tiene findById(UUID id)
        return productoDao.findById(id);
    }

    @Override
    public void btnGuardarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    return;
                }

                // 🛑 1. Sincronizar Entidades FK:
                UUID idVentaSeleccionada = this.registro.getIdVenta().getId();
                UUID idProductoSeleccionado = this.registro.getIdProducto().getId();

                this.registro.setIdVenta(obtenerVentaCompleta(idVentaSeleccionada));
                this.registro.setIdProducto(obtenerProductoCompleto(idProductoSeleccionado));


                // 2. Persistir
                getDao().crear(this.registro);

                // 3. Limpieza y Notificación
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de venta guardado correctamente"));
            } catch (Exception e) {
                e.printStackTrace();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", "Ocurrió un error de persistencia."));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar"));
        }
    }


    // ---------------------- Getters para JSF ----------------------

    public List<Venta> getVentasDisponibles() {
        return ventasDisponibles;
    }

    public List<Producto> getProductosDisponibles() {
        return productosDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }
}