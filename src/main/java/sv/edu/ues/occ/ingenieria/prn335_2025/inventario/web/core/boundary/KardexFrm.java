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
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

@Named("kardexFrm")
@ViewScoped
public class KardexFrm extends DefaultFrm<Kardex> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(KardexFrm.class.getName());

    @Inject
    private KardexDAO kardexDAO;  // CORREGIDO: mayúscula

    @Inject
    private ProductoDAO productoDAO;  // CORREGIDO: mayúscula

    @Inject
    private AlmacenDAO almacenDAO;  // CORREGIDO: mayúscula

    @Inject
    private CompraDetalleDAO compraDetalleDAO;  // CORREGIDO: mayúscula

    @Inject
    private VentaDetalleDAO ventaDetalleDAO;

    // Listas para los selectOneMenu
    private List<Producto> productosDisponibles;
    private List<Almacen> almacenesDisponibles;
    private List<CompraDetalle> comprasDetalleDisponibles;
    private List<VentaDetalle> ventasDetalleDisponibles;
    private final List<String> tiposMovimiento = List.of(
            "ENTRADA_COMPRA",
            "SALIDA_VENTA",
            "AJUSTE_ENTRADA",
            "AJUSTE_SALIDA",
            "TRANSFERENCIA_ENTRADA",
            "TRANSFERENCIA_SALIDA",
            "DEVOLUCION_COMPRA",
            "DEVOLUCION_VENTA"
    );

    // ---------------------- Inicialización ----------------------

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarDatosFiltros();
        this.pageSize = 6;
        this.nombreBean = "Gestión de Kardex";
        LOGGER.log(Level.INFO, "KardexFrm inicializado correctamente");
    }

    private void cargarDatosFiltros() {
        try {
            this.productosDisponibles = productoDAO != null ? productoDAO.findAll() : new ArrayList<>();
            this.almacenesDisponibles = almacenDAO != null ? almacenDAO.findAll() : new ArrayList<>();
            this.comprasDetalleDisponibles = compraDetalleDAO != null ? compraDetalleDAO.findAll() : new ArrayList<>();
            this.ventasDetalleDisponibles = ventaDetalleDAO != null ? ventaDetalleDAO.findAll() : new ArrayList<>();

            LOGGER.log(Level.INFO, "Cargados {0} productos, {1} almacenes",
                    new Object[]{productosDisponibles.size(), almacenesDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de filtros", e);
            this.productosDisponibles = new ArrayList<>();
            this.almacenesDisponibles = new ArrayList<>();
            this.comprasDetalleDisponibles = new ArrayList<>();
            this.ventasDetalleDisponibles = new ArrayList<>();
        }
    }

    // ---------------------- Métodos Abstractos ----------------------

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Kardex> getDao() {
        return kardexDAO;  // CORREGIDO
    }

    @Override
    protected Kardex nuevoRegistro() {
        Kardex kardex = new Kardex();
        kardex.setId(UUID.randomUUID());
        kardex.setFecha(OffsetDateTime.now());
        kardex.setCantidad(BigDecimal.ZERO);
        kardex.setPrecio(BigDecimal.ZERO);
        kardex.setCantidadActual(BigDecimal.ZERO);
        kardex.setPrecioActual(BigDecimal.ZERO);

        // Inicializar entidades FK para evitar NullPointer en XHTML
        kardex.setIdProducto(new Producto());
        kardex.setIdAlmacen(new Almacen());

        LOGGER.log(Level.INFO, "Nuevo registro de kardex creado con ID: {0}", kardex.getId());
        return kardex;
    }

    @Override
    protected Kardex buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return kardexDAO.findById((UUID) id);  // CORREGIDO
        }
        return null;
    }

    @Override
    protected String getIdAsText(Kardex r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected Kardex getIdByText(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(id);
            return kardexDAO.findById(uuid);  // CORREGIDO
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "ID inválido: {0}", id);
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(Kardex registro) {
        boolean fallo = false;

        // 1. Validar Producto
        if (registro.getIdProducto() == null || registro.getIdProducto().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Producto."));
            fallo = true;
        }

        // 2. Validar Almacén
        if (registro.getIdAlmacen() == null || registro.getIdAlmacen().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Almacén."));
            fallo = true;
        }

        // 3. Validar Tipo de Movimiento
        if (registro.getTipoMovimiento() == null || registro.getTipoMovimiento().trim().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un tipo de movimiento."));
            fallo = true;
        }

        // 4. Validar Cantidad
        if (registro.getCantidad() == null || registro.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La cantidad debe ser mayor a cero."));
            fallo = true;
        }

        // 5. Validar Precio
        if (registro.getPrecio() == null || registro.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "El precio debe ser mayor o igual a cero."));
            fallo = true;
        }

        // 6. Validar Fecha
        if (registro.getFecha() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La fecha es obligatoria."));
            fallo = true;
        }

        return fallo;
    }

    // ---------------------- Métodos Auxiliares ----------------------

    private Producto obtenerProductoCompleto(UUID id) {
        if (id == null) return null;
        try {
            return productoDAO.findById(id);  // CORREGIDO
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Producto con ID: " + id, e);
            return null;
        }
    }

    private Almacen obtenerAlmacenCompleto(Integer id) {
        if (id == null) return null;
        try {
            return almacenDAO.findById(id);  // CORREGIDO
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Almacén con ID: " + id, e);
            return null;
        }
    }

    private CompraDetalle obtenerCompraDetalleCompleto(UUID id) {
        if (id == null) return null;
        try {
            return compraDetalleDAO.findById(id);  // CORREGIDO
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener CompraDetalle con ID: " + id, e);
            return null;
        }
    }

    private VentaDetalle obtenerVentaDetalleCompleto(UUID id) {
        if (id == null) return null;
        try {
            return ventaDetalleDAO.findById(id);  // CORREGIDO
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener VentaDetalle con ID: " + id, e);
            return null;
        }
    }

    /**
     * Calcula los valores actuales basándose en el último movimiento
     */
    private void calcularValoresActuales(Kardex kardex) {
        try {
            // Obtener el último movimiento del producto en el almacén
            Kardex ultimoMovimiento = kardexDAO.findUltimoMovimiento(  // CORREGIDO
                    kardex.getIdProducto().getId(),
                    kardex.getIdAlmacen().getId()
            );

            BigDecimal cantidadAnterior = ultimoMovimiento != null
                    ? ultimoMovimiento.getCantidadActual()
                    : BigDecimal.ZERO;

            BigDecimal precioAnterior = ultimoMovimiento != null
                    ? ultimoMovimiento.getPrecioActual()
                    : BigDecimal.ZERO;

            // Calcular según el tipo de movimiento
            String tipoMov = kardex.getTipoMovimiento();
            BigDecimal cantidad = kardex.getCantidad();
            BigDecimal precio = kardex.getPrecio();

            if (tipoMov.startsWith("ENTRADA") || tipoMov.equals("DEVOLUCION_VENTA")) {
                // Entrada: suma cantidad
                kardex.setCantidadActual(cantidadAnterior.add(cantidad));

                // Precio promedio ponderado
                if (cantidadAnterior.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal totalAnterior = cantidadAnterior.multiply(precioAnterior);
                    BigDecimal totalNuevo = cantidad.multiply(precio);
                    BigDecimal cantidadTotal = cantidadAnterior.add(cantidad);
                    // CORREGIDO: usar RoundingMode en lugar de constante deprecated
                    kardex.setPrecioActual(totalAnterior.add(totalNuevo).divide(cantidadTotal, 2, RoundingMode.HALF_UP));
                } else {
                    kardex.setPrecioActual(precio);
                }

            } else if (tipoMov.startsWith("SALIDA") || tipoMov.equals("DEVOLUCION_COMPRA")) {
                // Salida: resta cantidad
                kardex.setCantidadActual(cantidadAnterior.subtract(cantidad));
                kardex.setPrecioActual(precioAnterior); // Mantiene el precio anterior

                // Validar que no quede en negativo
                if (kardex.getCantidadActual().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException("No hay suficiente inventario. Disponible: " + cantidadAnterior);
                }
            }

            LOGGER.log(Level.INFO, "Valores actuales calculados: Cantidad={0}, Precio={1}",
                    new Object[]{kardex.getCantidadActual(), kardex.getPrecioActual()});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al calcular valores actuales", e);
            throw new RuntimeException("Error al calcular inventario: " + e.getMessage(), e);
        }
    }

    // ---------------------- Manejo de Guardado ----------------------

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
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

            // Sincronizar Entidades FK obligatorias
            UUID idProductoSeleccionado = this.registro.getIdProducto().getId();
            Integer idAlmacenSeleccionado = this.registro.getIdAlmacen().getId();

            Producto productoCompleto = obtenerProductoCompleto(idProductoSeleccionado);
            Almacen almacenCompleto = obtenerAlmacenCompleto(idAlmacenSeleccionado);

            if (productoCompleto == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró el Producto seleccionado."));
                return;
            }

            if (almacenCompleto == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró el Almacén seleccionado."));
                return;
            }

            this.registro.setIdProducto(productoCompleto);
            this.registro.setIdAlmacen(almacenCompleto);

            // Sincronizar FK opcionales
            if (this.registro.getIdCompraDetalle() != null && this.registro.getIdCompraDetalle().getId() != null) {
                CompraDetalle compraDetalle = obtenerCompraDetalleCompleto(this.registro.getIdCompraDetalle().getId());
                this.registro.setIdCompraDetalle(compraDetalle);
            } else {
                this.registro.setIdCompraDetalle(null);
            }

            if (this.registro.getIdVentaDetalle() != null && this.registro.getIdVentaDetalle().getId() != null) {
                VentaDetalle ventaDetalle = obtenerVentaDetalleCompleto(this.registro.getIdVentaDetalle().getId());
                this.registro.setIdVentaDetalle(ventaDetalle);
            } else {
                this.registro.setIdVentaDetalle(null);
            }

            // Calcular valores actuales de inventario
            calcularValoresActuales(this.registro);

            // Persistir
            getDao().crear(this.registro);

            // Limpieza y Notificación
            LOGGER.log(Level.INFO, "Kardex guardado: {0}", this.registro.getId());
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Movimiento de kardex registrado correctamente"));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar kardex", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar",
                            "Ocurrió un error: " + e.getMessage()));
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                        "No se permite modificar registros de kardex por integridad del inventario"));
    }

    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                        "No se permite eliminar registros de kardex por integridad del inventario"));
    }

    // ---------------------- Getters para JSF ----------------------

    public List<Producto> getProductosDisponibles() {
        if (productosDisponibles == null) {
            cargarDatosFiltros();
        }
        return productosDisponibles;
    }

    public List<Almacen> getAlmacenesDisponibles() {
        if (almacenesDisponibles == null) {
            cargarDatosFiltros();
        }
        return almacenesDisponibles;
    }

    public List<CompraDetalle> getComprasDetalleDisponibles() {
        if (comprasDetalleDisponibles == null) {
            cargarDatosFiltros();
        }
        return comprasDetalleDisponibles;
    }

    public List<VentaDetalle> getVentasDetalleDisponibles() {
        if (ventasDetalleDisponibles == null) {
            cargarDatosFiltros();
        }
        return ventasDetalleDisponibles;
    }

    public List<String> getTiposMovimiento() {
        return tiposMovimiento;
    }
}