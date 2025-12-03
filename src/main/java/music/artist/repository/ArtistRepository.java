
package music.artist.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import music.artist.entity.Artist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Artist> cq = cb.createQuery(Artist.class);
        Root<Artist> root = cq.from(Artist.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public void create(Artist artist) {
        em.persist(artist);
    }

    public void update(Artist artist) {
        em.merge(artist);
    }

    public void delete(Artist artist) {
        em.remove(em.find(Artist.class, artist.getId()));
    }
}
