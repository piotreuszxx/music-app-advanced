package music.song.jsf;

import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
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

    @EJB
    SongService songService;

    @EJB
    ArtistService artistService;

    private String id;
    private GetSongResponse song;
    private boolean notFound = false;
    private GetArtistResponse artist;

    public String delete() {
        if (song == null || song.getId() == null) return null;
        try {
            java.util.UUID uuid = song.getId();
            songService.deleteWithUnlink(uuid);
            return "/songs/list.xhtml?faces-redirect=true";
        } catch (Exception e) {
            return null;
        }
    }

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
                if (notFound) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    if (fc != null) {
                        try {
                            fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/404.xhtml");
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
                notFound = true;
                FacesContext fc = FacesContext.getCurrentInstance();
                if (fc != null) {
                    try {
                        fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/404.xhtml");
                    } catch (Exception ex) {
                    }
                }
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
