
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
                .artistUuid(request.getArtistId())
                .userUuid(request.getUserId())
                .build();

        // persist song first
        songRepository.create(song);

        // link to artist
        if (request.getArtistId() != null) {
            artistService.find(request.getArtistId()).ifPresent(artist -> {
                if (artist.getSongs() == null) artist.setSongs(new java.util.ArrayList<>());
                artist.getSongs().add(song.getId());
                artistService.update(artist);
            });
        }

        // link to user
        if (request.getUserId() != null) {
            userService.find(request.getUserId()).ifPresent(user -> {
                if (user.getSongs() == null) user.setSongs(new java.util.ArrayList<>());
                user.getSongs().add(song.getId());
                userService.update(user);
            });
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
                UUID oldArtistId = song.getArtistUuid();
                if (oldArtistId != null) {
                    artistService.find(oldArtistId).ifPresent(oldArtist -> oldArtist.getSongs().removeIf(id -> Objects.equals(id, song.getId())));
                }
                artistService.find(request.getArtistId()).ifPresent(newArtist -> {
                    if (newArtist.getSongs() == null) newArtist.setSongs(new ArrayList<>());
                    newArtist.getSongs().add(song.getId());
                    artistService.update(newArtist);
                });
                song.setArtistUuid(request.getArtistId());
            }

            // re-link user
            if (request.getUserId() != null) {
                UUID oldUserId = song.getUserUuid();
                if (oldUserId != null) {
                    userService.find(oldUserId).ifPresent(oldUser -> { if (oldUser.getSongs() != null) oldUser.getSongs().removeIf(id -> Objects.equals(id, song.getId())); });
                }
                userService.find(request.getUserId()).ifPresent(newUser -> {
                    if (newUser.getSongs() == null) newUser.setSongs(new ArrayList<>());
                    newUser.getSongs().add(song.getId());
                    userService.update(newUser);
                });
                song.setUserUuid(request.getUserId());
            }

            songRepository.update(song);
            return true;
        }).orElse(false);
    }

    public void deleteWithUnlink(UUID uuid) {
        songRepository.find(uuid).ifPresent(song -> {
            UUID artistId = song.getArtistUuid();
            UUID userId = song.getUserUuid();
            if (artistId != null) {
                artistService.find(artistId).ifPresent(artist -> {
                    if (artist.getSongs() != null) artist.getSongs().removeIf(id -> Objects.equals(id, song.getId()));
                    artistService.update(artist);
                });
            }
            if (userId != null) {
                userService.find(userId).ifPresent(user -> {
                    if (user.getSongs() != null) user.getSongs().removeIf(id -> Objects.equals(id, song.getId()));
                    userService.update(user);
                });
            }
            songRepository.delete(song);
        });
    }
}
