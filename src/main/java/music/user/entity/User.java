package music.user.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;
import java.util.ArrayList;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private List<UUID> songs = new ArrayList<>();
}
