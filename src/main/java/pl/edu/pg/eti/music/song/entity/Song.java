package pl.edu.pg.eti.music.song.entity;

import pl.edu.pg.eti.music.artist.entity.Artist;

import java.time.LocalDate;

public class Song {
    String title;
    String genre;
    LocalDate releaseYear;
    double duration; // eg. 2.46

    Artist artist;
}
