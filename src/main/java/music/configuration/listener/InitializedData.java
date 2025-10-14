package music.configuration.listener;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.SneakyThrows;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.service.UserService;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebListener//using annotation does not allow configuring order
public class InitializedData implements ServletContextListener {

    private UserService userService;

    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent event) {
        userService = (UserService) event.getServletContext().getAttribute("userService");
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
                .password("adminadmin")
                .roles(List.of(Role.ADMIN, Role.USER))
                .avatar(null)
                .songs(null)
                .build();

        User piotr = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaab"))
                .login("piotreusz")
                .name("Piotr")
                .surname("Przymus")
                .birthday(LocalDate.of(2003, 5, 15))
                .email("piotreusz@gmail.com")
                .password("abcd")
                .roles(List.of(Role.USER))
                .avatar(null)
                .songs(null)
                .build();

        User nicole = User.builder()
                .id(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4faaac"))
                .login("nicolele")
                .name("Nicole")
                .surname("Sengebusch")
                .birthday(LocalDate.of(2003, 5, 2))
                .email("nicolele@gmail.com")
                .password("adminadmin")
                .roles(List.of(Role.USER))
                .avatar(null)
                //.avatar(getResourceAsByteArray("avatars/rsz_2image.png"))
                .songs(null)
                .build();

        userService.create(admin);
        userService.create(piotr);
        userService.create(nicole);
    }

    @SneakyThrows
    private byte[] getResourceAsByteArray(String name) {
        try (InputStream is = this.getClass().getResourceAsStream(name)) {
            if (is != null) {
                return is.readAllBytes();
            } else {
                throw new IllegalStateException("Unable to get resource %s".formatted(name));
            }
        }
    }
}
