package music.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// removed DecimalMin to avoid class cast issues from Number conversion (we validate >0 in bean)
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import music.artist.dto.GetArtistsResponse;
import music.song.entity.Genre;
import music.validation.NoProfanity;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class SongFormDto {
    private UUID id;

    @NotBlank(message = "Title is required")
    @Size(max = 12, message = "Title must be at most 12 characters")
    @NoProfanity(message = "Title contains forbidden words")
    private String title;

    private Genre genre;

    @NotNull(message = "Release year is required")
    private LocalDate releaseYear;

    @NotNull(message = "Duration is required")
    private Double duration;

    private GetArtistsResponse.Artist artist; // small DTO (id + name) for form binding
}
