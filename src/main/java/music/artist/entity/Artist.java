package music.artist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import music.song.entity.Song;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Artist implements Serializable {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private UUID id;

    private String name;
    private String country;
    private LocalDate debutYear;
    private double height;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Song> songs = new ArrayList<>();
}
