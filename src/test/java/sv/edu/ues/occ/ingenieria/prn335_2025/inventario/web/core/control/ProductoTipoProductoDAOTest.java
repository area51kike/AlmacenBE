package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.ProductoTipoProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ProductoTipoProductoDAOTest {

    @InjectMocks
    ProductoTipoProductoDAO dao;

    @Mock
    EntityManager em;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEntityManager() {
        assertEquals(em, dao.getEntityManager());
    }

    @Test
    void findById() {
        ProductoTipoProducto ptp = new ProductoTipoProducto();
        when(em.find(ProductoTipoProducto.class, 1)).thenReturn(ptp);

        ProductoTipoProducto result = dao.findById(1);
        assertEquals(ptp, result);
    }
}
