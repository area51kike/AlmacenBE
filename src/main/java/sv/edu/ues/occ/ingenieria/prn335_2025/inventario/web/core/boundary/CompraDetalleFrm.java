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
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDetalleDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.CompraDao; // Necesario para la lista de compras
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO; // Necesario para la lista de productos
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.InventarioDefaultDataAccess;

// Importaciones de Entidad
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;


@Named("compraDetalleFrm")
@ViewScoped
public class CompraDetalleFrm extends DefaultFrm<CompraDetalle> implements Serializable {

    @Inject
    private CompraDetalleDao compraDetalleDao;

    @Inject
    private CompraDao compraDao; // Para obtener la entidad Compra y la lista de Compras

    @Inject
    private ProductoDAO productoDao; // Para obtener la entidad Producto y la lista de Productos

    // Listas para los selectOneMenu
    private List<Compra> comprasDisponibles;
    private List<Producto> productosDisponibles;
    private final List<String> estadosDisponibles = List.of("RECIBIDO", "PENDIENTE", "DEVUELTO");


    // ---------------------- Inicialización ----------------------

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarDatosFiltros(); // Carga las listas de FK
        this.nombreBean = "Gestión de Detalle de Compra";
    }

    private void cargarDatosFiltros() {
        // Asume que CompraDao.findAll() devuelve List<Compra>
        this.comprasDisponibles = compraDao.findAll();

        // Asume que ProductoDao.findAll() devuelve List<Producto>
        this.productosDisponibles = productoDao.findAll();
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

        // 🛑 Inicialización de entidades FK (CRÍTICO para evitar NullPointer en XHTML)
        cd.setIdCompra(new Compra());
        cd.setIdProducto(new Producto());

        return cd;
    }

    @Override
    protected CompraDetalle buscarRegistroPorId(Object id) {
        return null; // Manejado por LazyDataModel
    }

    @Override
    protected String getIdAsText(CompraDetalle r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected CompraDetalle getIdByText(String id) {
        if (id == null || id.isEmpty()) { return null; }
        try {
            UUID uuid = UUID.fromString(id);
            return null; // En un caso real, implementarías la búsqueda por UUID aquí.
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(CompraDetalle registro) {
        boolean fallo = false;

        // 1. Validar que se seleccionó Compra (ID Long)
        if (registro.getIdCompra() == null || registro.getIdCompra().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar una Compra."));
            fallo = true;
        }

        // 2. Validar que se seleccionó Producto (ID UUID)
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

        // 4. Validar Precio (podría ser cero si es un item de cortesía, pero lo validamos como requerido)
        if (registro.getPrecio() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe ingresar un precio."));
            fallo = true;
        }

        return fallo;
    }

    // ---------------------- Manejo de Guardado y Sincronización de FK ----------------------

    /**
     * Busca la entidad Compra completa (incluyendo sus campos LAZY) a partir de su ID.
     * @param id El ID Long de la Compra.
     * @return La entidad Compra. Asume que CompraDao tiene findById(Long id).
     */
    private Compra obtenerCompraCompleta(Long id) {
        // Necesitas que CompraDao implemente: public Compra findById(Long id)
        return compraDao.findById(id);
    }

    /**
     * Busca la entidad Producto completa a partir de su ID.
     * @param id El ID UUID del Producto.
     * @return La entidad Producto. Asume que ProductoDao tiene findById(UUID id).
     */
    private Producto obtenerProductoCompleto(UUID id) {
        // Necesitas que ProductoDao implemente: public Producto findById(UUID id)
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
                // Obtiene los IDs que JSF estableció en las entidades vacías:
                Long idCompraSeleccionada = this.registro.getIdCompra().getId();
                UUID idProductoSeleccionado = this.registro.getIdProducto().getId();

                // Busca las entidades completas y las asigna al registro antes de guardar:
                this.registro.setIdCompra(obtenerCompraCompleta(idCompraSeleccionada));
                this.registro.setIdProducto(obtenerProductoCompleto(idProductoSeleccionado));


                // 2. Persistir
                getDao().crear(this.registro); // Asume que el método crear(T) es correcto.

                // 3. Limpieza y Notificación
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros(); // Para refrescar la tabla
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Detalle de compra guardado correctamente"));
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

    public List<Compra> getComprasDisponibles() {
        return comprasDisponibles;
    }

    public List<Producto> getProductosDisponibles() {
        return productosDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }
}