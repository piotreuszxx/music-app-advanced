package music.user.view;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import music.user.dto.GetUsersResponse;
import music.user.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequestScoped
@Named("usersView")
public class UsersView {

    @EJB
    private UserService userService;

    private List<GetUsersResponse.User> users;

    @PostConstruct
    public void init() {
        users = userService.findAllDtos();
    }

    public List<GetUsersResponse.User> getUsers() {
        return users;
    }

    public void checkAccess() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
        if (req.getUserPrincipal() == null) {
            fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/authentication/custom/login.xhtml");
        }
    }

    public String delete(UUID id) {
        userService.delete(id);
        init();
        return null;
    }
}
