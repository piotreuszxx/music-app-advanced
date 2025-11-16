
package music.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import music.song.dto.GetSongsResponse;
import music.user.dto.GetUserResponse;
import music.user.dto.GetUsersResponse;
import music.user.dto.PatchUserRequest;
import music.user.dto.PutUserRequest;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@NoArgsConstructor(force = true)
public class UserService {

    private UserRepository userRepository;
    private ServletContext servletContext;

    @Inject
    public UserService(UserRepository userRepository, ServletContext servletContext) {
        this.userRepository = userRepository;
        this.servletContext = servletContext;
    }

    public Optional<User> find(UUID id) {
        return userRepository.find(id);
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    // DTO helpers
    public List<GetUsersResponse.User> findAllDtos() {
        var list = findAll();
        return list.stream().map(u -> GetUsersResponse.User.builder()
                .id(u.getId())
                .login(u.getLogin())
                .build()).toList();
    }

    public Optional<GetUserResponse> findDto(UUID id) {
        return find(id).map(u -> {
            GetUserResponse dto = new GetUserResponse();
            dto.setLogin(u.getLogin());
            dto.setName(u.getName());
            dto.setSurname(u.getSurname());
            dto.setEmail(u.getEmail());
            if (u.getSongs() != null) {
                dto.setSongs(u.getSongs().stream().map(s -> {
                    var small = new GetSongsResponse.Song();
                    small.setId(s.getId());
                    small.setTitle(s.getTitle());
                    return small;
                }).toList());
            }
            return dto;
        });
    }

    @Transactional
    public void create(User user) {
        userRepository.create(user);
    }

    @Transactional
    public boolean createFromRequest(PutUserRequest request, UUID uuid) {
        if (find(uuid).isPresent()) return false;
        User newUser = User.builder()
                .id(uuid)
                .login(request.getLogin())
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(List.of(Role.USER))
                .avatar(readAvatarFile("guest.png"))
                .songs(new ArrayList<>())
                .build();
        create(newUser);
        return true;
    }

    @Transactional
    public void update(User user) {
        userRepository.update(user);
    }

    @Transactional
    public boolean updatePartial(PatchUserRequest request, UUID uuid) {
        return find(uuid)
                .map(user -> {
                    if (request.getLogin() != null) user.setLogin(request.getLogin());
                    if (request.getName() != null) user.setName(request.getName());
                    if (request.getSurname() != null) user.setSurname(request.getSurname());
                    if (request.getEmail() != null) user.setEmail(request.getEmail());
                    if (request.getPassword() != null) user.setPassword(request.getPassword());
                    userRepository.update(user);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void delete(UUID id) {
        deleteAvatarFile(id.toString() + ".png");
        userRepository.delete(userRepository.find(id).orElseThrow());
    }

    public Optional<byte[]> getAvatar(UUID id) {
        return userRepository.find(id).map(user -> {
            byte[] fromFile = null;
            fromFile = readAvatarFile(id.toString() + ".png");
            if (fromFile != null) return fromFile;
            return user.getAvatar();
        });
    }

    @Transactional
    public boolean updateAvatar(UUID id, byte[] avatarBytes) {
        return userRepository.find(id).map(user -> {
            user.setAvatar(avatarBytes);
            userRepository.update(user);
            persistAvatarFile(id, avatarBytes);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean deleteAvatar(UUID id) {
        return userRepository.find(id).map(user -> {
            user.setAvatar(null);
            userRepository.update(user);
            deleteAvatarFile(id.toString() + ".png");
            return true;
        }).orElse(false);
    }

    private Path getAvatarDirPath() {
        String serverWorkDir = System.getProperty("user.dir");
        String avatarsFolder = servletContext.getInitParameter("avatarDir");
        return Path.of(serverWorkDir, avatarsFolder);
    }

    private byte[] readAvatarFile(String fileName) {
        Path dir = getAvatarDirPath();
        Path avatarPath = dir.resolve(fileName);
        System.out.println("[DEBUG] Reading avatar from " + avatarPath.toAbsolutePath());
        try {
            if (Files.exists(avatarPath)) {
                return Files.readAllBytes(avatarPath);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading avatar " + fileName, e);
        }
    }

    private void persistAvatarFile(UUID id, byte[] bytes) {
        if (bytes == null) return;
        Path dir = getAvatarDirPath();
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(dir.resolve(id.toString() + ".png"), bytes);
        } catch (IOException e) {
            throw new RuntimeException("Error persisting avatar for user " + id, e);
        }
    }

    private void deleteAvatarFile(String fileName) {
        Path dir = getAvatarDirPath();
        try {
            Path avatarPath = dir.resolve(fileName);
            if (Files.exists(avatarPath)) {
                Files.delete(avatarPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting avatar " + fileName, e);
        }
    }
}
