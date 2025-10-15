package music.configuration.listener;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.SneakyThrows;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebListener//using annotation does not allow configuring order
public class InitializedData implements ServletContextListener {

    private UserService userService;
    private Path avatarDir;

    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent event) {
        userService = (UserService) event.getServletContext().getAttribute("userService");
        String avatarParam = event.getServletContext().getInitParameter("avatarDir");
        avatarDir = Path.of(event.getServletContext().getRealPath("/"), avatarParam);

        initData();
    }

    @SneakyThrows
    private void initData() {
        User admin = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaaa"))
                .login("admin")
                .name("System")
                .surname("Admin")
                .birthday(LocalDate.of(2000, 1, 1))
                .email("admin@simplerpg.example.com")
                .password("admin123")
                .roles(List.of(Role.ADMIN, Role.USER))
                .avatar(readAvatar("kacper.png"))
                .songs(null)
                .build();

        User piotr = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaab"))
                .login("piotreusz")
                .name("Piotr")
                .surname("Przymus")
                .birthday(LocalDate.of(2003, 5, 15))
                .email("piotreusz@gmail.com")
                .password("piotr123")
                .roles(List.of(Role.USER))
                .avatar(readAvatar("piotreusz.png"))
                .songs(null)
                .build();

        User nicole = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaac"))
                .login("nicolele")
                .name("Nicole")
                .surname("Sengebusch")
                .birthday(LocalDate.of(2003, 5, 2))
                .email("nicolele@gmail.com")
                .password("nicole123")
                .roles(List.of(Role.USER))
                .avatar(readAvatar("nicole.png"))
                .songs(null)
                .build();

        User ryan = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaad"))
                .login("driver")
                .name("Ryan")
                .surname("Gosling")
                .birthday(LocalDate.of(1980, 5, 2))
                .email("driver@gmail.com")
                .password("driver123")
                .roles(List.of(Role.USER))
                .avatar(readAvatar("ryan.png"))
                .songs(null)
                .build();

        User bezprof = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaae"))
                .login("bezprof")
                .name("Bez")
                .surname("Profilowego")
                .birthday(LocalDate.of(2005, 1, 1))
                .email("bezprof@gmail.com")
                .password("bezprof123")
                .roles(List.of(Role.USER))
                .avatar(null)
                .songs(null)
                .build();

        userService.create(admin);
        userService.create(piotr);
        userService.create(nicole);
        userService.create(ryan);
        userService.create(bezprof);
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
