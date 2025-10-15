package music.configuration.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import music.user.controller.UserController;
import music.user.service.UserService;

import java.nio.file.Path;

@WebListener
public class CreateControllers implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        UserService userService = (UserService) event.getServletContext().getAttribute("userService");
        String avatarParam = event.getServletContext().getInitParameter("avatarDir");
        Path avatarDir = Path.of(event.getServletContext().getRealPath("/"), avatarParam);
        event.getServletContext().setAttribute("userController", new UserController(userService, avatarDir));
    }
}
