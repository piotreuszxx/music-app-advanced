
package music.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import music.user.entity.User;
import music.user.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@NoArgsConstructor(force = true)
public class UserService {

    private UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    @Transactional
    public void create(User user) {
        userRepository.create(user);
    }

    @Transactional
    public void update(User user) {
        userRepository.update(user);
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.delete(userRepository.find(id).orElseThrow());
    }

    /**
     * Updates avatar of the user.
     *
     * @param id user's id
     * @param is input stream containing new portrait
     */
    public void updateAvatar(UUID id, InputStream is) {
        userRepository.find(id).ifPresent(user -> {
            try {
                user.setAvatar(is.readAllBytes());
                userRepository.update(user);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

}
