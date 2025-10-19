package music.artist.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PutArtistRequest {
    private String name;
    private String country;
    private LocalDate debutYear;
    private Double height;
}
