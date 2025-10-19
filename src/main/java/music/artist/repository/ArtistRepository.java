
package music.artist.repository;

import jakarta.enterprise.context.ApplicationScoped;
import music.artist.entity.Artist;

import java.util.*;

@ApplicationScoped
public class ArtistRepository {

    private final Set<Artist> artists = new HashSet<>();

    public Optional<Artist> find(UUID id) {
        return artists.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public List<Artist> findAll() {
        return new ArrayList<>(artists);
    }

    public void create(Artist artist) {
        if (artists.stream().anyMatch(a -> a.getId().equals(artist.getId()))) {
            throw new IllegalArgumentException("Artist with id " + artist.getId() + " already exists.");
            // this shouldnt happen
        }
        artists.add(artist);
    }

    public void update(Artist artist) {
        artists.removeIf(a -> a.getId().equals(artist.getId()));
        artists.add(artist);
    }

    public void delete(Artist artist) {
        artists.removeIf(a -> a.getId().equals(artist.getId()));
    }
}
