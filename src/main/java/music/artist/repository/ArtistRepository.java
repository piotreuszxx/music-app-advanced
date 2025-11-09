
package music.artist.repository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import music.artist.entity.Artist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class ArtistRepository {

    private EntityManager em;

    @PersistenceContext(unitName = "musicPU")
    public void setEm(EntityManager em) {
        this.em = em;
    }

    public Optional<Artist> find(UUID id) {
        return Optional.ofNullable(em.find(Artist.class, id));
    }

    public List<Artist> findAll() {
        return em.createQuery("SELECT a FROM Artist a", Artist.class).getResultList();
    }

    @Transactional
    public void create(Artist artist) {
        if (artist == null) return;
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
