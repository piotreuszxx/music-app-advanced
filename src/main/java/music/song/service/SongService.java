
package music.song.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import music.song.entity.Song;
import music.song.repository.SongRepository;
import music.song.dto.PutSongRequest;
import music.song.dto.PatchSongRequest;
import music.artist.service.ArtistService;
import music.user.service.UserService;

import java.util.*;

@ApplicationScoped
public class SongService {

    private SongRepository songRepository;
    private ArtistService artistService;
    private UserService userService;

    protected SongService() {
    }

    @Inject
    public SongService(SongRepository songRepository, ArtistService artistService, UserService userService) {
        this.songRepository = songRepository;
        this.artistService = artistService;
        this.userService = userService;
    }

    public Optional<Song> find(UUID id) {
        return songRepository.find(id);
    }

    public List<Song> findAll() {
        return songRepository.findAll();
    }

    public List<Song> findByArtist(UUID artistId) {
        return songRepository.findByArtist(artistId);
    }

    public void create(Song song) {
        songRepository.create(song);
    }

    public void update(Song song) {
        songRepository.update(song);
    }

    public void delete(UUID id) {
        songRepository.find(id).ifPresent(songRepository::delete);
    }

    public boolean createWithLinks(PutSongRequest request, UUID uuid) {
        if (songRepository.find(uuid).isPresent()) return false;
        Song song = Song.builder()
                .id(uuid)
                .title(request.getTitle())
                .genre(request.getGenre())
                .releaseYear(request.getReleaseYear())
                .duration(request.getDuration())
                .build();

        // attach artist object if provided
        if (request.getArtistId() != null) {
            artistService.find(request.getArtistId()).ifPresent(artist -> {
                song.setArtist(artist);
            });
        }

        // attach user object if provided
        if (request.getUserId() != null) {
            userService.find(request.getUserId()).ifPresent(user -> {
                song.setUser(user);
            });
        }

        // persist song first
        songRepository.create(song);

        // link to artist (add Song object to artist.songs)
        if (song.getArtist() != null) {
            var artist = song.getArtist();
            if (artist.getSongs() == null) artist.setSongs(new ArrayList<>());
            artist.getSongs().add(song);
            artistService.update(artist);
        }

        // link to user (add Song object to user.songs)
        if (song.getUser() != null) {
            var user = song.getUser();
            if (user.getSongs() == null) user.setSongs(new ArrayList<>());
            user.getSongs().add(song);
            userService.update(user);
        }

        return true;
    }

    public boolean updatePartialWithLinks(PatchSongRequest request, UUID uuid) {
        return songRepository.find(uuid).map(song -> {
            if (request.getTitle() != null) song.setTitle(request.getTitle());
            if (request.getGenre() != null) song.setGenre(request.getGenre());
            if (request.getReleaseYear() != null) song.setReleaseYear(request.getReleaseYear());
            if (request.getDuration() != null) song.setDuration(request.getDuration());
            // re-link artist
            if (request.getArtistId() != null) {
                var oldArtist = song.getArtist();
                if (oldArtist != null) {
                    oldArtist.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                    artistService.update(oldArtist);
                }
                artistService.find(request.getArtistId()).ifPresent(newArtist -> {
                    if (newArtist.getSongs() == null) newArtist.setSongs(new ArrayList<>());
                    newArtist.getSongs().add(song);
                    artistService.update(newArtist);
                    song.setArtist(newArtist);
                });
            }

            // re-link user
            if (request.getUserId() != null) {
                var oldUser = song.getUser();
                if (oldUser != null) {
                    if (oldUser.getSongs() != null) oldUser.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                    userService.update(oldUser);
                }
                userService.find(request.getUserId()).ifPresent(newUser -> {
                    if (newUser.getSongs() == null) newUser.setSongs(new ArrayList<>());
                    newUser.getSongs().add(song);
                    userService.update(newUser);
                    song.setUser(newUser);
                });
            }

            songRepository.update(song);
            return true;
        }).orElse(false);
    }

    public void deleteWithUnlink(UUID uuid) {
        songRepository.find(uuid).ifPresent(song -> {
            var artist = song.getArtist();
            var user = song.getUser();
            if (artist != null) {
                if (artist.getSongs() != null) artist.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                artistService.update(artist);
            }
            if (user != null) {
                if (user.getSongs() != null) user.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                userService.update(user);
            }
            songRepository.delete(song);
        });
    }
}
