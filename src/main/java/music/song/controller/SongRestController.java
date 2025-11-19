package music.song.controller;

import jakarta.annotation.security.RolesAllowed;
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
import music.user.entity.Role;

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
    @RolesAllowed({Role.ADMIN, Role.USER})
    public Response getAllSongsFromArtist(@PathParam("artistId") UUID artistId) {
        if (artistService.find(artistId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<GetSongsResponse.Song> list = songService.findByArtistDtos(artistId); // filtering inside
        if(list.isEmpty()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(list).build();
    }

    @GET
    @Path("{id}")
    @RolesAllowed({Role.ADMIN, Role.USER})
    public Response getSongFromArtistById(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id) {
        try {
            Optional<GetSongResponse> s = songService.findDto(id);
            if (s.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();
            GetSongResponse dto = s.get();
            if (dto.getArtistId() == null || !dto.getArtistId().equals(artistId)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(dto).build();
        } catch (Exception ex) {
            Throwable t = ex;
            while (t != null) {
                String msg = t.getMessage();
                if (msg != null && msg.contains("Access denied: not owner")) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
                }
                t = t.getCause();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("{id}")
    @RolesAllowed({Role.ADMIN, Role.USER})
    public Response createSongForArtist(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id, PutSongRequest req, @Context UriInfo uriInfo) {
        if (artistService.find(artistId).isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        req.setArtistId(artistId);
        try {
            boolean created_flag = songService.createWithLinks(req, id);
            if (!created_flag) return Response.status(Response.Status.CONFLICT).entity("Song already exists").build();
            URI created = uriInfo.getAbsolutePath();
            return Response.created(created).build();
        } catch (Exception ex) {
            Throwable t = ex;
            while (t != null) {
                String msg = t.getMessage();
                if (msg != null && msg.contains("Only administrators may set owner")) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Setting owner allowed for " + Role.ADMIN + " only").build();
                }
                t = t.getCause();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @PATCH
    @Path("{id}")
    @RolesAllowed({Role.ADMIN, Role.USER})
    public Response updateSongByArtist(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id, PatchSongRequest req) {
        Optional<GetSongResponse> s = songService.findDto(id);
        if (s.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        if (s.get().getArtistId() == null || !s.get().getArtistId().equals(artistId))
            return Response.status(Response.Status.NOT_FOUND).build();
        req.setArtistId(artistId);
        try {
            boolean updated_flag = songService.updatePartialWithLinks(req, id);
            if(!updated_flag) return Response.status(Response.Status.NOT_FOUND).build();
            return Response.noContent().build();
        } catch (Exception ex) {
            Throwable t = ex;
            while (t != null) {
                String msg = t.getMessage();
                if (msg != null && msg.contains("Only administrators may change song owner")) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Changing owner allowed for " + Role.ADMIN + " only").build();
                }
                t = t.getCause();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

    }

    @DELETE
    @Path("{id}")
    @RolesAllowed({Role.ADMIN, Role.USER})
    public Response deleteSong(@PathParam("artistId") UUID artistId, @PathParam("id") UUID id) {
        try {
            Optional<GetSongResponse> s = songService.findDto(id);
            if (s.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();
            if (s.get().getArtistId() == null || !s.get().getArtistId().equals(artistId))
                return Response.status(Response.Status.NOT_FOUND).build();
            songService.deleteWithUnlink(id);
            return Response.noContent().build();
        } catch (Exception ex) {
            Throwable t = ex;
            while (t != null) {
                String msg = t.getMessage();
                if (msg != null && msg.contains("Access denied: not owner")) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
                }
                t = t.getCause();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @DELETE
    @RolesAllowed({Role.ADMIN})
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
