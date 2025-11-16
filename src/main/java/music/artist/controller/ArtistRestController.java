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
        return Response.ok(dto).build();
    }

    @PUT
    @Path("{id}")
    public Response createArtist(@PathParam("id") UUID id, PutArtistRequest req, @Context UriInfo uriInfo) {
        if (artistService.find(id).isPresent()) {
            return Response.status(Response.Status.CONFLICT).entity("Artist already exists").build();
        }
        Artist a = Artist.builder()
                .id(id)
                .name(req.getName())
                .country(req.getCountry())
                .debutYear(req.getDebutYear())
                .height(req.getHeight())
                .build();
        artistService.create(a);
        URI created = uriInfo.getAbsolutePath();
        return Response.created(created).build();
    }

    @PATCH
    @Path("{id}")
    public Response updateArtist(@PathParam("id") UUID id, PatchArtistRequest req) {
        boolean ok = artistService.find(id).map(artist -> {
            if (req.getName() != null) artist.setName(req.getName());
            if (req.getCountry() != null) artist.setCountry(req.getCountry());
            if (req.getDebutYear() != null) artist.setDebutYear(req.getDebutYear());
            if (req.getHeight() != null) artist.setHeight(req.getHeight());
            artistService.update(artist);
            return true;
        }).orElse(false);
        return ok ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteArtist(@PathParam("id") UUID id) {
        if (artistService.find(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        artistService.delete(id);
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteAllArtistsWithSongs() {
        List<Artist> all = artistService.findAll();
        if(all.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        for (Artist a : all) {
            if (a.getId() != null) {
                artistService.delete(a.getId());
            }
        }
        return Response.noContent().build();
    }
}
