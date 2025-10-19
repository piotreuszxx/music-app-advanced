package music.configuration.listener;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.SneakyThrows;
import music.song.dto.PutSongRequest;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.service.UserService;
import music.artist.entity.Artist;
import music.artist.service.ArtistService;
import music.song.entity.Song;
import music.song.entity.Genre;
import music.song.service.SongService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebListener//using annotation does not allow configuring order
public class InitializedData implements ServletContextListener {

    private UserService userService;
    private ArtistService artistService;
    private SongService songService;
    private Path avatarDir;

    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent event) {
        userService = (UserService) event.getServletContext().getAttribute("userService");
        artistService = (ArtistService) event.getServletContext().getAttribute("artistService");
        songService = (SongService) event.getServletContext().getAttribute("songService");
        String avatarParam = event.getServletContext().getInitParameter("avatarDir");
        avatarDir = Path.of(event.getServletContext().getRealPath("/"), avatarParam);

        initData();
    }

    @SneakyThrows
    private void initData() {
        User admin = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000000"))
                .login("admin")
                .name("System")
                .surname("Admin")
                .birthday(LocalDate.of(2000, 1, 1))
                .email("admin@simplerpg.example.com")
                .password("admin123")
                .roles(List.of(Role.ADMIN, Role.USER))
                .avatar(readAvatar("admin.png"))
                .build();

        User piotr = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000001"))
                .login("piotreusz")
                .name("Piotr")
                .surname("Przymus")
                .birthday(LocalDate.of(2003, 5, 15))
                .email("piotreusz@gmail.com")
                .password("piotr123")
                .roles(List.of(Role.USER))
                .avatar(readAvatar("piotreusz.png"))
                .build();

        User nicole = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000002"))
                .login("nicolele")
                .name("Nicole")
                .surname("Sengebusch")
                .birthday(LocalDate.of(2003, 5, 2))
                .email("nicolele@gmail.com")
                .password("nicole123")
                .roles(List.of(Role.USER))
                .avatar(readAvatar("nicole.png"))
                .build();

        User ryan = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000003"))
                .login("driver")
                .name("Ryan")
                .surname("Gosling")
                .birthday(LocalDate.of(1980, 5, 2))
                .email("driver@gmail.com")
                .password("driver123")
                .roles(List.of(Role.USER))
                .avatar(readAvatar("ryan.png"))
                .build();

        User bezprof = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000004"))
                .login("bezprof")
                .name("Bez")
                .surname("Profilowego")
                .birthday(LocalDate.of(2005, 1, 1))
                .email("bezprof@gmail.com")
                .password("bezprof123")
                .roles(List.of(Role.USER))
                .avatar(null)
                .build();

        userService.create(admin);
        userService.create(piotr);
        userService.create(nicole);
        userService.create(ryan);
        userService.create(bezprof);

        // initialize artists
        Artist artist1 = Artist.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-000000000001"))
                .name("Lil Uzi Vert")
                .country("USA")
                .debutYear(LocalDate.of(1994, 7, 31))
                .height(1.73)
                .build();

        Artist artist2 = Artist.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-000000000002"))
                .name("The Kid LAROI")
                .country("Australia")
                .debutYear(LocalDate.of(2003, 11, 16))
                .height(1.75)
                .build();

        artistService.create(artist1);
        artistService.create(artist2);

        // initialize songs
        Song song1 = Song.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-000000000001"))
                .title("You Was Right")
                .genre(Genre.HIPHOP)
                .releaseYear(LocalDate.of(2016, 1, 1))
                .duration(3.5)
                .artistUuid(artist1.getId())
                .userUuid(piotr.getId())
                .build();

        Song song2 = Song.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-000000000002"))
                .title("Stay")
                .genre(Genre.POP)
                .releaseYear(LocalDate.of(2021, 1, 1))
                .duration(2.7)
                .artistUuid(artist2.getId())
                .userUuid(nicole.getId())
                .build();

        // persist songs and also update relations
        songService.createWithLinks(new PutSongRequest(song1.getTitle(), song1.getGenre(), song1.getReleaseYear(), song1.getDuration(), song1.getArtistUuid(), song1.getUserUuid()), song1.getId());
        songService.createWithLinks(new PutSongRequest(song2.getTitle(), song2.getGenre(), song2.getReleaseYear(), song2.getDuration(), song2.getArtistUuid(), song2.getUserUuid()), song2.getId());
    }

    private byte[] readAvatar(String fileName) {
        try {
            Path avatarPath = avatarDir.resolve(fileName);
            if (Files.exists(avatarPath)) {
                return Files.readAllBytes(avatarPath);
            } else {
                System.err.println("[WARN] Avatar file not found: " + avatarPath);
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading avatar " + fileName, e);
        }
    }
}
