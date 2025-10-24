
package music.user.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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
    private Path avatarDir;

    protected UserController() {
    }

    @Inject
    public UserController(UserService service, Path avatarDir) {
        this.service = service;
        this.avatarDir = avatarDir;
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
                .map(User::getAvatar)
                .orElse(null);
    }

    public boolean putUserAvatar(UUID uuid, InputStream avatarStream) {
        return service.find(uuid)
                .map(user -> {
                    try {
                        user.setAvatar(avatarStream.readAllBytes());
                        service.update(user);
                        Files.write(avatarDir.resolve(user.getId().toString() + ".png"), avatarStream.readAllBytes());
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to read avatar", e);
                    }
                    return true;
                })
                .orElse(false); // user does not exist
    }

    public boolean deleteUserAvatar(UUID uuid) {
        return service.find(uuid)
                .map(user -> {
                    user.setAvatar(null);
                    service.update(user);
                    return true;
                })
                .orElse(false); // user does not exist
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
