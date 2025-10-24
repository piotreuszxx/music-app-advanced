
package music.song.repository;

import jakarta.enterprise.context.ApplicationScoped;
import music.song.entity.Song;

import java.util.*;

@ApplicationScoped
public class SongRepository {

    private final Set<Song> songs = new HashSet<>();

    public Optional<Song> find(UUID id) {
        return songs.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public List<Song> findAll() {
        return new ArrayList<>(songs);
    }

    public List<Song> findByArtist(UUID artistId) {
        if (artistId == null) return List.of();
        return songs.stream()
                .filter(s -> artistId.equals(s.getArtistUuid()))
                .toList();
    }

    public void create(Song song) {
        if (songs.stream().anyMatch(s -> s.getId().equals(song.getId()))) {
            throw new IllegalArgumentException("Song with id " + song.getId() + " already exists.");
            // this shouldnt happen
        }
        songs.add(song);
    }

    public void update(Song song) {
        songs.removeIf(s -> s.getId().equals(song.getId()));
        songs.add(song);
    }

    public void delete(Song song) {
        songs.removeIf(s -> s.getId().equals(song.getId()));
    }
}
