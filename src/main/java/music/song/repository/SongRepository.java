
package music.song.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import music.song.dto.SongFilter;
import music.song.entity.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class SongRepository {

    private EntityManager em;

    @PersistenceContext(unitName = "musicPU")
    public void setEm(EntityManager em) {
        this.em = em;
    }

    public Optional<Song> find(UUID id) {
        return Optional.ofNullable(em.find(Song.class, id));
    }

    public List<Song> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);
        Root<Song> root = cq.from(Song.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public List<Song> findByArtist(UUID artistId) {
        if (artistId == null) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);
        Root<Song> root = cq.from(Song.class);
        cq.select(root).where(cb.equal(root.get("artist").get("id"), artistId));
        return em.createQuery(cq).getResultList();
    }

    // dynamic filtering by artist using optional title and genre, using AND
    public List<Song> findByArtistWithFilter(UUID artistId, SongFilter filter) {
        if (artistId == null) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);
        Root<Song> root = cq.from(Song.class);

        Predicate artistPred = cb.equal(root.get("artist").get("id"), artistId);
        List<Predicate> preds = new ArrayList<>();
        preds.add(artistPred);

        if (filter != null) {
            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                String t = filter.getTitle().trim().toLowerCase();
                String pattern = t + "%"; // starts with
                preds.add(cb.like(cb.lower(root.get("title")), pattern));
            }
            if (filter.getGenre() != null) {
                preds.add(cb.equal(root.get("genre"), filter.getGenre()));
            }
        }

        cq.select(root).where(cb.and(preds.toArray(new Predicate[0])));
        return em.createQuery(cq).getResultList();
    }

    public List<Song> findByArtistAndUser(UUID artistId, UUID userId) {
        if (artistId == null || userId == null) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);
        Root<Song> root = cq.from(Song.class);
        Predicate p1 = cb.equal(root.get("artist").get("id"), artistId);
        Predicate p2 = cb.equal(root.get("user").get("id"), userId);
        cq.select(root).where(cb.and(p1, p2));
        return em.createQuery(cq).getResultList();
    }

    public List<Song> findByUser(UUID userId) {
        if (userId == null) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);
        Root<Song> root = cq.from(Song.class);
        cq.select(root).where(cb.equal(root.get("user").get("id"), userId));
        return em.createQuery(cq).getResultList();
    }

    public void create(Song song) {
        em.persist(song);
    }

    public void update(Song song) {
        em.merge(song);
    }

    public void delete(Song song) {
        em.remove(em.find(Song.class, song.getId()));
    }
}
