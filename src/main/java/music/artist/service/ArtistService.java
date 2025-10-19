package music.artist.service;

import music.artist.entity.Artist;
import music.artist.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public Optional<Artist> find(UUID id) {
        return artistRepository.find(id);
    }

    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    public void create(Artist artist) {
        artistRepository.create(artist);
    }

    public void update(Artist artist) {
        artistRepository.update(artist);
    }

    public void delete(UUID id) {
        artistRepository.find(id).ifPresent(artistRepository::delete);
    }
}
