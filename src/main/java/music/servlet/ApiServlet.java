package music.servlet;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import music.user.controller.UserController;
import music.user.dto.GetUserResponse;
import music.user.dto.PatchUserRequest;
import music.user.dto.PutUserRequest;
import music.artist.controller.ArtistController;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.PutArtistRequest;
import music.artist.dto.PatchArtistRequest;
import music.song.controller.SongController;
import music.song.dto.GetSongsResponse;
import music.song.dto.PutSongRequest;
import music.song.dto.PatchSongRequest;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(ApiServlet.Paths.API + "/*")
@MultipartConfig(maxFileSize = 200 * 1024)
public class ApiServlet extends HttpServlet {
    public static class Paths {
        public static final String API = "/api";
    }

    private UserController userController;
    private ArtistController artistController;
    private SongController songController;

    String avatarDir;

    @Override
    public void init() {
        this.userController = (UserController) getServletContext().getAttribute("userController");
        this.avatarDir = getServletContext().getInitParameter("avatarDir");
        this.artistController = (ArtistController) getServletContext().getAttribute("artistController");
        this.songController = (SongController) getServletContext().getAttribute("songController");
    }

    /// for PATCH method support
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equals("PATCH")) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = parseRequestPath(request);
        String servletPath = request.getServletPath();

        if (Paths.API.equals(servletPath)) {
            if (path.matches(Patterns.USERS.pattern())) {
                response.setContentType("application/json");
                response.getWriter().write(jsonb.toJson(userController.getUsers()));
                return;

            } else if (path.matches(Patterns.USER.pattern())) {
                UUID uuid = extractUuid(Patterns.USER, path);
                GetUserResponse user = userController.getUser(uuid);
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("application/json");
                response.getWriter().write(jsonb.toJson(user));
                return;

            } else if (path.matches(Patterns.ARTISTS.pattern())) {
                response.setContentType("application/json");
                response.getWriter().write(jsonb.toJson(artistController.getArtists()));
                return;

            } else if (path.matches(Patterns.ARTIST.pattern())) {
                UUID uuid = extractUuid(Patterns.ARTIST, path);
                var artist = artistController.getArtist(uuid);
                if (artist == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("application/json");
                response.getWriter().write(jsonb.toJson(artist));
                return;

            } else if (path.matches(Patterns.SONGS.pattern())) {
                response.setContentType("application/json");
                response.getWriter().write(jsonb.toJson(songController.getSongs()));
                return;

            } else if (path.matches(Patterns.SONG.pattern())) {
                UUID uuid = extractUuid(Patterns.SONG, path);
                var song = songController.getSong(uuid);
                if (song == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("application/json");
                response.getWriter().write(jsonb.toJson(song));
                return;

            } else if (path.matches(Patterns.USER_AVATAR.pattern())) {
                UUID uuid = extractUuid(Patterns.USER_AVATAR, path);
                byte[] portrait = userController.getUserAvatar(uuid);
                if (portrait == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("image/png");
                response.setContentLength(portrait.length);
                response.getOutputStream().write(portrait);
                return;
            }
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = parseRequestPath(request);
        String servletPath = request.getServletPath();
        if (Paths.API.equals(servletPath)) {

            // create user
            if (path.matches(Patterns.USER.pattern())) {
                UUID uuid = extractUuid(Patterns.USER, path);
                boolean created = userController.createUser(jsonb.fromJson(request.getReader(), PutUserRequest.class), uuid);
                if (created) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.setHeader("Location", request.getRequestURL().toString());
                } else {
                    response.sendError(HttpServletResponse.SC_CONFLICT, "User already exists");
                }
                return;
            }

            // upload user avatar
            if (path.matches(Patterns.USER_AVATAR.pattern())) {
                UUID uuid = extractUuid(Patterns.USER_AVATAR, path);
                boolean updated = userController.putUserAvatar(uuid, request.getPart("avatar").getInputStream());
                if (updated) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            // create artist
            if (path.matches(Patterns.ARTIST.pattern())) {
                UUID uuid = extractUuid(Patterns.ARTIST, path);
                boolean created = artistController.createArtist(jsonb.fromJson(request.getReader(), PutArtistRequest.class), uuid);
                if (created) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.setHeader("Location", request.getRequestURL().toString());
                } else {
                    response.sendError(HttpServletResponse.SC_CONFLICT, "Artist already exists");
                }
                return;
            }

            // create song
            if (path.matches(Patterns.SONG.pattern())) {
                UUID uuid = extractUuid(Patterns.SONG, path);
                boolean created = songController.createSong(jsonb.fromJson(request.getReader(), PutSongRequest.class), uuid);
                if (created) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.setHeader("Location", request.getRequestURL().toString());
                } else {
                    response.sendError(HttpServletResponse.SC_CONFLICT, "Song already exists");
                }
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = parseRequestPath(request);

        if (path.matches(Patterns.USER.pattern())) {
            UUID uuid = extractUuid(Patterns.USER, path);
            boolean updated = userController.updateUserPartial(jsonb.fromJson(request.getReader(), PatchUserRequest.class), uuid);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        if (path.matches(Patterns.ARTIST.pattern())) {
            UUID uuid = extractUuid(Patterns.ARTIST, path);
            boolean updated = artistController.updateArtistPartial(jsonb.fromJson(request.getReader(), PatchArtistRequest.class), uuid);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        if (path.matches(Patterns.SONG.pattern())) {
            UUID uuid = extractUuid(Patterns.SONG, path);
            boolean updated = songController.updateSongPartial(jsonb.fromJson(request.getReader(), PatchSongRequest.class), uuid);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = parseRequestPath(request);
        String servletPath = request.getServletPath();
        if (Paths.API.equals(servletPath)) {
            if (path.matches(Patterns.USER.pattern())) {
                UUID uuid = extractUuid(Patterns.USER, path);
                if (userController.getUser(uuid) != null) {
                    userController.deleteUser(uuid);
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else if (path.matches(Patterns.USER_AVATAR.pattern())) {
                UUID uuid = extractUuid(Patterns.USER_AVATAR, path);
                boolean deleted = userController.deleteUserAvatar(uuid);
                if (deleted) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            } else if (path.matches(Patterns.ARTIST.pattern())) {
                UUID uuid = extractUuid(Patterns.ARTIST, path);
                if (artistController.getArtist(uuid) != null) {
                    artistController.deleteArtist(uuid);
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else if (path.matches(Patterns.SONG.pattern())) {
                UUID uuid = extractUuid(Patterns.SONG, path);
                if (songController.getSong(uuid) != null) {
                    songController.deleteSong(uuid);
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    ///  util functions
    private String parseRequestPath(HttpServletRequest request) {
        String path = request.getPathInfo();
        path = path != null ? path : "";
        return path;
    }

    private static UUID extractUuid(Pattern pattern, String path) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            return UUID.fromString(matcher.group(1));
        }
        throw new IllegalArgumentException("No UUID in path.");
    }

    private final Jsonb jsonb = JsonbBuilder.create();

    public static final class Patterns {

        private static final Pattern UUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        public static final Pattern USERS = Pattern.compile("/users/?");

        public static final Pattern USER = Pattern.compile("/users/(%s)".formatted(UUID.pattern()));

        public static final Pattern USER_AVATAR = Pattern.compile("/users/(%s)/avatar".formatted(UUID.pattern()));

        public static final Pattern ARTISTS = Pattern.compile("/artists/?");

        public static final Pattern ARTIST = Pattern.compile("/artists/(%s)".formatted(UUID.pattern()));

        public static final Pattern SONGS = Pattern.compile("/songs/?");

        public static final Pattern SONG = Pattern.compile("/songs/(%s)".formatted(UUID.pattern()));


    }
}