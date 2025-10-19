package music.user.dto;

import lombok.*;
import music.song.dto.GetSongsResponse;

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
    // private byte[] avatar; // too big

    /// list of user's songs as small DTOs (id + title)
    private List<GetSongsResponse.Song> songs;
}
