package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
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


    public UUID getIdProductoSeleccionado() {
        return idProductoSeleccionado;
    }

    public void setIdProductoSeleccionado(UUID idProductoSeleccionado) {
        this.idProductoSeleccionado = idProductoSeleccionado;
    }

    public Long getIdTipoProductoSeleccionado() {
        return idTipoProductoSeleccionado;
    }

    public void setIdTipoProductoSeleccionado(Long idTipoProductoSeleccionado) {
        this.idTipoProductoSeleccionado = idTipoProductoSeleccionado;
    }

    private UUID idProductoSeleccionado;



    private Long idTipoProductoSeleccionado;

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

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (idProductoSeleccionado == null || idTipoProductoSeleccionado == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar Producto y Tipo de Producto"));
                    return;
                }

                // Buscar objetos por ID
                Producto producto = productoDAO.findById(idProductoSeleccionado);
                TipoProducto tipo = tipoProductoDAO.findById(idTipoProductoSeleccionado);

                registro.setIdProducto(producto);
                registro.setIdTipoProducto(tipo);

                if (estado == ESTADO_CRUD.CREAR) {
                    getDao().crear(registro);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro creado correctamente"));
                } else if (estado == ESTADO_CRUD.MODIFICAR) {
                    getDao().modificar(registro);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro modificado correctamente"));
                }

                registro = null;
                estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        }
    }
    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                // Validación específica: asegurarse que producto y tipo producto estén seleccionados
                if (idProductoSeleccionado == null || idTipoProductoSeleccionado == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar Producto y Tipo de Producto"));
                    return;
                }

                // Buscar objetos por ID
                Producto producto = productoDAO.findById(idProductoSeleccionado);
                TipoProducto tipo = tipoProductoDAO.findById(idTipoProductoSeleccionado);

                registro.setIdProducto(producto);
                registro.setIdTipoProducto(tipo);

                // Llamar al DAO para modificar
                getDao().modificar(this.registro);

                // Resetear estado y refrescar modelo
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                this.idProductoSeleccionado = null;
                this.idTipoProductoSeleccionado = null;

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro modificado correctamente"));

            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
                e.printStackTrace();
            }
        }

    }
    @Override
    public void selectionHandler(SelectEvent<ProductoTipoProducto> r) {
        if (r != null && r.getObject() != null) {
            this.registro = r.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;

            // Sincronizar los auxiliares para que los combos muestren el valor actual
            if (registro.getIdProducto() != null) {
                this.idProductoSeleccionado = registro.getIdProducto().getId(); // UUID
            }

            if (registro.getIdTipoProducto() != null) {
                this.idTipoProductoSeleccionado = registro.getIdTipoProducto().getId(); // Long
            }
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