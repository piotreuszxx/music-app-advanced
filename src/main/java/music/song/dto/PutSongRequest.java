package music.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import music.song.entity.Genre;
import music.validation.NoProfanity;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PutSongRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    @NoProfanity(message = "Title contains forbidden words")
    private String title;
    private Genre genre;
    private LocalDate releaseYear;
    @DecimalMin(value = "0.0", inclusive = false, message = "Duration must be greater than 0")
    private double duration;
    private java.util.UUID artistId;
    private java.util.UUID userId;
}
