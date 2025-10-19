
package music.song.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import music.song.dto.GetSongsResponse;
import music.song.dto.PatchSongRequest;
import music.song.dto.PutSongRequest;
import music.song.entity.Song;
import music.song.entity.Genre;
import music.song.service.SongService;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequestScoped
public class SongController {

    private SongService service;

    protected SongController() {
    }

    @Inject
    public SongController(SongService service) {
        this.service = service;
    }

    public GetSongsResponse getSongs() {
        return new GetSongsResponse(service.findAll().stream()
                .map(s -> new GetSongsResponse.Song(s.getId(), s.getTitle()))
                .toList());
    }

    public Song getSong(UUID id) {
        return service.find(id).orElse(null);
    }

    public boolean createSong(PutSongRequest request, UUID uuid) {
        return service.createWithLinks(request, uuid);
    }

    public boolean updateSongPartial(PatchSongRequest request, UUID uuid) {
        return service.updatePartialWithLinks(request, uuid);
    }

    public void deleteSong(UUID uuid) {
        service.deleteWithUnlink(uuid);
    }
}
