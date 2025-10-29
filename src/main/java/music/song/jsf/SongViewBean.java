package music.song.jsf;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import music.song.entity.Song;
import music.song.service.SongService;

@Named("songView")
@ViewScoped
public class SongViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    SongService songService;

    private String id;
    private Song song;

    public void init() {
        if (id != null) {
            try {
                UUID uuid = UUID.fromString(id);
                Optional<Song> s = songService.find(uuid);
                song = s.orElse(null);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Song getSong() {
        return song;
    }
}
