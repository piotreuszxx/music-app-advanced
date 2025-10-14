package music.artist.entity;

import lombok.*;
import music.song.entity.Song;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class Artist {
    private UUID id;
    private String name;
    private String country;
    private LocalDate debutYear;
    private double height;
    private List<Song> songs;
}
