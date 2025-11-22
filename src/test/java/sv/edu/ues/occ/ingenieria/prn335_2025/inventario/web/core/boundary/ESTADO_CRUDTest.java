package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ESTADO_CRUDTest {

    @Test
    void testEnumConstants() {
        // 1. Verificar que values() devuelve todos los elementos esperados
        ESTADO_CRUD[] estados = ESTADO_CRUD.values();
        assertNotNull(estados);
        assertTrue(estados.length > 0);

        // Verificar que contiene específicamente los 4 estados definidos
        assertEquals(4, estados.length);

        // 2. Verificar valueOf() para cada constante para asegurar consistencia
        assertEquals(ESTADO_CRUD.NADA, ESTADO_CRUD.valueOf("NADA"));
        assertEquals(ESTADO_CRUD.CREAR, ESTADO_CRUD.valueOf("CREAR"));
        assertEquals(ESTADO_CRUD.MODIFICAR, ESTADO_CRUD.valueOf("MODIFICAR"));
        assertEquals(ESTADO_CRUD.ELIMINAR, ESTADO_CRUD.valueOf("ELIMINAR"));
    }
}