package music.song.dto;

import lombok.*;
import music.song.entity.Genre;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PatchSongRequest {
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    private Double duration;
    private java.util.UUID artistId;
    private java.util.UUID userId;
}
