
package music.user.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import music.user.dto.GetUserResponse;
import music.user.dto.GetUsersResponse;
import music.user.dto.PatchUserRequest;
import music.user.dto.PutUserRequest;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.service.UserService;
import music.song.service.SongService;
import music.song.dto.GetSongsResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class UserController {

    private UserService service;
    private ServletContext servletContext;

    protected UserController() {
    }

    @Inject
    public UserController(UserService service, ServletContext servletContext) {
        this.service = service;
        this.servletContext = servletContext;
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

    public GetUserResponse getUser(UUID uuid) {
    return service.find(uuid)
        .map(user -> new GetUserResponse(
            user.getLogin(),
            user.getName(),
            user.getSurname(),
            user.getEmail(),
            user.getSongs().stream()
                .map(s -> new GetSongsResponse.Song(s.getId(), s.getTitle()))
                .toList()))
        .orElse(null);
    }

    public GetUsersResponse getUsers() {
        return new GetUsersResponse(service.findAll().stream()
                .map(user -> new GetUsersResponse.User(user.getId(), user.getLogin()))
                .toList());
    }

    public boolean createUser(PutUserRequest request, UUID uuid) {
        if(service.find(uuid).isPresent()) {
            return false;
        }
        User newUser = User.builder()
                .id(uuid)
                .login(request.getLogin())
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(List.of(Role.USER))
                .avatar(readAvatar("guest.png"))
                .songs(new ArrayList<>())
                .build();
        service.create(newUser);
        return true;
    }

    public boolean updateUserPartial(PatchUserRequest request, UUID uuid) {
        return service.find(uuid)
                .map(user -> {
                    if(request.getLogin() != null) user.setLogin(request.getLogin());
                    if(request.getName() != null) user.setName(request.getName());
                    if(request.getSurname() != null) user.setSurname(request.getSurname());
                    if(request.getEmail() != null) user.setEmail(request.getEmail());
                    if(request.getPassword() != null) user.setPassword(request.getPassword());
                    service.update(user);
                    return true;
                })
                .orElse(false); // user does not exist
    }


    public void deleteUser(UUID uuid) {
        service.delete(uuid);
    }


    public byte[] getUserAvatar(UUID id) {
        return service.find(id)
                .map(user -> {
                    try {
                        Path dir = getAvatarDirPath();
                        var path = dir.resolve(user.getId().toString() + ".png");
                        if (Files.exists(path)) {
                            //System.out.println("Reading avatar from file: " + path);
                            return Files.readAllBytes(path);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to read avatar file: " + e.getMessage());
                    }
                    // will return null if no avatar file found, can be return null;
                    return user.getAvatar();
                })
                .orElse(null);
    }

    public boolean putUserAvatar(UUID uuid, InputStream avatarStream) {
        return service.find(uuid)
                .map(user -> {
                    try {
                        byte[] bytes = avatarStream.readAllBytes();
                        // update avatar byte[] field
                        user.setAvatar(bytes);
                        service.update(user);
                        // write/overwrite real file
                        Path dir = getAvatarDirPath();
                        if (!java.nio.file.Files.exists(dir)) java.nio.file.Files.createDirectories(dir);
                        java.nio.file.Files.write(dir.resolve(user.getId().toString() + ".png"), bytes);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to read or write avatar", e);
                    }
                    return true;
                })
                .orElse(false); // user does not exist
    }

    public boolean deleteUserAvatar(UUID uuid) {
        return service.find(uuid)
                .map(user -> {
                        Path dir = getAvatarDirPath();
                        var path = dir.resolve(user.getId().toString() + ".png");
                        boolean fileDeleted = false;
                        try {
                            fileDeleted = java.nio.file.Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("[WARN] Failed to delete avatar file: " + e.getMessage());
                        }

                        // ff there was no file and no byte[] avatar, treat as Not Found
                        if (!fileDeleted && user.getAvatar() == null) {
                            return false;
                        }

                        // clear byte[] avatar field
                        user.setAvatar(null);
                        service.update(user);
                        return true;
                })
                .orElse(false); // user does not exist
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

}
