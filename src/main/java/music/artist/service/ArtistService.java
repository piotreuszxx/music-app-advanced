
package music.artist.service;

import jakarta.ejb.Stateless;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.LocalBean;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import music.artist.entity.Artist;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.GetArtistResponse;
import music.song.dto.GetSongsResponse;
import music.artist.repository.ArtistRepository;
import music.user.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@LocalBean
@Stateless
@NoArgsConstructor(force = true)
public class ArtistService {

    private ArtistRepository artistRepository;

    @Inject
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    public Optional<Artist> find(UUID id) {
        return artistRepository.find(id);
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    // DTO helpers
    @RolesAllowed({Role.ADMIN, Role.USER})
    public List<GetArtistsResponse.Artist> findAllDtos() {
        var list = findAll();
        return list.stream().map(a -> GetArtistsResponse.Artist.builder()
                .id(a.getId())
                .name(a.getName())
                .build()).toList();
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
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

    @RolesAllowed(Role.ADMIN)
    public void create(Artist artist) {
        artistRepository.create(artist);
    }

    @RolesAllowed(Role.ADMIN)
    public void update(Artist artist) {
        artistRepository.update(artist);
    }

    @RolesAllowed(Role.ADMIN)
    public void delete(UUID id) {
        artistRepository.find(id).ifPresent(artistRepository::delete);
    }
}
