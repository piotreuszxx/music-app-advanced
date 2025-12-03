package music.song.dto;

import lombok.*;
import music.song.entity.Genre;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GetSongResponse {
    private UUID id;
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    private double duration;
    private UUID artistId;
    private UUID userId;
    private Date createdAt;
    private Date updatedAt;
}
