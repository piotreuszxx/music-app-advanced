package pl.edu.pg.eti.music.song.entity;

public enum Genre {
    HIPHOP, CLASSICAL, ROCK, POP, METAL;

    String getRoleName() {
        if (this == HIPHOP) {
            return "hiphop";
        } else if (this == ROCK){
            return "rock";
        } else if (this == CLASSICAL){
            return "classical";
        } else if (this == POP){
            return "pop";
        } else if (this == METAL){
            return "metal";
        }
        else return "unknown";
    }
}