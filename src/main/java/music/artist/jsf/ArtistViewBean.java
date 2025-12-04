package music.artist.jsf;

import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import music.artist.dto.GetArtistResponse;
import music.artist.service.ArtistService;
import music.song.service.SongService;
import music.song.entity.Genre;

import java.util.List;
import java.util.Optional;
import jakarta.faces.context.FacesContext;
import music.user.entity.Role;
import java.util.UUID;

@Named("artistView")
@ViewScoped
@Getter
@Setter
public class ArtistViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    ArtistService artistService;

    @EJB
    SongService songService;

    private String id;
    private GetArtistResponse artist;
    private boolean notFound = false;
    private String songToDeleteId;
    // filtering fields (title and genre)
    private List<?> songs;
    private String filterTitle;
    private Genre filterGenre;

    public Genre getFilterGenre() {
        return filterGenre;
    }

    public void setFilterGenre(Genre filterGenre) {
        this.filterGenre = filterGenre;
    }

    public Genre[] getGenres() {
        return Genre.values();
    }

    // Explicit getters/setters for EL (avoid relying on Lombok at runtime)
    public String getFilterTitle() {
        return filterTitle;
    }

    public void setFilterTitle(String filterTitle) {
        this.filterTitle = filterTitle;
    }

    public void init() {
        if (id != null) {
            try {
                UUID uuid = UUID.fromString(id);
                Optional<GetArtistResponse> a = artistService.findDto(uuid);
                artist = a.orElse(null);
                notFound = (artist == null);
                songs = null;
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public GetArtistResponse getArtistDto() {
        return artist;
    }

    public List<?> getSongs() {
        if (id == null) return List.of();
        if (songs != null) return songs;
        try {
            UUID aid = UUID.fromString(id);
            List<?> result = songService.findByArtistDtos(aid);
            songs = result;
            return result;
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public void applyFilter() {
        if (id == null) {
            songs = List.of();
            return;
        }
        try {
            UUID aid = UUID.fromString(id);
            music.song.dto.SongFilter filter = new music.song.dto.SongFilter();
            if (filterTitle != null && !filterTitle.isBlank()) filter.setTitle(filterTitle);
            if (filterGenre != null) filter.setGenre(filterGenre);
            List<music.song.dto.GetSongsResponse.Song> dtos = songService.findByArtistDtosWithFilter(aid, filter);
            songs = dtos;
        } catch (IllegalArgumentException e) {
            songs = List.of();
        }
    }


    public void deleteSelectedSong() {
        if (songToDeleteId == null) return;
        try {
            UUID sid = UUID.fromString(songToDeleteId);
            songService.deleteWithUnlink(sid);
            // clear selection and refresh list so getter will reflect removal
            songToDeleteId = null;
            songs = null;
            FacesContext.getCurrentInstance().addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Song deleted", null));
        } catch (IllegalArgumentException e) {
            // ignore invalid id
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error deleting song", e.getMessage()));
        }
    }

    public String deleteArtist(String artistId) {
        if (artistId == null) return null;
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null && !fc.getExternalContext().isUserInRole(Role.ADMIN)) {
                try {
                    fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/403.xhtml");
                } catch (Exception ignored) {}
                return null;
            }
            UUID aid = UUID.fromString(artistId);
            // delete songs first
            songService.deleteByArtist(aid);
            artistService.delete(aid);
        } catch (IllegalArgumentException ignored) {}
        return "/artists/list.xhtml?faces-redirect=true";
    }
}
