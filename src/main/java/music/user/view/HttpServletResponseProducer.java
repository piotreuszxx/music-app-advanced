package music.user.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseProducer {

    /**
     * Current faces context.
     */
    private final FacesContext facesContext;

    /**
     * @param facesContext current faces context
     */
    @Inject
    public HttpServletResponseProducer(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    /**
     *
     * @return managed HTTP response
     */
    @Produces
    @RequestScoped
    @FacesElement
    HttpServletResponse create() {
        return (HttpServletResponse) facesContext.getExternalContext().getResponse();
    }

}
