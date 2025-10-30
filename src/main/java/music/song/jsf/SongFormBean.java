package music.song.jsf;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import music.song.entity.Genre;
import music.song.service.SongService;
import music.artist.service.ArtistService;
import music.artist.entity.Artist;
import music.song.dto.PutSongRequest;
import music.song.dto.PatchSongRequest;
import music.song.dto.SongFormDto;

@Named("songForm")
@ViewScoped
@Getter
@Setter
public class SongFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    SongService songService;

    @Inject
    ArtistService artistService;

    private String id; // preselected song id, from viewParam, for editing
    private String artistId; // preselected artist id, from viewParam, for adding new song
    private SongFormDto song = new SongFormDto();

    public void init() {
        if (id != null && !id.isBlank()) {
            try {
                UUID uuid = UUID.fromString(id);
                Optional<music.song.entity.Song> s = songService.find(uuid);
                if (s.isPresent()) {
                    music.song.entity.Song ent = s.get();
                    song.setId(ent.getId());
                    song.setTitle(ent.getTitle());
                    song.setGenre(ent.getGenre());
                    song.setReleaseYear(ent.getReleaseYear());
                    song.setDuration(ent.getDuration());
                    song.setArtist(ent.getArtist());
                }
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

    public String save() {
        UUID artistUuid = null;
        if (song.getArtist() != null && song.getArtist().getId() != null) artistUuid = song.getArtist().getId();

        if (song.getId() == null) {
            UUID newSongId = UUID.randomUUID();
            PutSongRequest putSongRequest = PutSongRequest.builder()
                    .title(song.getTitle())
                    .genre(song.getGenre())
                    .releaseYear(song.getReleaseYear())
                    .duration(song.getDuration())
                    .artistId(artistUuid)
                    .build();
            boolean ok = songService.createWithLinks(putSongRequest, newSongId);
            if (ok) return "/songs/view.xhtml?id=" + newSongId + "&faces-redirect=true";
            else return null;
        } else {
            // update partial
            UUID songId = song.getId();
            PatchSongRequest patchSongRequest = PatchSongRequest.builder()
                    .title(song.getTitle())
                    .genre(song.getGenre())
                    .releaseYear(song.getReleaseYear())
                    .duration(song.getDuration())
                    .artistId(artistUuid)
                    .build();
            boolean ok = songService.updatePartialWithLinks(patchSongRequest, songId);
            if (ok) return "/songs/view.xhtml?id=" + songId + "&faces-redirect=true";
            else return null;
        }
    }
}