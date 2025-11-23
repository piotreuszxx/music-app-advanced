package music.user.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.credential.Credential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import music.user.entity.Role;

import static jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;

@RequestScoped
@Named
public class UserLogin {

    private final HttpServletRequest request;
    private final FacesContext facesContext;
    private final SecurityContext securityContext;
    private final HttpServletResponse response;

    @Inject
    public UserLogin(HttpServletRequest request, @FacesElement HttpServletResponse response, FacesContext facesContext, SecurityContext securityContext) {
        this.request = request;
        this.response = response;
        this.facesContext = facesContext;
        this.securityContext = securityContext;
    }

    @Getter @Setter
    private String login;

    @Getter @Setter
    private String password;

    @SneakyThrows
    public void loginAction() {
        Credential credential = new UsernamePasswordCredential(login, new Password(password));
        AuthenticationStatus status = securityContext.authenticate(request, response, withParams().credential(credential));

        var caller = securityContext.getCallerPrincipal();
        boolean isAdminRole = request.isUserInRole(Role.ADMIN); // zweryfikuj nazwę roli
        System.out.println("Auth status: " + status + ", principal: " + caller + ", isAdminRole: " + isAdminRole);
        if (status == AuthenticationStatus.SUCCESS) {
            facesContext.getExternalContext().redirect(facesContext.getExternalContext().getRequestContextPath() + "/index.xhtml");
            //facesContext.responseComplete();
        } else {
            facesContext.responseComplete();
        }
    }

}
