package music.user.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import music.song.entity.Song;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class User {
    private UUID id;
    private String login;
    private String name;
    private String surname;
    @ToString.Exclude
    private String password;
    private String email;
    private LocalDate birthday;
    private List<Role> roles;
    private List<Song> songs;
}
