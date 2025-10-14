package music.user.dto;

import lombok.*;
import music.song.entity.Song;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GetUserResponse {
    private String login;
    private String name;
    private String surname;
    private String email;
    private byte[] avatar;

    /// should it contain a list of songs?
    /// if yes, then of actual Song objects?
    private List<Song> songs;
}
