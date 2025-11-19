package music.configuration.observer;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.*;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RunAs;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
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
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Singleton
@Startup
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
@DependsOn("InitializeAdminService")
@DeclareRoles({Role.ADMIN, Role.USER})
@RunAs(Role.ADMIN)
@NoArgsConstructor
public class InitializedData {

    @EJB
    private UserService userService;

    @EJB
    private ArtistService artistService;

    @EJB
    private SongService songService;

    @Resource(name = "avatarDir")
    private String avatarDir;

    @Resource(name = "avatarInitDir")
    private String avatarInitDir;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @PostConstruct
    @SneakyThrows
    private void initData() {
        // if admin exists, assume DB already initialized -> skip seeding
        if (userService.findByLogin("piotreusz").isPresent()) {
            System.out.println("[INFO] Data already initialized");
            return;
        }
//        User admin = User.builder()
//                .id(UUID.fromString("11111111-1111-1111-1111-000000000000"))
//                .login("admin")
//                .name("System")
//                .surname("Admin")
//                .birthday(LocalDate.of(2000, 1, 1))
//                .email("admin@simplerpg.example.com")
//                .password(passwordHash.generate("admin123".toCharArray()))
//                .roles(List.of(Role.ADMIN, Role.USER))
//                .avatar(getResourceAsByteArray("admin.png"))
//                .build();

        User piotr = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000001"))
                .login("piotreusz")
                .name("Piotr")
                .surname("Przymus")
                .birthday(LocalDate.of(2003, 5, 15))
                .email("piotreusz@gmail.com")
                .password(passwordHash.generate("piotr123".toCharArray()))
                .roles(List.of(Role.USER))
                .avatar(getResourceAsByteArray("piotreusz.png"))
                .build();

        User nicole = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000002"))
                .login("nicolele")
                .name("Nicole")
                .surname("Sengebusch")
                .birthday(LocalDate.of(2003, 5, 2))
                .email("nicolele@gmail.com")
                .password(passwordHash.generate("nicole123".toCharArray()))
                .roles(List.of(Role.USER))
                .avatar(getResourceAsByteArray("nicole.png"))
                .build();

        User ryan = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000003"))
                .login("driver")
                .name("Ryan")
                .surname("Gosling")
                .birthday(LocalDate.of(1980, 5, 2))
                .email("driver@gmail.com")
                .password(passwordHash.generate("driver123".toCharArray()))
                .roles(List.of(Role.USER))
                .avatar(getResourceAsByteArray("ryan.png"))
                .build();

        User bezprof = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-000000000004"))
                .login("bezprof")
                .name("Bez")
                .surname("Profilowego")
                .birthday(LocalDate.of(2005, 1, 1))
                .email("bezprof@gmail.com")
                .password(passwordHash.generate("bezprof123".toCharArray()))
                .roles(List.of(Role.USER))
                .avatar(null)
                .build();

//        userService.create(admin);
//        persistAvatarFile(admin);
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
        songService.createWithLinksInit(new PutSongRequest("You Was Right", Genre.HIPHOP, LocalDate.of(2016, 1, 1), 3.5, artist1.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000001"));
        songService.createWithLinksInit(new PutSongRequest("Stay", Genre.POP, LocalDate.of(2021, 1, 1), 2.7, artist2.getId(), nicole.getId()), UUID.fromString("33333333-3333-3333-3333-000000000002"));

        songService.createWithLinksInit(new PutSongRequest("Of Course", Genre.HIPHOP, LocalDate.of(2018, 1, 1), 3.3, artist1.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000003"));
        songService.createWithLinksInit(new PutSongRequest("Do What I Want", Genre.HIPHOP, LocalDate.of(2016, 1, 1), 3.5, artist1.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000004"));

        songService.createWithLinksInit(new PutSongRequest("Maybe", Genre.POP, LocalDate.of(2020, 1, 1), 3.5, artist2.getId(), piotr.getId()), UUID.fromString("33333333-3333-3333-3333-000000000005"));
        System.out.println("[INFO] Songs initialized");



    }

    private Path getAvatarServerDirPath() {
        // Persist avatars to the server working directory (defaultServer)
        String serverWorkDir = System.getProperty("user.dir");
        String avatarsFolder = avatarDir;
        return Path.of(serverWorkDir, avatarsFolder);
    }

    private byte[] getResourceAsByteArray(String fileName) {
        String path = "/" + avatarInitDir + "/" + fileName;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

            Path dir = getAvatarServerDirPath();
            if (!java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }
            java.nio.file.Files.write(dir.resolve(user.getId().toString() + ".png"), user.getAvatar());
        } catch (IOException e) {
            System.err.println("[WARN] Failed to persist avatar file for user " + user.getId() + ": " + e.getMessage());
        }
    }
}