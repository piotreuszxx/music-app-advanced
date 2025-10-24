package music.artist.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import music.song.entity.Song;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Artist implements Serializable {
    private UUID id;
    private String name;
    private String country;
    private LocalDate debutYear;
    private double height;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Song> songs = new ArrayList<>();
}
