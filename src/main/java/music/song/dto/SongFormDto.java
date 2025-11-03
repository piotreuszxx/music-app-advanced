package music.song.dto;

import lombok.Getter;
import lombok.Setter;
import music.artist.dto.GetArtistsResponse;
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
    private GetArtistsResponse.Artist artist; // small DTO (id + name) for form binding
}
