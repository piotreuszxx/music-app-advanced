package music.configuration.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import music.user.repository.UserRepository;
import music.user.service.UserService;

@WebListener//using annotation does not allow configuring order
public class CreateServices implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        UserRepository userRepository = (UserRepository) event.getServletContext().getAttribute("userRepository");
        event.getServletContext().setAttribute("userService", new UserService(userRepository));
    }

}
