package pl.edu.pg.eti.music.user.entity;

import pl.edu.pg.eti.music.song.entity.Song;

import java.time.LocalDate;
import java.util.List;

public class User {
    String login;
    LocalDate birthday;
    Role role;
    List<Song> songs;
}
