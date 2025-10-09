package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.control;

import java.util.List;

public interface InventarioDAOInterface<T> {
    void crear(T registro) throws IllegalArgumentException, IllegalAccessException;
    int count() throws IllegalArgumentException;
    List<T> findRange(int first, int max) throws IllegalArgumentException;
    void modificar(T registro) throws IllegalArgumentException;
    void eliminar(T registro) throws IllegalArgumentException, IllegalAccessException;
}
