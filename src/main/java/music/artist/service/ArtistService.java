
package music.artist.service;

import jakarta.ejb.Stateless;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.LocalBean;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import music.artist.dto.PatchArtistRequest;
import music.artist.dto.PutArtistRequest;
import music.artist.entity.Artist;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.GetArtistResponse;
import music.song.dto.GetSongsResponse;
import music.artist.repository.ArtistRepository;
import music.song.entity.Song;
import music.song.repository.SongRepository;
import music.user.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import music.configuration.interceptor.binding.LogAccess;

@LocalBean
@Stateless
@NoArgsConstructor(force = true)
public class ArtistService {

    private ArtistRepository artistRepository;

    private SongRepository songRepository;

    @Inject
    public ArtistService(ArtistRepository artistRepository, SongRepository songRepository) {
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
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
        List<Artist> list = findAll();
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
                    GetSongsResponse.Song small = new GetSongsResponse.Song();
                    small.setId(s.getId());
                    small.setTitle(s.getTitle());
                    return small;
                }).toList());
            }
            return dto;
        });
    }

    @RolesAllowed(Role.ADMIN)
    @LogAccess("CREATE_ARTIST")
    public boolean createIfNotExists(UUID id, PutArtistRequest req) {
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

    @RolesAllowed(Role.ADMIN)
    @LogAccess("UPDATE_ARTIST")
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
    @RolesAllowed(Role.ADMIN)
    @LogAccess("DELETE_ARTIST")
    public boolean deleteArtistWithSongs(UUID artistId) {
        return find(artistId).map(artist -> {
            List<Song> songs = songRepository.findByArtist(artistId);
            for (Song s : songs) {
                songRepository.delete(s);
            }
            delete(artistId);
            return true;
        }).orElse(false);
    }

    @RolesAllowed(Role.ADMIN)
    @LogAccess("DELETE_ARTIST")
    public int deleteAllArtistsWithSongs() {
        List<Artist> all = findAll();
        if (all.isEmpty()) return 0;
        int count = 0;
        for (Artist a : all) {
            if (a.getId() != null) {
                List<Song> songs = songRepository.findByArtist(a.getId());
                for (Song s : songs) songRepository.delete(s);
                delete(a.getId());
                count++;
            }
        }
        return count;
    }

    @RolesAllowed(Role.ADMIN)
    @LogAccess("CREATE_ARTIST")
    public void create(Artist artist) {
        artistRepository.create(artist);
    }

    @RolesAllowed(Role.ADMIN)
    @LogAccess("UPDATE_ARTIST")
    public void update(Artist artist) {
        artistRepository.update(artist);
    }

    @RolesAllowed(Role.ADMIN)
    @LogAccess("DELETE_ARTIST")
    public void delete(UUID id) {
        artistRepository.find(id).ifPresent(artistRepository::delete);
    }
}
