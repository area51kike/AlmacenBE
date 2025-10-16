package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named("idiomaBean")
@SessionScoped
public class IdiomaBean implements Serializable {

    private String idioma = "es"; // Idioma por defecto
    private String pais = "SV";   // País por defecto (El Salvador)

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Locale getLocale() {
        return new Locale(idioma, pais);  // ✅ Con idioma Y país
    }

    public void cambiarIdioma(String nuevoIdioma) {
        this.idioma = nuevoIdioma;
        // Cambiar país según el idioma si es necesario
        if ("en".equals(nuevoIdioma)) {
            this.pais = "US";
        } else if ("es".equals(nuevoIdioma)) {
            this.pais = "SV";
        } else if ("fr".equals(nuevoIdioma)) {
            this.pais = "FR";
        }

        FacesContext.getCurrentInstance()
                .getViewRoot()
                .setLocale(new Locale(nuevoIdioma, pais));
    }
}