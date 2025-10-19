package music.configuration.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import music.user.controller.UserController;
import music.user.service.UserService;
import music.artist.controller.ArtistController;
import music.artist.service.ArtistService;
import music.song.controller.SongController;
import music.song.service.SongService;

import java.nio.file.Path;

@WebListener
public class CreateControllers implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        String avatarParam = event.getServletContext().getInitParameter("avatarDir");
        Path avatarDir = Path.of(event.getServletContext().getRealPath("/"), avatarParam);

        SongService songService = (SongService) event.getServletContext().getAttribute("songService");
        event.getServletContext().setAttribute("songController", new SongController(songService));

        UserService userService = (UserService) event.getServletContext().getAttribute("userService");
        event.getServletContext().setAttribute("userController", new UserController(userService, songService, avatarDir));

        ArtistService artistService = (ArtistService) event.getServletContext().getAttribute("artistService");
        event.getServletContext().setAttribute("artistController", new ArtistController(artistService));
    }
}
