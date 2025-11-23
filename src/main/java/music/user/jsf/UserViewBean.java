package music.user.jsf;

import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import music.user.dto.GetUserResponse;
import music.user.service.UserService;

@Named("userView")
@ViewScoped
@Getter
@Setter
public class UserViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserService userService;

    private String id;
    private GetUserResponse userDto;
    private boolean notFound = false;

    public void init() {
        if (id == null) return;
        UUID uuid = UUID.fromString(id);
        Optional<GetUserResponse> u = userService.findDto(uuid);
        userDto = u.orElse(null);
        notFound = (userDto == null);
        if (notFound) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                try {
                    fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/404.xhtml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public GetUserResponse getUserDto() {
        return userDto;
    }
}
