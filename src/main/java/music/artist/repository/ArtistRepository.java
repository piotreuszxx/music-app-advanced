
package music.artist.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import music.artist.entity.Artist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ArtistRepository {

    @PersistenceContext(unitName = "musicPU")
    private EntityManager em;

    public Optional<Artist> find(UUID id) {
        return Optional.ofNullable(em.find(Artist.class, id));
    }

    public List<Artist> findAll() {
        return em.createQuery("SELECT a FROM Artist a", Artist.class).getResultList();
    }

    @Transactional
    public void create(Artist artist) {
        em.persist(artist);
    }

    @Transactional
    public void update(Artist artist) {
        em.merge(artist);
    }

    @Transactional
    public void delete(Artist artist) {
        Artist managed = em.contains(artist) ? artist : em.merge(artist);
        em.remove(managed);
    }
}
