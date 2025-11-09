
package music.artist.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import music.artist.entity.Artist;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.GetArtistResponse;
import music.song.dto.GetSongsResponse;
import music.artist.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@NoArgsConstructor(force = true)
public class ArtistService {

    private ArtistRepository artistRepository;

    @Inject
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
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

    @Transactional
    public void create(Artist artist) {
        artistRepository.create(artist);
    }

    @Transactional
    public void update(Artist artist) {
        artistRepository.update(artist);
    }

    @Transactional
    public void delete(UUID id) {
        artistRepository.find(id).ifPresent(artistRepository::delete);
    }
}
