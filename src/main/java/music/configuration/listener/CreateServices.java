package music.configuration.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import music.user.repository.UserRepository;
import music.user.service.UserService;
import music.artist.repository.ArtistRepository;
import music.artist.service.ArtistService;
import music.song.repository.SongRepository;
import music.song.service.SongService;

@WebListener//using annotation does not allow configuring order
public class CreateServices implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        UserRepository userRepository = (UserRepository) event.getServletContext().getAttribute("userRepository");
        event.getServletContext().setAttribute("userService", new UserService(userRepository));

        ArtistRepository artistRepository = (ArtistRepository) event.getServletContext().getAttribute("artistRepository");
        event.getServletContext().setAttribute("artistService", new ArtistService(artistRepository));

        SongRepository songRepository = (SongRepository) event.getServletContext().getAttribute("songRepository");
        ArtistService artistService = (ArtistService) event.getServletContext().getAttribute("artistService");
        UserService userService = (UserService) event.getServletContext().getAttribute("userService");

        event.getServletContext().setAttribute("songService", new SongService(songRepository, artistService, userService));
    }

}
