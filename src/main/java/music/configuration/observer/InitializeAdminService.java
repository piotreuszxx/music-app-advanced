package music.configuration.observer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.repository.UserRepository;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * EJB singleton can be forced to start automatically when application starts. Injects proxy to the services and fills
 * database with default content. When using persistence storage application instance should be initialized only during
 * first run in order to init database with starting data. Good place to create first default admin user.
 */
@Singleton
@Startup
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
@NoArgsConstructor(force = true)
public class InitializeAdminService {

    private final UserRepository userRepository;

    private final Pbkdf2PasswordHash passwordHash;

    @Resource(name = "avatarDir")
    private String avatarDir;

    @Resource(name = "avatarInitDir")
    private String avatarInitDir;

    @Inject
    public InitializeAdminService(
            UserRepository userRepository,
            @SuppressWarnings("CdiInjectionPointsInspection") Pbkdf2PasswordHash passwordHash
    ) {
        this.userRepository = userRepository;
        this.passwordHash = passwordHash;
    }

    /**
     * Initializes database with some example values. Should be called after creating this object. This object should be
     * created only once.
     */
    @PostConstruct
    @SneakyThrows
    private void init() {
        if (userRepository.findByLogin("admin-service").isEmpty()) {

            User admin = User.builder()
                    .id(UUID.fromString("11111111-1111-1111-1111-000000000000"))
                    .login("admin-service")
                    .name("Admin")
                    .surname("Service")
                    .birthday(LocalDate.of(2000, 1, 1))
                    .email("admin@gmail.com")
                    .password(passwordHash.generate("admin123".toCharArray()))
                    .roles(List.of(Role.ADMIN, Role.USER))
                    .avatar(getResourceAsByteArray("admin.png"))
                    .build();

            userRepository.create(admin);
            persistAvatarFile(admin);
        }
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
