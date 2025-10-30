package music.artist.jsf;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import music.artist.dto.GetArtistResponse;
import music.artist.service.ArtistService;
import music.song.service.SongService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Named("artistView")
@ViewScoped
@Getter
@Setter
public class ArtistViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    ArtistService artistService;

    @Inject
    SongService songService;

    private String id;
    private GetArtistResponse artist;
    private boolean notFound = false;
    private String songToDeleteId;

    public void init() {
        if (id != null) {
            try {
                UUID uuid = UUID.fromString(id);
                Optional<GetArtistResponse> a = artistService.findDto(uuid);
                artist = a.orElse(null);
                notFound = (artist == null);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public GetArtistResponse getArtistDto() {
        return artist;
    }

    public List<?> getSongs() {
        if (id == null) return List.of();
            try {
                UUID aid = UUID.fromString(id);
                return songService.findByArtistDtos(aid);
            } catch (IllegalArgumentException e) {
                return List.of();
            }
    }


    public String deleteSelectedSong() {
        if (songToDeleteId == null) return null;
        try {
            UUID sid = UUID.fromString(songToDeleteId);
            songService.deleteWithUnlink(sid);
        } catch (IllegalArgumentException e) {
        }
        // clear selection and refresh view
        songToDeleteId = null;
        return "/artists/view.xhtml?id=" + id + "&faces-redirect=true";
    }

    public String deleteArtist(String artistId) {
        if (artistId == null) return null;
        try {
            UUID aid = UUID.fromString(artistId);
            // delete songs first
            songService.deleteByArtist(aid);
            artistService.delete(aid);
        } catch (IllegalArgumentException ignored) {}
        return "/artists/list.xhtml?faces-redirect=true";
    }
}
