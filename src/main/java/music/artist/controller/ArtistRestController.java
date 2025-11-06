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
import music.song.service.SongService;
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

    @Inject
    SongService songService;

    @GET
    public List<GetArtistsResponse.Artist> getAllArtists() {
        return artistService.findAllDtos();
    }

    @GET
    @Path("{id}")
    public Response getArtist(@PathParam("id") UUID id) {
        Optional<GetArtistResponse> dto = artistService.findDto(id);
        return dto.map(d -> Response.ok(d).build()).orElse(Response.status(Response.Status.NOT_FOUND).build());
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
        // delete songs first, then artist
        songService.deleteByArtist(id);
        artistService.delete(id);
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteAllArtists_withSongs() {
        List<Artist> all = artistService.findAll();
        for (Artist a : all) {
            if (a.getId() != null) {
                songService.deleteByArtist(a.getId());
                artistService.delete(a.getId());
            }
        }
        return Response.noContent().build();
    }

    @PUT
    public Response replaceAllArtists(List<GetArtistResponse> incoming) {
        Set<UUID> incomingIds = new HashSet<>();
        for (GetArtistResponse g : incoming) {
            UUID id = g.getId();
            if (id == null) id = UUID.randomUUID();
            incomingIds.add(id);

            if (artistService.find(id).isPresent()) {
                // update existing
                artistService.find(id).ifPresent(existing -> {
                    existing.setName(g.getName());
                    existing.setCountry(g.getCountry());
                    existing.setDebutYear(g.getDebutYear());
                    existing.setHeight(g.getHeight() == null ? 0.0 : g.getHeight());
                    artistService.update(existing);
                });
            } else {
                // create new
                Artist a = Artist.builder()
                        .id(id)
                        .name(g.getName())
                        .country(g.getCountry())
                        .debutYear(g.getDebutYear())
                        .height(g.getHeight() == null ? 0.0 : g.getHeight())
                        .build();
                artistService.create(a);
            }
        }

        // delete artists not in incoming
        List<Artist> existing = artistService.findAll();
        for (Artist a : existing) {
            if (a.getId() != null && !incomingIds.contains(a.getId())) {
                songService.deleteByArtist(a.getId());
                artistService.delete(a.getId());
            }
        }

        return Response.noContent().build();
    }
}
