package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdiomaFrmTest {

    @Mock
    FacesContext facesContext;

    @Mock
    UIViewRoot viewRoot;

    // NOTA: Si tu clase se llama diferente (ej: IdiomaBean), cambia 'IdiomaFrm' aquí
    @InjectMocks
    IdiomaBean cut;

    private MockedStatic<FacesContext> mockedFacesContext;

    // Variables para silenciar la consola
    private final PrintStream originalErr = System.err;
    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        // --- SILENCIADOR DE CONSOLA ---
        // 1. Redirigir System.err
        System.setErr(new PrintStream(new OutputStream() {
            @Override public void write(int b) {}
        }));

        // 2. Apagar Logger (por si acaso tu clase lo usa)
        // Asegúrate de cambiar 'IdiomaFrm.class' si tu clase tiene otro nombre
        Logger appLogger = Logger.getLogger(IdiomaBean.class.getName());
        originalLogLevel = appLogger.getLevel();
        appLogger.setLevel(Level.OFF);
        // ------------------------------

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        // Configuramos el comportamiento lenient porque se llama dentro de cambiarIdioma
        lenient().when(facesContext.getViewRoot()).thenReturn(viewRoot);
    }

    @AfterEach
    void tearDown() {
        // Restaurar consola y logger
        System.setErr(originalErr);
        if (originalLogLevel != null) {
            Logger.getLogger(IdiomaBean.class.getName()).setLevel(originalLogLevel);
        } else {
            Logger.getLogger(IdiomaBean.class.getName()).setLevel(Level.INFO);
        }

        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
    }

    @Test
    void testGetSetIdioma() {
        cut.setIdioma("fr");
        assertEquals("fr", cut.getIdioma());
    }

    @Test
    void testGetSetPais() {
        cut.setPais("FR");
        assertEquals("FR", cut.getPais());
    }

    @Test
    void testGetLocale() {
        cut.setIdioma("en");
        cut.setPais("US");

        Locale locale = cut.getLocale();
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
    }

    @Test
    void testCambiarIdioma_Ingles() {
        cut.cambiarIdioma("en");

        assertEquals("en", cut.getIdioma());
        assertEquals("US", cut.getPais());
        verify(viewRoot).setLocale(new Locale("en", "US"));
    }

    @Test
    void testCambiarIdioma_Espanol() {
        // Pre-condición: Estaba en otro idioma
        cut.setIdioma("en");
        cut.setPais("US");

        cut.cambiarIdioma("es");

        assertEquals("es", cut.getIdioma());
        assertEquals("SV", cut.getPais());
        verify(viewRoot).setLocale(new Locale("es", "SV"));
    }

    @Test
    void testCambiarIdioma_Frances() {
        cut.cambiarIdioma("fr");

        assertEquals("fr", cut.getIdioma());
        assertEquals("FR", cut.getPais());
        verify(viewRoot).setLocale(new Locale("fr", "FR"));
    }

    @Test
    void testCambiarIdioma_Otro() {
        cut.setPais("SV");

        // Caso default (idioma desconocido, mantiene país)
        cut.cambiarIdioma("de");

        assertEquals("de", cut.getIdioma());
        assertEquals("SV", cut.getPais());
        verify(viewRoot).setLocale(new Locale("de", "SV"));
    }
}