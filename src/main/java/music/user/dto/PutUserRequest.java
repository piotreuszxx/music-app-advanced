package music.user.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PutUserRequest {
    private String login;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String password;
    private String email;
    private byte[] avatar;

    ///  identify songs by their uuids
    private List<UUID> songs;
}
