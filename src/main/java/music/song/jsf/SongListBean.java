package music.song.jsf;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import music.song.entity.Song;
import music.song.service.SongService;

import java.util.List;
import java.util.UUID;

@Named("songList")
@RequestScoped
public class SongListBean {

    @EJB
    SongService songService;

    public List<Song> getSongs() {
        return songService.findForCurrentUser();
    }

    public String delete(UUID id) {
        if (id != null) {
            try {
                songService.deleteWithUnlink(id);
                return "/songs/list.xhtml?faces-redirect=true";
            } catch (Exception ignored) {
                // if deletion was forbidden or failed, stay on current page
                return null;
            }
        }
        return "/songs/list.xhtml?faces-redirect=true";
    }
}
