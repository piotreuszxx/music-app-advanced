package music.song.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import music.song.dto.*;
import music.song.service.SongService;
import music.artist.service.ArtistService;

import java.net.URI;
import java.util.*;

@Path("/artists/{artistId}/songs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class SongRestController {

    @Inject
    SongService songService;

    @Inject
    ArtistService artistService;

    @GET
    public Response getAllSongsFromArtist(@PathParam("artistId") UUID artistId) {
        if (artistService.find(artistId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<GetSongsResponse.Song> list = songService.findByArtistDtos(artistId);
        return Response.ok(list).build();
    }

    @GET
    @Path("{id}")
    public Response getSongFromArtistById(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty()) return Response.status(Response.Status.NOT_FOUND).build();
        GetSongResponse dto = s.get();
        // ensure it belongs to the artist in path
        if (dto.getArtistId() == null || !dto.getArtistId().equals(artistId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(dto).build();
    }

    @PUT
    @Path("{id}")
    public Response createSong(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id, PutSongRequest req, @Context UriInfo uriInfo) {
        if (artistService.find(artistId).isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        // set artistId from path (ignore body artistId)
        req.setArtistId(artistId);
        boolean created_flag = songService.createWithLinks(req, id);
        if (!created_flag) return Response.status(Response.Status.CONFLICT).entity("Song already exists").build();
        URI created = uriInfo.getAbsolutePath();
        return Response.created(created).build();
    }

    @PATCH
    @Path("{id}")
    public Response updateSong(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id, PatchSongRequest req) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty()) return Response.status(Response.Status.NOT_FOUND).build();
        if (s.get().getArtistId() == null || !s.get().getArtistId().equals(artistId))
            return Response.status(Response.Status.NOT_FOUND).build();
        // don't allow artist in body to override path (but service will re-link only if artistId provided)
        req.setArtistId(artistId);
        boolean ok = songService.updatePartialWithLinks(req, id);
        return ok ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteSong(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty()) return Response.status(Response.Status.NOT_FOUND).build();
        if (s.get().getArtistId() == null || !s.get().getArtistId().equals(artistId)) return Response.status(Response.Status.NOT_FOUND).build();
        songService.deleteWithUnlink(id);
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteAllForArtist(@PathParam("artistId") UUID artistId) {
        if (artistService.find(artistId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        songService.deleteByArtist(artistId);
        return Response.noContent().build();
    }

    @PUT
    public Response replaceAllSongsForArtist(@PathParam("artistId") UUID artistId, List<PutSongWithId> incoming) {
        if (artistService.find(artistId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // existing song ids for artist
        List<GetSongsResponse.Song> existing = songService.findByArtistDtos(artistId);
        Set<UUID> existingIds = new HashSet<>();
        for (GetSongsResponse.Song s : existing)
        {
            if (s.getId() != null)
                existingIds.add(s.getId());
        }

        Set<UUID> incomingIds = new HashSet<>();

        for (PutSongWithId p : incoming) {
            UUID id = p.getId() == null ? UUID.randomUUID() : p.getId(); // create new uuid for song, probably should not be null
            incomingIds.add(id);

            // build PutSongRequest
            PutSongRequest req = PutSongRequest.builder()
                    .title(p.getTitle())
                    .genre(p.getGenre())
                    .releaseYear(p.getReleaseYear())
                    .duration(p.getDuration())
                    .artistId(artistId)
                    .userId(p.getUserId())
                    .build();

            // if exists -> remove and recreate to ensure full replace semantics
            if (songService.find(id).isPresent()) {
                songService.deleteWithUnlink(id);
            }
            songService.createWithLinks(req, id);
        }

        // delete remaining existing songs not present in incoming
        for (UUID ex : existingIds) {
            if (!incomingIds.contains(ex)) songService.deleteWithUnlink(ex);
        }

        return Response.noContent().build();
    }
}
