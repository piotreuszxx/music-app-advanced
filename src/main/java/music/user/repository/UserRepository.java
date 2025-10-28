
package music.user.repository;

import jakarta.enterprise.context.ApplicationScoped;
import music.user.entity.User;

import java.util.*;

@ApplicationScoped
public class UserRepository {

    private final Set<User> users = new HashSet<>();

    public Optional<User> find(UUID id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public void create(User user) {
        if (users.stream().anyMatch(u -> u.getId().equals(user.getId()))) {
            throw new IllegalArgumentException("User with id " + user.getId() + " already exists.");
        }
        users.add(user);
    }

    public void update(User user) {
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
    }

    public void delete(User user) {
        users.removeIf(u -> u.getId().equals(user.getId()));
    }

}
