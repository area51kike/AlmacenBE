package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.*;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("recepcionKardexFrm")
@ViewScoped
public class RecepcionKardexFrm extends DefaultFrm<Kardex> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(RecepcionKardexFrm.class.getName());

    @Inject
    FacesContext facesContext;

    @Inject
    KardexDAO kardexDAO;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    CompraDetalleDAO compraDetalleDAO;

    @Inject
    KardexDetalleDAO kardexDetalleDAO;

    @Inject
    CompraDAO compraDAO;

    private KardexDetalle detalleKardex = new KardexDetalle();
    private CompraDetalle detalleActual;
    private List<Almacen> listaAlmacenes;

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Kardex> getDao() {
        return kardexDAO;
    }

    @Override
    protected Kardex nuevoRegistro() {
        Kardex kardex = new Kardex();
        kardex.setId(UUID.randomUUID());
        kardex.setTipoMovimiento("INGRESO");
        kardex.setFecha(OffsetDateTime.now());
        kardex.setCantidad(BigDecimal.ZERO);
        kardex.setPrecio(BigDecimal.ZERO);
        kardex.setCantidadActual(BigDecimal.ZERO);
        kardex.setPrecioActual(BigDecimal.ZERO);
        return kardex;
    }

    @Override
    protected Kardex buscarRegistroPorId(Object id) {
        if (id == null) return null;
        UUID buscado;
        try {
            if (id instanceof UUID) {
                buscado = (UUID) id;
            } else {
                buscado = UUID.fromString(id.toString());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return kardexDAO.findAll().stream()
                .filter(k -> k.getId() != null && k.getId().equals(buscado))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected String getIdAsText(Kardex r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected Kardex getIdByText(String id) {
        if (id == null) return null;
        try {
            return buscarRegistroPorId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Obtiene lista de almacenes activos con cache
     */
    public List<Almacen> getListaAlmacenes() {
        if (listaAlmacenes == null) {
            try {
                listaAlmacenes = almacenDAO.findAll().stream()
                        .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                        .toList();

                LOGGER.log(Level.INFO, "Cargados {0} almacenes activos", listaAlmacenes.size());

                // LOG para debug
                for (Almacen alm : listaAlmacenes) {
                    LOGGER.log(Level.INFO, "Almacén ID: {0}, Observaciones: {1}",
                            new Object[]{alm.getId(), alm.getObservaciones()});
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al cargar almacenes", e);
                listaAlmacenes = new ArrayList<>();
            }
        }
        return listaAlmacenes;
    }

    /**
     * Listener para cambio de almacén
     */
    public void onAlmacenChange(AjaxBehaviorEvent event) {
        if (this.registro != null && this.registro.getIdAlmacen() != null) {
            LOGGER.log(Level.INFO, "Almacén seleccionado: {0} - {1}",
                    new Object[]{
                            this.registro.getIdAlmacen().getId(),
                            this.registro.getIdAlmacen().getObservaciones()
                    });
        }
    }

    /**
     * Prepara el diálogo para crear un Kardex desde un CompraDetalle
     */
    public void prepararKardex(CompraDetalle detalle) {
        try {
            LOGGER.log(Level.INFO, "=== PREPARANDO KARDEX ===");

            this.detalleActual = detalle;
            this.registro = nuevoRegistro();

            // Pre-llenar datos desde el detalle de compra
            this.registro.setIdProducto(detalle.getIdProducto());
            this.registro.setFecha(OffsetDateTime.now());
            this.registro.setIdCompraDetalle(detalle);
            this.registro.setCantidad(detalle.getCantidad());
            this.registro.setPrecio(detalle.getPrecio());
            this.registro.setIdVentaDetalle(null);
            this.registro.setReferenciaExterna(null);
            this.registro.setIdAlmacen(null); // IMPORTANTE: Iniciar en null
            this.registro.setObservaciones(null);

            // Inicializar KardexDetalle
            detalleKardex = new KardexDetalle();
            detalleKardex.setId(UUID.randomUUID());
            detalleKardex.setActivo(true);
            detalleKardex.setIdKardex(registro);

            // Forzar recarga de almacenes
            listaAlmacenes = null;
            List<Almacen> almacenes = getListaAlmacenes();

            LOGGER.log(Level.INFO, "Producto: {0}", detalle.getIdProducto().getNombreProducto());
            LOGGER.log(Level.INFO, "Almacenes disponibles: {0}", almacenes.size());
            LOGGER.log(Level.INFO, "IdAlmacen inicial: {0}", this.registro.getIdAlmacen());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al preparar kardex", e);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Error al preparar recepción: " + e.getMessage()));
        }
    }

    public void btnRecibirHandler(ActionEvent actionEvent) {
        try {
            LOGGER.log(Level.INFO, "=== INICIANDO RECEPCIÓN ===");

            // Log del estado actual
            LOGGER.log(Level.INFO, "IdAlmacen antes de validar: {0}",
                    this.registro.getIdAlmacen());

            // Validar almacén
            if (this.registro.getIdAlmacen() == null) {
                LOGGER.log(Level.WARNING, "Almacén no seleccionado");
                facesContext.addMessage("frmKardex:cbAlmacenKardex",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Debe seleccionar un almacén"));
                return;
            }

            LOGGER.log(Level.INFO, "Recibiendo en almacén ID: {0}, Observaciones: {1}",
                    new Object[]{
                            this.registro.getIdAlmacen().getId(),
                            this.registro.getIdAlmacen().getObservaciones()
                    });

            // 1. Guardar el movimiento en Kardex
            kardexDAO.crear(this.registro);
            LOGGER.log(Level.INFO, "Kardex creado: {0}", this.registro.getId());

            // Guardar detalle de kardex
            detalleKardex.setIdKardex(this.registro);
            kardexDetalleDAO.crear(detalleKardex);
            LOGGER.log(Level.INFO, "KardexDetalle creado: {0}", detalleKardex.getId());

            // 2. Actualizar estado del detalle de compra
            CompraDetalle detalle = this.registro.getIdCompraDetalle();
            detalle.setEstado("RECIBIDO");
            compraDetalleDAO.modificar(detalle);
            LOGGER.log(Level.INFO, "CompraDetalle actualizado a RECIBIDO");

            // 3. Verificar si todos los detalles están recibidos
            Long idCompra = detalle.getIdCompra().getId();
            verificarYActualizarEstadoCompra(idCompra);

            // 4. Limpiar formulario
            this.registro = null;
            this.detalleActual = null;
            this.detalleKardex = new KardexDetalle();
            this.listaAlmacenes = null;

            // 5. Mensaje de éxito
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Producto recibido correctamente"));

            LOGGER.log(Level.INFO, "=== RECEPCIÓN COMPLETADA ===");

            btnCancelarHandler(actionEvent);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al recibir producto", e);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo recibir el producto: " + e.getMessage()));
        }
    }

    private void verificarYActualizarEstadoCompra(Long idCompra) {
        try {
            if (compraDetalleDAO.todosDetallesRecibidos(idCompra)) {
                compraDAO.actualizarEstado(idCompra, "RECIBIDA");
                facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Información",
                                "Todos los productos han sido recibidos. Compra completada."));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al verificar estado de compra", e);
        }
    }

    // Getters y Setters
    public CompraDetalle getDetalleActual() {
        return detalleActual;
    }

    public void setDetalleActual(CompraDetalle detalleActual) {
        this.detalleActual = detalleActual;
    }

    public KardexDetalle getDetalleKardex() {
        return detalleKardex;
    }

    public void setDetalleKardex(KardexDetalle detalleKardex) {
        this.detalleKardex = detalleKardex;
    }
}