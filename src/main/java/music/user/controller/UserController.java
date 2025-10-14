package music.user.controller;

import jakarta.ws.rs.NotFoundException;
import music.user.dto.GetUserResponse;
import music.user.dto.GetUsersResponse;
import music.user.entity.User;
import music.user.service.UserService;

import java.util.UUID;

public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public GetUserResponse getUser(UUID uuid) {
        return service.find(uuid)
                .map(user -> new GetUserResponse(user.getLogin(), user.getName(), user.getSurname(), user.getEmail(), user.getAvatar(), user.getSongs()))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public GetUsersResponse getUsers() {
        return new GetUsersResponse(service.findAll().stream()
                .map(user -> new GetUsersResponse.User(user.getId(), user.getLogin()))
                .toList());
    }

    public byte[] getUserAvatar(UUID id) {
        return service.find(id)
                .map(User::getAvatar)
                .orElseThrow(NotFoundException::new);
    }

}
