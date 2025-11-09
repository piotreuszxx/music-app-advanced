
package music.artist.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import music.artist.entity.Artist;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.GetArtistResponse;
import music.song.dto.GetSongsResponse;
import music.artist.dto.PatchArtistRequest;
import music.song.repository.SongRepository;
import music.artist.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ArtistService {

    private ArtistRepository artistRepository;
    private SongRepository songRepository;

    protected ArtistService() {
    }

    @Inject
    public ArtistService(ArtistRepository artistRepository, SongRepository songRepository) {
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
    }

    public Optional<Artist> find(UUID id) {
        return artistRepository.find(id);
    }

    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    // DTO helpers
    public List<GetArtistsResponse.Artist> findAllDtos() {
        var list = findAll();
        return list.stream().map(a -> GetArtistsResponse.Artist.builder()
                .id(a.getId())
                .name(a.getName())
                .build()).toList();
    }

    public Optional<GetArtistResponse> findDto(UUID id) {
        return find(id).map(a -> {
            GetArtistResponse dto = new GetArtistResponse();
            dto.setId(a.getId());
            dto.setName(a.getName());
            dto.setCountry(a.getCountry());
            dto.setDebutYear(a.getDebutYear());
            dto.setHeight(a.getHeight());
            // map songs as small DTOs
            if (a.getSongs() != null) {
                dto.setSongs(a.getSongs().stream().map(s -> {
                    var small = new GetSongsResponse.Song();
                    small.setId(s.getId());
                    small.setTitle(s.getTitle());
                    return small;
                }).toList());
            }
            return dto;
        });
    }

    public void create(Artist artist) {
        artistRepository.create(artist);
    }

    /**
     * Create an artist with provided id and request data if not exists.
     * Returns true when created, false if artist already exists.
     */
    public boolean createIfNotExists(UUID id, music.artist.dto.PutArtistRequest req) {
        if (find(id).isPresent()) return false;
        Artist a = Artist.builder()
                .id(id)
                .name(req.getName())
                .country(req.getCountry())
                .debutYear(req.getDebutYear())
                .height(req.getHeight())
                .build();
        create(a);
        return true;
    }

    public void update(Artist artist) {
        artistRepository.update(artist);
    }

    public void delete(UUID id) {
        artistRepository.find(id).ifPresent(artistRepository::delete);
    }

    /**
     * Apply partial update (patch) to an artist. Returns true if artist existed and was updated.
     */
    public boolean patchArtist(UUID id, PatchArtistRequest req) {
        return find(id).map(artist -> {
            if (req.getName() != null) artist.setName(req.getName());
            if (req.getCountry() != null) artist.setCountry(req.getCountry());
            if (req.getDebutYear() != null) artist.setDebutYear(req.getDebutYear());
            if (req.getHeight() != null) artist.setHeight(req.getHeight());
            update(artist);
            return true;
        }).orElse(false);
    }

    /**
     * Delete artist and all their songs using repositories (no cross-service injection).
     */
    public boolean deleteArtistWithSongs(UUID artistId) {
        return find(artistId).map(artist -> {
            var songs = songRepository.findByArtist(artistId);
            for (var s : songs) {
                songRepository.delete(s);
            }
            delete(artistId);
            return true;
        }).orElse(false);
    }

    public int deleteAllArtistsWithSongs() {
        List<Artist> all = findAll();
        if (all.isEmpty()) return 0;
        int count = 0;
        for (Artist a : all) {
            if (a.getId() != null) {
                var songs = songRepository.findByArtist(a.getId());
                for (var s : songs) songRepository.delete(s);
                delete(a.getId());
                count++;
            }
        }
        return count;
    }
}
