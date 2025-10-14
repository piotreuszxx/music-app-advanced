package music.song.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import music.artist.entity.Artist;
import music.user.entity.User;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class Song implements Serializable {
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    private double duration; // eg. 2.46

    /*@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] coverArt;*/


    private Artist artist;
    private User user;
}
