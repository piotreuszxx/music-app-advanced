package music.song.jsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import music.song.entity.Song;
import music.song.entity.Genre;
import music.song.service.SongService;
import music.artist.service.ArtistService;
import music.artist.entity.Artist;
import music.song.dto.PutSongRequest;
import music.song.dto.PatchSongRequest;

@Named("songForm")
@ViewScoped
public class SongFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    SongService songService;

    @Inject
    ArtistService artistService;

    private String id; // preselected song id, from viewParam, for editing
    private String artistId; // preselected artist id, from viewParam, for adding new song
    private Song song = new Song();

    public void init() {
        if (id != null && !id.isBlank()) {
            try {
                UUID uuid = UUID.fromString(id);
                Optional<Song> s = songService.find(uuid);
                song = s.orElseGet(Song::new);
            } catch (IllegalArgumentException ignored) {}
        } else if (artistId != null && !artistId.isBlank()) {
            try {
                UUID aid = UUID.fromString(artistId);
                artistService.find(aid).ifPresent(song::setArtist);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public List<Artist> getArtists() {
        return artistService.findAll();
    }

    public Genre[] getGenres() {
        return Genre.values();
    }

    public String getReleaseYearString() {
        if (song.getReleaseYear() == null) return "";
        return song.getReleaseYear().toString();
    }

    public void setReleaseYearString(String s) {
        if (s == null || s.isBlank()) {
            song.setReleaseYear(null);
            return;
        }
        try {
            song.setReleaseYear(LocalDate.parse(s));
        } catch (Exception e) {
            song.setReleaseYear(null);
        }
    }

    public Song getSong() {
        return song;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getArtistId() { return artistId; }
    public void setArtistId(String artistId) { this.artistId = artistId; }

    public String save() {
        UUID artistUuid = null;
        if (song.getArtist() != null && song.getArtist().getId() != null) artistUuid = song.getArtist().getId();

        if (song.getId() == null) {
            UUID newId = UUID.randomUUID();
            PutSongRequest req = PutSongRequest.builder()
                    .title(song.getTitle())
                    .genre(song.getGenre())
                    .releaseYear(song.getReleaseYear())
                    .duration(song.getDuration())
                    .artistId(artistUuid)
                    .build();
            boolean ok = songService.createWithLinks(req, newId);
            if (ok) return "/songs/view.xhtml?id=" + newId + "&faces-redirect=true";
            else return null;
        } else {
            // update partial
            UUID sid = song.getId();
            PatchSongRequest req = PatchSongRequest.builder()
                    .title(song.getTitle())
                    .genre(song.getGenre())
                    .releaseYear(song.getReleaseYear())
                    .duration(song.getDuration())
                    .artistId(artistUuid)
                    .build();
            boolean ok = songService.updatePartialWithLinks(req, sid);
            if (ok) return "/songs/view.xhtml?id=" + sid + "&faces-redirect=true";
            else return null;
        }
    }
}
