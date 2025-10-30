package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.entity.UnidadMedida;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnidadMedidaDAOTest {

    @InjectMocks
    UnidadMedidaDAO dao;

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
}
