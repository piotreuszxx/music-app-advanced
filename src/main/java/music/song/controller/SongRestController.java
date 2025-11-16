package music.song.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
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

    @EJB
    SongService songService;

    @EJB
    ArtistService artistService;

    @GET
    public Response getAllSongsFromArtist(@PathParam("artistId") UUID artistId) {
        if (artistService.find(artistId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<GetSongsResponse.Song> list = songService.findByArtistDtos(artistId);
        if(list.isEmpty()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(list).build();
    }

    @GET
    @Path("{id}")
    public Response getSongFromArtistById(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        GetSongResponse dto = s.get();
        if (dto.getArtistId() == null || !dto.getArtistId().equals(artistId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(dto).build();
    }

    @PUT
    @Path("{id}")
    public Response createSongForArtist(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id, PutSongRequest req, @Context UriInfo uriInfo) {
        if (artistService.find(artistId).isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        req.setArtistId(artistId);
        boolean created_flag = songService.createWithLinks(req, id);
        if (!created_flag)
            return Response.status(Response.Status.CONFLICT).entity("Song already exists").build();
        URI created = uriInfo.getAbsolutePath();
        return Response.created(created).build();
    }

    @PATCH
    @Path("{id}")
    public Response updateSongByArtist(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id, PatchSongRequest req) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        if (s.get().getArtistId() == null || !s.get().getArtistId().equals(artistId))
            return Response.status(Response.Status.NOT_FOUND).build();
        req.setArtistId(artistId);
        boolean updated_flag = songService.updatePartialWithLinks(req, id);
        if(!updated_flag)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.noContent().build();

    }

    @DELETE
    @Path("{id}")
    public Response deleteSong(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        if (s.get().getArtistId() == null || !s.get().getArtistId().equals(artistId))
            return Response.status(Response.Status.NOT_FOUND).build();
        songService.deleteWithUnlink(id);
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteAllForArtist(@PathParam("artistId") UUID artistId) {
        if (artistService.find(artistId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(songService.findByArtistDtos(artistId).isEmpty()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        songService.deleteByArtist(artistId);
        return Response.noContent().build();
    }
}
