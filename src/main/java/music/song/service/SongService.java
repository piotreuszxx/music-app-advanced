
package music.song.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import music.artist.entity.Artist;
import music.artist.repository.ArtistRepository;
import music.song.entity.Song;
import music.song.repository.SongRepository;
import music.song.dto.PutSongRequest;
import music.song.dto.PatchSongRequest;
import music.song.dto.GetSongResponse;
import music.song.dto.GetSongsResponse;
import music.user.entity.User;
import music.user.repository.UserRepository;

import java.util.*;

@ApplicationScoped
public class SongService {

    private SongRepository songRepository;
    private ArtistRepository artistRepository;
    private UserRepository userRepository;

    protected SongService() {
    }

    @Inject
    public SongService(SongRepository songRepository, ArtistRepository artistRepository, UserRepository userRepository) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
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

    public List<GetSongsResponse.Song> findByArtistDtos(UUID artistId) {
        return findByArtist(artistId).stream().map(this::toSmallDto).toList();
    }

    public Optional<GetSongResponse> findDto(UUID id) {
        return find(id).map(this::toFullDto);
    }

    /**
     * Check whether artist exists.
     */
    public boolean artistExists(UUID artistId) {
        if (artistId == null) return false;
        return artistRepository.find(artistId).isPresent();
    }

    public Optional<GetSongResponse> findDtoByArtist(UUID artistId, UUID songId) {
        return findDto(songId).filter(dto -> dto.getArtistId() != null && dto.getArtistId().equals(artistId));
    }

    public boolean updateForArtist(UUID artistId, UUID songId, PatchSongRequest req) {
        Optional<GetSongResponse> s = findDtoByArtist(artistId, songId);
        if (s.isEmpty()) return false;
        req.setArtistId(artistId);
        return updatePartialWithLinks(req, songId);
    }

    public boolean deleteForArtist(UUID artistId, UUID songId) {
        Optional<GetSongResponse> s = findDtoByArtist(artistId, songId);
        if (s.isEmpty()) return false;
        deleteWithUnlink(songId);
        return true;
    }

    private GetSongsResponse.Song toSmallDto(Song s) {
        var r = new GetSongsResponse.Song();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        return r;
    }

    private GetSongResponse toFullDto(Song s) {
        var r = new GetSongResponse();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setGenre(s.getGenre());
        r.setReleaseYear(s.getReleaseYear());
        r.setDuration(s.getDuration());
        r.setArtistId(s.getArtist() == null ? null : s.getArtist().getId());
        r.setUserId(s.getUser() == null ? null : s.getUser().getId());
        return r;
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
            artistRepository.find(request.getArtistId()).ifPresent(artist -> {
                song.setArtist(artist);
            });
        }

        // attach user object if provided
        if (request.getUserId() != null) {
            userRepository.find(request.getUserId()).ifPresent(user -> {
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
            artistRepository.update(artist);
        }

        // link to user (add Song object to user.songs)
        if (song.getUser() != null) {
            var user = song.getUser();
            if (user.getSongs() == null) user.setSongs(new ArrayList<>());
            user.getSongs().add(song);
            userRepository.update(user);
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
                    artistRepository.update(oldArtist);
                }
                artistRepository.find(request.getArtistId()).ifPresent(newArtist -> {
                    if (newArtist.getSongs() == null) newArtist.setSongs(new ArrayList<>());
                    newArtist.getSongs().add(song);
                    artistRepository.update(newArtist);
                    song.setArtist(newArtist);
                });
            }

            // re-link user
            if (request.getUserId() != null) {
                var oldUser = song.getUser();
                if (oldUser != null) {
                    if (oldUser.getSongs() != null) oldUser.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                    userRepository.update(oldUser);
                }
                userRepository.find(request.getUserId()).ifPresent(newUser -> {
                    if (newUser.getSongs() == null) newUser.setSongs(new ArrayList<>());
                    newUser.getSongs().add(song);
                    userRepository.update(newUser);
                    song.setUser(newUser);
                });
            }

            songRepository.update(song);
            return true;
        }).orElse(false);
    }

    public void deleteWithUnlink(UUID uuid) {
        songRepository.find(uuid).ifPresent(song -> {
            // System.out.println("[DEBUG] SongService.deleteWithUnlink: deleting song " + uuid);
            Artist artist = song.getArtist();
            User user = song.getUser();
            if (artist != null) {
                if (artist.getSongs() != null) artist.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                artistRepository.update(artist);
            }
            if (user != null) {
                if (user.getSongs() != null) user.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                userRepository.update(user);
            }
            songRepository.delete(song);
        });
    }

    public void deleteByArtist(UUID artistId) {
        if (artistId == null) return;
        var songs = findByArtist(artistId);
        for (Song s : songs) {
            deleteWithUnlink(s.getId());
        }
    }
}
