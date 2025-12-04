package music.song.dto;

import java.time.LocalDate;

public class SongFilter {
    private String title;
    private LocalDate createdDate; // filter by createdAt date (date-only)

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}
