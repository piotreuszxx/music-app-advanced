package music.configuration.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import music.user.repository.UserRepository;
import music.artist.repository.ArtistRepository;
import music.song.repository.SongRepository;

@WebListener
public class CreateRepositories implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("userRepository", new UserRepository());
        event.getServletContext().setAttribute("artistRepository", new ArtistRepository());
        event.getServletContext().setAttribute("songRepository", new SongRepository());
    }
}
