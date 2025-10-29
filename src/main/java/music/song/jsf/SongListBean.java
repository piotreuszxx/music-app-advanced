package music.song.jsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import music.song.entity.Song;
import music.song.service.SongService;

import java.util.List;
import java.util.UUID;

@Named("songList")
@RequestScoped
public class SongListBean {

    @Inject
    SongService songService;

    public List<Song> getSongs() {
        return songService.findAll();
    }

    public String delete(UUID id) {
        if (id != null) {
            songService.deleteWithUnlink(id);
        }
        return "/songs/list.xhtml?faces-redirect=true";
    }
}
