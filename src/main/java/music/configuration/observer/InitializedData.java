package music.configuration.observer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.context.Initialized;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import lombok.SneakyThrows;
import music.song.dto.PutSongRequest;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.service.UserService;
import music.artist.entity.Artist;
import music.artist.service.ArtistService;
import music.song.entity.Genre;
import music.song.service.SongService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InitializedData {

    private final UserService userService;
    private final ArtistService artistService;
    private final SongService songService;
    private final RequestContextController requestContextController;
    private final ServletContext servletContext;

    @Inject
    public InitializedData(UserService userService,
                           ArtistService artistService,
                           SongService songService,
                           RequestContextController requestContextController,
                           ServletContext servletContext) {
        this.userService = userService;
        this.artistService = artistService;
        this.songService = songService;
        this.requestContextController = requestContextController;
        this.servletContext = servletContext;
    }

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // activate request context because some beans (controllers) are request-scoped
        boolean activated = requestContextController.activate();
        System.out.println("[INFO] context activated: " + activated);
        try {
            initData();
        } finally {
            if (activated) requestContextController.deactivate();
        }
    }

    @SneakyThrows
    private void initData() {
        // if admin exists, assume DB already initialized -> skip seeding
        if (userService.findByLogin("admin").isPresent()) {
            System.out.println("[INFO] Data already initialized");
            return;
        }
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
        persistAvatarFile(admin);
        userService.create(piotr);
        persistAvatarFile(piotr);
        userService.create(nicole);
        persistAvatarFile(nicole);
        userService.create(ryan);
        persistAvatarFile(ryan);
        userService.create(bezprof);
        persistAvatarFile(bezprof);
        System.out.println("[INFO] Users initialized");

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
        System.out.println("[INFO] Artists initialized");

        // initialize and persist songs (use service to maintain relations)
        songService.createWithLinks(new PutSongRequest("You Was Right", Genre.HIPHOP, LocalDate.of(2016, 1, 1), 3.5, artist1.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000001"));
        songService.createWithLinks(new PutSongRequest("Stay", Genre.POP, LocalDate.of(2021, 1, 1), 2.7, artist2.getId(), nicole.getId()), UUID.fromString("33333333-3333-3333-3333-000000000002"));

        songService.createWithLinks(new PutSongRequest("Of Course", Genre.HIPHOP, LocalDate.of(2018, 1, 1), 3.3, artist1.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000003"));
        songService.createWithLinks(new PutSongRequest("Do What I Want", Genre.HIPHOP, LocalDate.of(2016, 1, 1), 3.5, artist1.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000004"));

        songService.createWithLinks(new PutSongRequest("Maybe", Genre.POP, LocalDate.of(2020, 1, 1), 3.5, artist2.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000005"));
        System.out.println("[INFO] Songs initialized");



    }

    private Path getAvatarDirPath() {
        String avatarParam = servletContext.getInitParameter("avatarDir");
        if (avatarParam == null || avatarParam.isBlank()) {
            throw new IllegalStateException("Context param 'avatarDir' must be defined in web.xml and point to avatar directory");
        }
        Path p = Path.of(avatarParam);
        if (!p.isAbsolute()) p = Path.of(System.getProperty("user.dir")).resolve(p);
        return p;
    }

    private byte[] readAvatar(String fileName) {
        try {
            Path dir = getAvatarDirPath();
            Path avatarPath = dir.resolve(fileName);
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

    private void persistAvatarFile(User user) {
        if (user == null || user.getAvatar() == null) return;
        try {
            // debug filesystem
//            try {
//                Path dir = getAvatarDirPath();
//                System.out.println("[DEBUG] avatarDir=" + dir + ", avatarFs=" + dir.getFileSystem().getClass().getName() + ", defaultFs=" + java.nio.file.FileSystems.getDefault().getClass().getName());
//            } catch (Exception ignored) {}

            Path dir = getAvatarDirPath();
            if (!java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }
            java.nio.file.Files.write(dir.resolve(user.getId().toString() + ".png"), user.getAvatar());
        } catch (IOException e) {
            System.err.println("[WARN] Failed to persist avatar file for user " + user.getId() + ": " + e.getMessage());
        }
    }
}