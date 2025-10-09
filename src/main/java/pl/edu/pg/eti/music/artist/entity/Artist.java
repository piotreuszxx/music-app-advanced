package pl.edu.pg.eti.music.artist.entity;

import pl.edu.pg.eti.music.song.entity.Song;

import java.time.LocalDate;
import java.util.List;

public class Artist {
    String name;
    String country;
    LocalDate debutYear;
    double weight;
    List<Song> songs;
}
