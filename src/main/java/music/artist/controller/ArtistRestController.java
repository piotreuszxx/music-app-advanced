package music.artist.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import music.artist.dto.GetArtistResponse;
import music.artist.dto.GetArtistsResponse;
import music.artist.dto.PatchArtistRequest;
import music.artist.dto.PutArtistRequest;
import music.artist.service.ArtistService;
import music.artist.entity.Artist;

import java.net.URI;
import java.util.*;

@Path("/artists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ArtistRestController {

    @Inject
    ArtistService artistService;


    @GET
    public Response getAllArtists() {
        List<GetArtistsResponse.Artist> all = artistService.findAllDtos();
        if(all.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(all).build();
    }

    @GET
    @Path("{id}")
    public Response getArtist(@PathParam("id") UUID id) {
        Optional<GetArtistResponse> dto = artistService.findDto(id);
        if(dto.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(dto.get()).build();
    }

    @PUT
    @Path("{id}")
    public Response createArtist(@PathParam("id") UUID id, PutArtistRequest req, @Context UriInfo uriInfo) {
        boolean created_flag = artistService.createIfNotExists(id, req);
        if (!created_flag) return Response.status(Response.Status.CONFLICT).entity("Artist already exists").build();
        URI created = uriInfo.getAbsolutePath();
        return Response.created(created).build();
    }

    @PATCH
    @Path("{id}")
    public Response updateArtist(@PathParam("id") UUID id, PatchArtistRequest req) {
        boolean ok = artistService.patchArtist(id, req);
        return ok ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteArtist(@PathParam("id") UUID id) {
        boolean deleted = artistService.deleteArtistWithSongs(id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    public Response deleteAllArtistsWithSongs() {
        int deleted = artistService.deleteAllArtistsWithSongs();
        if (deleted == 0) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.noContent().build();
    }
}
