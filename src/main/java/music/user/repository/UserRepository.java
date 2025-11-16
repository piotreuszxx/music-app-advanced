
package music.user.repository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import music.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class UserRepository {

    private EntityManager em;

    @PersistenceContext(unitName = "musicPU")
    public void setEm(EntityManager em) {
        this.em = em;
    }

    public Optional<User> find(UUID id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public Optional<User> findByLogin(String login) {
        var list = em.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class)
                .setParameter("login", login)
                .getResultList();
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public void create(User user) {
        if (user == null) return;
        em.persist(user);
    }

    public void update(User user) {
        em.merge(user);
    }

    public void delete(User user) {
        User managed = em.contains(user) ? user : em.merge(user);
        em.remove(managed);
    }

}
