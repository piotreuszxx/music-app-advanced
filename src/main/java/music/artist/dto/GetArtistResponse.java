package music.artist.dto;

import lombok.*;
import music.song.dto.GetSongsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GetArtistResponse {
    private UUID id;
    private String name;
    private String country;
    private LocalDate debutYear;
    private Double height;

    /// list of artist's songs as small DTOs (id + title)
    private List<GetSongsResponse.Song> songs;
}
