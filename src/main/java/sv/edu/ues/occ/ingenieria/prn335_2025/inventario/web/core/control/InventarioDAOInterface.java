package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import java.util.List;

public interface InventarioDAOInterface<T> {
    void crear(T registro) throws IllegalArgumentException, IllegalAccessException;
     List<T> findRange(int first, int max) throws IllegalArgumentException;
    int count() throws IllegalArgumentException;
   void eliminar(T registro) throws IllegalArgumentException, IllegalAccessException;
}
