package music.user.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import music.song.entity.Song;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class User implements Serializable {
    private UUID id;
    private String login;
    private String name;
    private String surname;
    @ToString.Exclude
    private String password;
    private String email;
    private LocalDate birthday;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] avatar;

    @ToString.Exclude //It's common to exclude lists from toString
    @EqualsAndHashCode.Exclude
    private List<Role> roles;

    @ToString.Exclude//It's common to exclude lists from toString
    @EqualsAndHashCode.Exclude
    private List<Song> songs;
}
