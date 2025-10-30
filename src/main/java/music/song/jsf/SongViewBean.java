package music.song.jsf;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import music.song.dto.GetSongResponse;
import music.song.service.SongService;
import music.artist.dto.GetArtistResponse;
import music.artist.service.ArtistService;

@Named("songView")
@ViewScoped
@Getter
@Setter
public class SongViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    SongService songService;

    @Inject
    ArtistService artistService;

    private String id;
    private GetSongResponse song;
    private boolean notFound = false;
    private GetArtistResponse artist;

    public void init() {
        if (id != null) {
            try {
                UUID uuid = UUID.fromString(id);
                Optional<GetSongResponse> s = songService.findDto(uuid);
                song = s.orElse(null);
                notFound = (song == null);
                if (song != null && song.getArtistId() != null) {
                    artist = artistService.findDto(song.getArtistId()).orElse(null);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public GetSongResponse getSongDto() {
        return song;
    }

    public GetArtistResponse getArtistDto() {
        return artist;
    }

    public String getArtistName() {
        return artist == null ? "-" : artist.getName();
    }
}
