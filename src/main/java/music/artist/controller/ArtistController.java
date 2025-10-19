
package music.artist.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.GetArtistResponse;
import music.artist.dto.PatchArtistRequest;
import music.artist.dto.PutArtistRequest;
import music.artist.entity.Artist;
import music.artist.service.ArtistService;
import music.song.service.SongService;
import music.song.dto.GetSongsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class ArtistController {

    private ArtistService service;
    private SongService songService;

    protected ArtistController() {
    }

    @Inject
    public ArtistController(ArtistService service) {
        this.service = service;
    }

    @Inject
    public void setSongService(SongService songService) {
        this.songService = songService;
    }

    public GetArtistsResponse getArtists() {
        return new GetArtistsResponse(service.findAll().stream()
                .map(a -> new GetArtistsResponse.Artist(a.getId(), a.getName()))
                .toList());
    }

    public GetArtistResponse getArtist(UUID id) {
        return service.find(id).map(a -> new GetArtistResponse(
                a.getId(),
                a.getName(),
                a.getCountry(),
                a.getDebutYear(),
                a.getHeight(),
                a.getSongs() == null ? null : a.getSongs().stream()
                        .map(songId -> songService.find(songId)
                                .map(s -> new GetSongsResponse.Song(s.getId(), s.getTitle()))
                                .orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .toList()
        )).orElse(null);
    }

    public boolean createArtist(PutArtistRequest request, UUID uuid) {
        if(service.find(uuid).isEmpty()) {
            Artist artist = Artist.builder()
                    .id(uuid)
                    .name(request.getName())
                    .country(request.getCountry())
                    .debutYear(request.getDebutYear())
                    .height(request.getHeight())
                    .build();
            service.create(artist);
            return true;
        }
        return false;
    }

    public boolean updateArtistPartial(PatchArtistRequest request, UUID uuid) {
        return service.find(uuid).map(artist -> {
            if (request.getName() != null) artist.setName(request.getName());
            if (request.getCountry() != null) artist.setCountry(request.getCountry());
            if (request.getDebutYear() != null) artist.setDebutYear(request.getDebutYear());
            if (request.getHeight() != null) artist.setHeight(request.getHeight());
            service.update(artist);
            return true;
        }).orElse(false);
    }

    public void deleteArtist(UUID uuid) {
        service.delete(uuid);
    }
}
