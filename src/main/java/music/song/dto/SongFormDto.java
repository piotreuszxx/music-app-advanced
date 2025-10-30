package music.song.dto;

import lombok.Getter;
import lombok.Setter;
import music.artist.entity.Artist;
import music.song.entity.Genre;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class SongFormDto {
    private UUID id;
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    private Double duration;
    private Artist artist; // probably needed to use in artist converter?
}
