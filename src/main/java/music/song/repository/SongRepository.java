
package music.song.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
        return em.createQuery("SELECT s FROM Song s", Song.class).getResultList();
    }

    public List<Song> findByArtist(UUID artistId) {
        if (artistId == null) return List.of();
        return em.createQuery("SELECT s FROM Song s WHERE s.artist.id = :aid", Song.class)
                .setParameter("aid", artistId)
                .getResultList();
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
