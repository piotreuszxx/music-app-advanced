
package music.song.service;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.annotation.security.RolesAllowed;
import lombok.NoArgsConstructor;
import music.artist.entity.Artist;
import music.song.dto.*;
import music.song.entity.Song;
import music.song.repository.SongRepository;
import music.artist.service.ArtistService;
import music.user.entity.Role;
import music.user.entity.User;
import music.user.service.UserService;

import java.security.Principal;
import java.util.*;
import java.time.ZoneId;
import java.util.Date;
import music.configuration.interceptor.binding.LogAccess;

@LocalBean
@Stateless
@NoArgsConstructor(force = true)
public class SongService {

    private final SongRepository songRepository;
    private final ArtistService artistService;
    private final UserService userService;

    @Inject
    private SecurityContext securityContext;

    @Inject
    public SongService(SongRepository songRepository, ArtistService artistService, UserService userService) {
        this.songRepository = songRepository;
        this.artistService = artistService;
        this.userService = userService;
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    public Optional<Song> find(UUID id) {
        return songRepository.find(id);
    }

    @RolesAllowed(Role.ADMIN)
    public List<Song> findAll() {
        return songRepository.findAll();
    }

    @RolesAllowed(Role.ADMIN)
    public List<Song> findByArtist(UUID artistId) {
        return songRepository.findByArtist(artistId);
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    public List<GetSongsResponse.Song> findByArtistDtos(UUID artistId) {
        List<Song> songs;
        if (securityContext != null && securityContext.isCallerInRole(Role.ADMIN)) {
            songs = songRepository.findByArtist(artistId);
        } else {
            Principal principal = securityContext.getCallerPrincipal();
            if (principal == null) return List.of();
            String login = principal.getName();
            Optional<User> userOpt = userService.findByLogin(login);
            if (userOpt.isEmpty()) return List.of();
            songs = songRepository.findByArtistAndUser(artistId, userOpt.get().getId());
        }
        return songs.stream().map(this::toSmallDto).toList();
    }

        @RolesAllowed({Role.ADMIN, Role.USER})
        public List<GetSongsResponse.Song> findByArtistDtosWithFilter(UUID artistId, SongFilter filter) {
            if (artistId == null) return List.of();
            List<Song> songs = songRepository.findByArtistWithFilter(artistId, filter);
            if (securityContext != null && securityContext.isCallerInRole(Role.ADMIN)) {
                return songs.stream().map(this::toSmallDto).toList();
            }
            Principal principal = securityContext == null ? null : securityContext.getCallerPrincipal();
            if (principal == null) return List.of();
            Optional<User> userOpt = userService.findByLogin(principal.getName());
            if (userOpt.isEmpty()) return List.of();
            UUID userId = userOpt.get().getId();
            return songs.stream().filter(s -> s.getUser() != null && Objects.equals(s.getUser().getId(), userId)).map(this::toSmallDto).toList();
        }

    @RolesAllowed({Role.ADMIN, Role.USER})
    public Optional<GetSongResponse> findDto(UUID id) {
        Optional<Song> opt = find(id);
        if (opt.isEmpty()) return Optional.empty();
        Song s = opt.get();
        // admin can see any song
        if (securityContext != null && securityContext.isCallerInRole(Role.ADMIN)) {
            return Optional.of(toFullDto(s));
        }
        // otherwise only owner may see
        Principal principal = securityContext == null ? null : securityContext.getCallerPrincipal();
        if (principal == null) return Optional.empty();
        String login = principal.getName();
        Optional<User> userOpt = userService.findByLogin(login);
        if (userOpt.isEmpty()) return Optional.empty();
        if (s.getUser() == null || !Objects.equals(s.getUser().getId(), userOpt.get().getId())) {
            return Optional.empty();
        }
        return Optional.of(toFullDto(s));
    }

    private GetSongsResponse.Song toSmallDto(Song s) {
        GetSongsResponse.Song r = new GetSongsResponse.Song();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setUserId(s.getUser() == null ? null : s.getUser().getId());
        ZoneId displayZone = ZoneId.of("Europe/Warsaw");
        if (s.getCreatedAt() != null) r.setCreatedAt(Date.from(s.getCreatedAt().plusHours(1).atZone(displayZone).toInstant()));
        if (s.getUpdatedAt() != null) r.setUpdatedAt(Date.from(s.getUpdatedAt().plusHours(1).atZone(displayZone).toInstant()));
        return r;
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    public List<Song> findForCurrentUser() {
        if (securityContext != null && securityContext.isCallerInRole(Role.ADMIN)) {
            return songRepository.findAll();
        }
        Principal principal = securityContext == null ? null : securityContext.getCallerPrincipal();
        if (principal == null) return List.of();
        Optional<User> userOpt = userService.findByLogin(principal.getName());
        if (userOpt.isEmpty()) return List.of();
        return songRepository.findByUser(userOpt.get().getId());
    }

    private GetSongResponse toFullDto(Song s) {
        GetSongResponse r = new GetSongResponse();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setGenre(s.getGenre());
        r.setReleaseYear(s.getReleaseYear());
        r.setDuration(s.getDuration());
        r.setArtistId(s.getArtist() == null ? null : s.getArtist().getId());
        r.setUserId(s.getUser() == null ? null : s.getUser().getId());
        ZoneId displayZone = ZoneId.of("Europe/Warsaw");
        if (s.getCreatedAt() != null) r.setCreatedAt(Date.from(s.getCreatedAt().plusHours(1).atZone(displayZone).toInstant()));
        if (s.getUpdatedAt() != null) r.setUpdatedAt(Date.from(s.getUpdatedAt().plusHours(1).atZone(displayZone).toInstant()));
        return r;
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    @LogAccess("CREATE_SONG")
    public void create(Song song) {
        songRepository.create(song);
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    @LogAccess("UPDATE_SONG")
    public void update(Song song) {
        songRepository.update(song);
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    @LogAccess("DELETE_SONG")
    public void delete(UUID id) {
        songRepository.find(id).ifPresent(songRepository::delete);
    }

    @RolesAllowed(Role.ADMIN)
    public boolean createWithLinksInit(PutSongRequest request, UUID uuid) {
        if (songRepository.find(uuid).isPresent()) return false;

        Song song = Song.builder()
                .id(uuid)
                .title(request.getTitle())
                .genre(request.getGenre())
                .releaseYear(request.getReleaseYear())
                .duration(request.getDuration())
                .artist(artistService.find(request.getArtistId()).orElse(null))
                .user(userService.find(request.getUserId()).orElse(null))
                .build();

        // persist song first
        songRepository.create(song);

        // link to artist (add Song object to artist.songs)
        if (song.getArtist() != null) {
            Artist artist = song.getArtist();
            if (artist.getSongs() == null) artist.setSongs(new ArrayList<>());
            artist.getSongs().add(song);
            // artistService.update(artist);
        }

        // link to user (add Song object to user.songs)
        if (song.getUser() != null) {
            User user = song.getUser();
            if (user.getSongs() == null) user.setSongs(new ArrayList<>());
            user.getSongs().add(song);
            // userService.update(user);
        }

        return true;
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    @LogAccess("CREATE_SONG")
    public boolean createWithLinks(PutSongRequest request, UUID uuid) throws Exception {
        if (songRepository.find(uuid).isPresent()) return false;
        Song song = Song.builder()
                .id(uuid)
                .title(request.getTitle())
                .genre(request.getGenre())
                .releaseYear(request.getReleaseYear())
                .duration(request.getDuration())
                .build();
        /// user id is ignored, owner is set to authenticated user if present

        // attach artist object if provided
        if (request.getArtistId() != null) {
            artistService.find(request.getArtistId()).ifPresent(artist -> {
                song.setArtist(artist);
            });
        }

        // attach user object: owner is set from authenticated principal for normal users.
        // If request.userId is provided, only ADMIN is allowed to set owner explicitly.
        if (request.getUserId() != null && (securityContext == null || !securityContext.isCallerInRole(Role.ADMIN))) {
            throw new Exception("Only administrators may set owner on create");
        }

        if (request.getUserId() != null && securityContext != null && securityContext.isCallerInRole(Role.ADMIN)) {
            // admin creating on behalf of another user
            userService.find(request.getUserId()).ifPresent(user -> song.setUser(user));
        } else if (securityContext != null && securityContext.getCallerPrincipal() != null) {
            String login = securityContext.getCallerPrincipal().getName();
            userService.findByLogin(login).ifPresent(user -> song.setUser(user));
        }

        // persist song first
        songRepository.create(song);

        // link to artist (add Song object to artist.songs)
        if (song.getArtist() != null) {
            Artist artist = song.getArtist();
            if (artist.getSongs() == null) artist.setSongs(new ArrayList<>());
            artist.getSongs().add(song);
            // artistService.update(artist);
        }

        // link to user (add Song object to user.songs)
        if (song.getUser() != null) {
            User user = song.getUser();
            if (user.getSongs() == null) user.setSongs(new ArrayList<>());
            user.getSongs().add(song);
            // userService.update(user);
        }

        return true;
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    @LogAccess("UPDATE_SONG")
    public boolean updatePartialWithLinks(PatchSongRequest request, UUID uuid) throws Exception {
        return songRepository.find(uuid).map(song -> {
            // authorization: only ADMIN or owner can update
            if (securityContext != null && !securityContext.isCallerInRole(Role.ADMIN)) {
                Principal principal = securityContext.getCallerPrincipal();
                if (principal == null) return false;
                String login = principal.getName();
                if (song.getUser() == null || !login.equals(song.getUser().getLogin())) return false;
            }
            if (request.getTitle() != null) song.setTitle(request.getTitle());
            if (request.getGenre() != null) song.setGenre(request.getGenre());
            if (request.getReleaseYear() != null) song.setReleaseYear(request.getReleaseYear());
            if (request.getDuration() != null) song.setDuration(request.getDuration());
            // re-link artist
            if (request.getArtistId() != null) {
                Artist oldArtist = song.getArtist();
                if (oldArtist != null) {
                    oldArtist.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                    // artistService.update(oldArtist);
                }
                artistService.find(request.getArtistId()).ifPresent(newArtist -> {
                    if (newArtist.getSongs() == null) newArtist.setSongs(new ArrayList<>());
                    newArtist.getSongs().add(song);
                    // artistService.update(newArtist);
                    song.setArtist(newArtist);
                });
            }

            // re-link user (only admins may change owner). For non-admins attempting to change owner -> forbid
            if (request.getUserId() != null && (securityContext == null || !securityContext.isCallerInRole(Role.ADMIN))) {
                try {
                    throw new Exception("Only administrators may change song owner");
                } catch (Exception e) {
                    // wrap into a RuntimeException so lambda can throw
                    throw new RuntimeException(e);
                }
            }
            if (request.getUserId() != null && securityContext != null && securityContext.isCallerInRole(Role.ADMIN)) {
                User oldUser = song.getUser();
                if (oldUser != null) {
                    if (oldUser.getSongs() != null) oldUser.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                    // userService.update(oldUser);
                }
                userService.find(request.getUserId()).ifPresent(newUser -> {
                    if (newUser.getSongs() == null) newUser.setSongs(new ArrayList<>());
                    newUser.getSongs().add(song);
                    // userService.update(newUser);
                    song.setUser(newUser);
                });
            }

            songRepository.update(song);
            return true;
        }).orElse(false);
    }

    @RolesAllowed({Role.ADMIN, Role.USER})
    @LogAccess("DELETE_SONG")
    public void deleteWithUnlink(UUID uuid) {
        songRepository.find(uuid).ifPresent(song -> {
            // authorization: only ADMIN or owner can delete
            if (securityContext != null && !securityContext.isCallerInRole(Role.ADMIN)) {
                    Principal principal = securityContext.getCallerPrincipal();
                    if (principal == null) throw new RuntimeException("Access denied: not owner");
                    Optional<User> userOpt = userService.findByLogin(principal.getName());
                    if (userOpt.isEmpty()) throw new RuntimeException("Access denied: not owner");
                    if (song.getUser() == null || !Objects.equals(song.getUser().getId(), userOpt.get().getId())) {
                        throw new RuntimeException("Access denied: not owner");
                    }
            }
            // System.out.println("[DEBUG] SongService.deleteWithUnlink: deleting song " + uuid);
            Artist artist = song.getArtist();
            User user = song.getUser();
            if (artist != null) {
                if (artist.getSongs() != null) artist.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                // artistService.update(artist);
            }
            if (user != null) {
                if (user.getSongs() != null) user.getSongs().removeIf(s -> Objects.equals(s.getId(), song.getId()));
                // userService.update(user);
            }
            songRepository.delete(song);
        });
    }

    @RolesAllowed(Role.ADMIN)
    public void deleteByArtist(UUID artistId) {
        if (artistId == null) return;
        List<Song> songs = findByArtist(artistId);
        for (Song s : songs) {
            deleteWithUnlink(s.getId());
        }
    }
}
