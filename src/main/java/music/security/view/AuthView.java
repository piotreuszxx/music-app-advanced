package music.security.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.ejb.EJB;
import music.user.service.UserService;
import music.user.entity.Role;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
@Named("authView")
public class AuthView {

    @EJB
    private UserService userService;

    public void checkAuth() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return;
        if (fc.getExternalContext().getUserPrincipal() == null) {
            fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/authentication/custom/login.xhtml");
        }
    }

    public boolean isAdmin() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return false;
        return fc.getExternalContext().isUserInRole(Role.ADMIN);
    }

    public UUID getCurrentUserId() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return null;
        Principal p = fc.getExternalContext().getUserPrincipal();
        if (p == null) return null;
        Optional<music.user.entity.User> u = userService.findByLogin(p.getName());
        if (u.isEmpty()) return null;
        return u.get().getId();
    }

    public boolean isOwner(Object ownerId) {
        if (ownerId == null) return false;
        UUID owner;
        try {
            if (ownerId instanceof UUID) owner = (UUID) ownerId;
            else owner = UUID.fromString(ownerId.toString());
        } catch (Exception e) {
            return false;
        }
        UUID cur = getCurrentUserId();
        return cur != null && cur.equals(owner);
    }

    public void checkAdminAuth() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return;
        if (fc.getExternalContext().getUserPrincipal() == null) {
            fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/authentication/custom/login.xhtml");
            return;
        }
        if (!isAdmin()) {
            fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/403.xhtml");
        }
    }
}
