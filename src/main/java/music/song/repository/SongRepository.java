
package music.song.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import music.song.entity.Song;

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

    /**
     * Dynamic filtering by artist using optional title and createdDate (date-only).
     * Both fields are optional and combined with AND. If filter is null or empty,
     * behaves like {@link #findByArtist(UUID)}.
     */
    public List<Song> findByArtistWithFilter(UUID artistId, music.song.dto.SongFilter filter) {
        if (artistId == null) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);
        Root<Song> root = cq.from(Song.class);

        Predicate artistPred = cb.equal(root.get("artist").get("id"), artistId);
        java.util.List<Predicate> preds = new java.util.ArrayList<>();
        preds.add(artistPred);

        if (filter != null) {
            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                String t = filter.getTitle().trim().toLowerCase();
                String pattern = t + "%"; // starts-with match (anchored on the left)
                preds.add(cb.like(cb.lower(root.get("title")), pattern));
            }
            if (filter.getCreatedDate() != null) {
                java.time.LocalDate d = filter.getCreatedDate();
                java.time.LocalDateTime start = d.atStartOfDay();
                java.time.LocalDateTime end = d.atTime(23,59,59,999999999);
                preds.add(cb.between(root.get("createdAt"), start, end));
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
