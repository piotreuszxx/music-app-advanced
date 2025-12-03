package music.song.dto;

import lombok.*;

import java.util.List;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GetSongsResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Song {
        private UUID id;
        private String title;
        private UUID userId;
        private Date createdAt;
        private Date updatedAt;
    }

    @Singular
    private List<Song> songs;
}
