package music.song.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Song implements Serializable {
    private UUID id;
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    private double duration; // eg. 2.46

    /*@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] coverArt;*/


    private UUID artistUuid;
    private UUID userUuid;
}
