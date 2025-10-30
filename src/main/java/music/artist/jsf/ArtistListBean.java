package music.artist.jsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import music.artist.dto.GetArtistsResponse;
import music.artist.service.ArtistService;
import music.song.service.SongService;

import java.util.List;
import java.util.UUID;

@Named("artistList")
@RequestScoped
public class ArtistListBean {

    @Inject
    ArtistService artistService;

    @Inject
    SongService songService;

    public List<GetArtistsResponse.Artist> getArtists() {
        return artistService.findAllDtos();
    }

    public String delete(UUID id) {
        if (id != null) {
            // delete songs belonging to artist first
            songService.deleteByArtist(id);
            artistService.delete(id);
        }
        // redirect to the same list page
        return "/artists/list.xhtml?faces-redirect=true";
    }
}
