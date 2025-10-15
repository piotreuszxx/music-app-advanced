package music.user.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class PatchUserRequest {
    private String login;
    private String name;
    private String surname;
    private String email;
    private String password;
    // private byte[] avatar;

    ///  identify songs by their uuids
    // private List<UUID> songs;
}
