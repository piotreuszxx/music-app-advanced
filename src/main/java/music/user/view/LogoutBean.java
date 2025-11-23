package music.user.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;

@RequestScoped
@Named
public class LogoutBean {

    @Inject
    private HttpServletRequest request;

    @Inject
    private FacesContext facesContext;

    @SneakyThrows
    public void logout() {
        try {
            request.logout();
        } catch (Exception ignored) {}
        facesContext.getExternalContext().invalidateSession();
        facesContext.getExternalContext().redirect(facesContext.getExternalContext().getRequestContextPath() + "/index.xhtml");
    }
}
