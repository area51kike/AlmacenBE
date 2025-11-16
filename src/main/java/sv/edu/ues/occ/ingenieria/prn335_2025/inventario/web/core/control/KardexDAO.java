package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class KardexDAO extends InventarioDefaultDataAccess<Kardex> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(KardexDAO.class.getName());

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public KardexDAO() {
        super(Kardex.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Kardex entidad) {
        LOGGER.log(Level.INFO, "Creando nuevo registro de kardex...");

        try {
            // Validaciones básicas
            if (entidad.getIdProducto() == null) {
                throw new IllegalArgumentException("El producto es obligatorio");
            }

            if (entidad.getTipoMovimiento() == null || entidad.getTipoMovimiento().isBlank()) {
                throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
            }

            if (entidad.getCantidad() == null) {
                throw new IllegalArgumentException("La cantidad es obligatoria");
            }

            // Generar UUID si no existe
            if (entidad.getId() == null) {
                entidad.setId(UUID.randomUUID());
            }

            // Establecer fecha actual si no tiene
            if (entidad.getFecha() == null) {
                entidad.setFecha(OffsetDateTime.now());
            }

            LOGGER.log(Level.INFO, "Guardando kardex: {0}", entidad.getId());

            // Persistir usando el método de la clase base
            super.crear(entidad);

            LOGGER.log(Level.INFO, "Kardex creado exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear kardex", e);

            // Obtener la causa raíz
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            LOGGER.log(Level.SEVERE, "Causa raíz: {0} - {1}",
                    new Object[]{causa.getClass().getName(), causa.getMessage()});

            throw new RuntimeException("Error al crear el registro de kardex: " + causa.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Kardex registro) {
        LOGGER.log(Level.INFO, "Eliminando kardex con ID: {0}", registro.getId());
        try {
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Kardex eliminado exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar kardex", e);
            throw new RuntimeException("Error al eliminar el kardex: " + e.getMessage(), e);
        }
    }

    @Override
    public Kardex findById(Object id) {
        if (!(id instanceof UUID)) {
            throw new IllegalArgumentException("El ID debe ser de tipo UUID");
        }
        LOGGER.log(Level.FINE, "Buscando kardex por ID: {0}", id);
        return super.findById(id);
    }

    /**
     * Busca todos los movimientos de kardex de un producto específico
     */
    public List<Kardex> findByProducto(UUID idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query = em.createQuery(
                    "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC",
                    Kardex.class
            );
            query.setParameter("idProducto", idProducto);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar kardex por producto", e);
            throw new RuntimeException("Error al buscar movimientos del producto", e);
        }
    }

    /**
     * Busca movimientos de kardex por tipo de movimiento
     */
    public List<Kardex> findByTipoMovimiento(String tipoMovimiento) {
        if (tipoMovimiento == null || tipoMovimiento.isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser vacío");
        }

        try {
            TypedQuery<Kardex> query = em.createQuery(
                    "SELECT k FROM Kardex k WHERE k.tipoMovimiento = :tipo ORDER BY k.fecha DESC",
                    Kardex.class
            );
            query.setParameter("tipo", tipoMovimiento);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar kardex por tipo de movimiento", e);
            throw new RuntimeException("Error al buscar movimientos por tipo", e);
        }
    }

    /**
     * Busca movimientos de kardex en un rango de fechas
     */
    public List<Kardex> findByRangoFechas(OffsetDateTime fechaInicio, OffsetDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha fin");
        }

        try {
            TypedQuery<Kardex> query = em.createQuery(
                    "SELECT k FROM Kardex k WHERE k.fecha BETWEEN :inicio AND :fin ORDER BY k.fecha DESC",
                    Kardex.class
            );
            query.setParameter("inicio", fechaInicio);
            query.setParameter("fin", fechaFin);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar kardex por rango de fechas", e);
            throw new RuntimeException("Error al buscar movimientos por fechas", e);
        }
    }

    /**
     * Busca movimientos de kardex por almacén
     */
    public List<Kardex> findByAlmacen(Integer idAlmacen) {
        if (idAlmacen == null) {
            throw new IllegalArgumentException("El ID del almacén no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query = em.createQuery(
                    "SELECT k FROM Kardex k WHERE k.idAlmacen.id = :idAlmacen ORDER BY k.fecha DESC",
                    Kardex.class
            );
            query.setParameter("idAlmacen", idAlmacen);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar kardex por almacén", e);
            throw new RuntimeException("Error al buscar movimientos del almacén", e);
        }
    }

    /**
     * Busca movimientos de kardex relacionados con una compra
     */
    public List<Kardex> findByCompraDetalle(UUID idCompraDetalle) {
        if (idCompraDetalle == null) {
            throw new IllegalArgumentException("El ID del detalle de compra no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query = em.createQuery(
                    "SELECT k FROM Kardex k WHERE k.idCompraDetalle.id = :idCompraDetalle ORDER BY k.fecha DESC",
                    Kardex.class
            );
            query.setParameter("idCompraDetalle", idCompraDetalle);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar kardex por compra detalle", e);
            throw new RuntimeException("Error al buscar movimientos de la compra", e);
        }
    }

    /**
     * Busca movimientos de kardex relacionados con una venta
     */
    public List<Kardex> findByVentaDetalle(UUID idVentaDetalle) {
        if (idVentaDetalle == null) {
            throw new IllegalArgumentException("El ID del detalle de venta no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query = em.createQuery(
                    "SELECT k FROM Kardex k WHERE k.idVentaDetalle.id = :idVentaDetalle ORDER BY k.fecha DESC",
                    Kardex.class
            );
            query.setParameter("idVentaDetalle", idVentaDetalle);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar kardex por venta detalle", e);
            throw new RuntimeException("Error al buscar movimientos de la venta", e);
        }
    }

    /**
     * Obtiene el último movimiento de un producto en un almacén específico
     */
    public Kardex findUltimoMovimiento(UUID idProducto, Integer idAlmacen) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query;

            if (idAlmacen != null) {
                query = em.createQuery(
                        "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto " +
                                "AND k.idAlmacen.id = :idAlmacen ORDER BY k.fecha DESC",
                        Kardex.class
                );
                query.setParameter("idAlmacen", idAlmacen);
            } else {
                query = em.createQuery(
                        "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC",
                        Kardex.class
                );
            }

            query.setParameter("idProducto", idProducto);
            query.setMaxResults(1);

            List<Kardex> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar último movimiento", e);
            throw new RuntimeException("Error al buscar último movimiento del producto", e);
        }
    }

    /**
     * Valida que las entidades relacionadas existan
     */
    public void validarEntidadesRelacionadas(Kardex kardex) {
        if (kardex.getIdProducto() != null && kardex.getIdProducto().getId() != null) {
            Producto producto = em.find(Producto.class, kardex.getIdProducto().getId());
            if (producto == null) {
                throw new IllegalArgumentException("El producto especificado no existe");
            }
        }

        if (kardex.getIdAlmacen() != null && kardex.getIdAlmacen().getId() != null) {
            Almacen almacen = em.find(Almacen.class, kardex.getIdAlmacen().getId());
            if (almacen == null) {
                throw new IllegalArgumentException("El almacén especificado no existe");
            }
        }

        if (kardex.getIdCompraDetalle() != null && kardex.getIdCompraDetalle().getId() != null) {
            CompraDetalle compraDetalle = em.find(CompraDetalle.class, kardex.getIdCompraDetalle().getId());
            if (compraDetalle == null) {
                throw new IllegalArgumentException("El detalle de compra especificado no existe");
            }
        }

        if (kardex.getIdVentaDetalle() != null && kardex.getIdVentaDetalle().getId() != null) {
            VentaDetalle ventaDetalle = em.find(VentaDetalle.class, kardex.getIdVentaDetalle().getId());
            if (ventaDetalle == null) {
                throw new IllegalArgumentException("El detalle de venta especificado no existe");
            }
        }
    }
}