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

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;


@Named("compraDetalleFrm")
@ViewScoped
public class CompraDetalleFrm extends DefaultFrm<CompraDetalle> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraDetalleFrm.class.getName());

    @Inject
    private CompraDetalleDAO compraDetalleDao;

    @Inject
    private CompraDAO compraDao;

    @Inject
    private ProductoDAO productoDao;
    protected Long idCompra;
    // Listas para los selectOneMenu
    private List<Compra> comprasDisponibles;
    private List<Producto> productosDisponibles;
    private final List<String> estadosDisponibles = List.of("RECIBIDO", "PENDIENTE", "DEVUELTO");


    // ---------------------- Inicialización ----------------------

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarDatosFiltros();
        this.nombreBean = "Gestión de Detalle de Compra";
        LOGGER.log(Level.INFO, "CompraDetalleFrm inicializado correctamente");
    }

    private void cargarDatosFiltros() {
        try {
            // Inicializar listas vacías para evitar NullPointerException
            this.comprasDisponibles = compraDao != null ? compraDao.findAll() : new ArrayList<>();
            this.productosDisponibles = productoDao != null ? productoDao.findAll() : new ArrayList<>();

            LOGGER.log(Level.INFO, "Cargadas {0} compras y {1} productos",
                    new Object[]{comprasDisponibles.size(), productosDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de filtros", e);
            this.comprasDisponibles = new ArrayList<>();
            this.productosDisponibles = new ArrayList<>();
        }
    }


    // ---------------------- Métodos Abstractos ----------------------

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<CompraDetalle> getDao() {
        return compraDetalleDao;
    }

    @Override
    protected CompraDetalle nuevoRegistro() {
        CompraDetalle cd = new CompraDetalle();
        cd.setId(UUID.randomUUID());
        cd.setCantidad(BigDecimal.ZERO);
        cd.setPrecio(BigDecimal.ZERO);
        cd.setEstado("PENDIENTE"); // Estado por defecto

        // Inicialización de entidades FK para evitar NullPointer en XHTML
        cd.setIdCompra(new Compra());
        cd.setIdProducto(new Producto());

        LOGGER.log(Level.INFO, "Nuevo registro creado con ID: {0}", cd.getId());
        return cd;
    }

    @Override
    protected CompraDetalle buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return compraDetalleDao.findById((UUID) id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(CompraDetalle r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected CompraDetalle getIdByText(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(id);
            return compraDetalleDao.findById(uuid);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "ID inválido: {0}", id);
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(CompraDetalle registro) {
        boolean fallo = false;

        // 1. Validar que se seleccionó Compra
        if (registro.getIdCompra() == null || registro.getIdCompra().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar una Compra."));
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

    private Compra obtenerCompraCompleta(Long id) {
        if (id == null) return null;
        try {
            return compraDao.findById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Compra con ID: " + id, e);
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
    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
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
            Long idCompraSeleccionada = this.registro.getIdCompra().getId();
            UUID idProductoSeleccionado = this.registro.getIdProducto().getId();

            Compra compraCompleta = obtenerCompraCompleta(idCompraSeleccionada);
            Producto productoCompleto = obtenerProductoCompleto(idProductoSeleccionado);

            if (compraCompleta == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró la Compra seleccionada."));
                return;
            }

            if (productoCompleto == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró el Producto seleccionado."));
                return;
            }

            this.registro.setIdCompra(compraCompleta);
            this.registro.setIdProducto(productoCompleto);

            // Persistir
            getDao().crear(this.registro);

            // Limpieza y Notificación
            LOGGER.log(Level.INFO, "Detalle de compra guardado: {0}", this.registro.getId());
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de compra guardado correctamente"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar detalle de compra", e);
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

            // Sincronizar Entidades FK (igual que en guardar)
            Long idCompraSeleccionada = this.registro.getIdCompra().getId();
            UUID idProductoSeleccionado = this.registro.getIdProducto().getId();

            Compra compraCompleta = obtenerCompraCompleta(idCompraSeleccionada);
            Producto productoCompleto = obtenerProductoCompleto(idProductoSeleccionado);

            if (compraCompleta == null || productoCompleto == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontraron las entidades relacionadas."));
                return;
            }

            this.registro.setIdCompra(compraCompleta);
            this.registro.setIdProducto(productoCompleto);

            getDao().modificar(this.registro);

            LOGGER.log(Level.INFO, "Detalle de compra modificado: {0}", this.registro.getId());
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de compra modificado correctamente"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al modificar detalle de compra", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar",
                            "Ocurrió un error: " + e.getMessage()));
        }
    }



    // ---------------------- Getters para JSF ----------------------

    public List<Compra> getComprasDisponibles() {
        if (comprasDisponibles == null) {
            cargarDatosFiltros();
        }
        return comprasDisponibles;
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