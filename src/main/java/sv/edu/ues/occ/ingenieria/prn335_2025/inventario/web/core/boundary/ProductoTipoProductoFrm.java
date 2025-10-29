package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control.TipoProductoDao;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.TipoProducto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class ProductoTipoProductoFrm extends DefaultFrm<ProductoTipoProducto> implements Serializable {

    @EJB
    ProductoTipoProductoDAO productoTipoProductoDAO;

    @EJB
    ProductoDAO productoDAO;

    @EJB
    TipoProductoDao tipoProductoDAO;

    private List<Producto> listaProductos;
    private List<TipoProducto> listaTipoProductos;

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        cargarListaProductos();
        cargarListaTipoProductos();
    }

    private void cargarListaProductos() {
        try {
            this.listaProductos = productoDAO.findAll();
            System.out.println("Productos cargados: " + listaProductos.size());
        } catch (Exception e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
        }
    }


    private void cargarListaTipoProductos() {
        try {
            this.listaTipoProductos = tipoProductoDAO.findAll();
        } catch (Exception e) {
            System.err.println("Error al cargar tipos de producto: " + e.getMessage());
        }
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected ProductoTipoProductoDAO getDao() {
        return productoTipoProductoDAO;
    }

    @Override
    protected ProductoTipoProducto nuevoRegistro() {
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setId(UUID.randomUUID());
        nuevo.setFechaCreacion(OffsetDateTime.now());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    protected ProductoTipoProducto buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return productoTipoProductoDAO.findById((UUID) id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(ProductoTipoProducto r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected ProductoTipoProducto getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                return buscarRegistroPorId(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                System.err.println("Error al convertir ID: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(ProductoTipoProducto registro) {
        // ProductoTipoProducto no tiene campo "nombre", así que validamos otros campos
        return registro.getIdProducto() == null || registro.getIdTipoProducto() == null;
    }

    // Métodos para conversión de fechas (para p:calendar)
    public Date getFechaCreacionDate() {
        if (registro != null && registro.getFechaCreacion() != null) {
            return Date.from(registro.getFechaCreacion().toInstant());
        }
        return new Date();
    }

    public void setFechaCreacionDate(Date date) {
        if (registro != null && date != null) {
            registro.setFechaCreacion(
                    OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
            );
        }
    }

    // Getters y Setters
    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public List<TipoProducto> getListaTipoProductos() {
        return listaTipoProductos;
    }

    public void setListaTipoProductos(List<TipoProducto> listaTipoProductos) {
        this.listaTipoProductos = listaTipoProductos;
    }
}