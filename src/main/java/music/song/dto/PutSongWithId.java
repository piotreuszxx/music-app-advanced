package music.song.dto;

import lombok.*;
import music.song.entity.Genre;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PutSongWithId {
    private UUID id;
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    private double duration;
    private UUID userId;
}
