
package music.song.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import music.song.entity.Song;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SongRepository {

    @PersistenceContext(unitName = "musicPU")
    private EntityManager em;

    public Optional<Song> find(UUID id) {
        return Optional.ofNullable(em.find(Song.class, id));
    }

    public List<Song> findAll() {
        return em.createQuery("SELECT s FROM Song s", Song.class).getResultList();
    }

    public List<Song> findByArtist(UUID artistId) {
        if (artistId == null) return List.of();
        return em.createQuery("SELECT s FROM Song s WHERE s.artist.id = :aid", Song.class)
                .setParameter("aid", artistId)
                .getResultList();
    }

    @Transactional
    public void create(Song song) {
        em.persist(song);
    }

    @Transactional
    public void update(Song song) {
        em.merge(song);
    }

    @Transactional
    public void delete(Song song) {
        Song managed = em.contains(song) ? song : em.merge(song);
        em.remove(managed);
    }
}
