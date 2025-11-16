package music.user.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import music.user.dto.GetUserResponse;
import music.user.dto.GetUsersResponse;
import music.user.dto.PatchUserRequest;
import music.user.dto.PutUserRequest;
import music.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserRestController {

    @EJB
    UserService userService;

    @GET
    public Response getAllUsers() {
        List<GetUsersResponse.User> all = userService.findAllDtos();
        if (all.isEmpty()) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(new GetUsersResponse(all)).build();
    }

    @PUT
    @Path("{id}")
    public Response createUser(@PathParam("id") UUID id, PutUserRequest req, @Context UriInfo uriInfo) {
        boolean created = userService.createFromRequest(req, id);
        if (!created) return Response.status(Response.Status.CONFLICT).entity("User already exists").build();
        URI createdUri = uriInfo.getAbsolutePath();
        return Response.created(createdUri).build();
    }

    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") UUID id) {
        Optional<GetUserResponse> dto = userService.findDto(id);
        if (dto.isEmpty()) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(dto.get()).build();
    }

    @PATCH
    @Path("{id}")
    public Response updateUserPartial(@PathParam("id") UUID id, PatchUserRequest req) {
        boolean ok = userService.updatePartial(req, id);
        return ok ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteUser(@PathParam("id") UUID id) {
        if (userService.find(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        userService.delete(id);
        return Response.noContent().build();
    }

    // Avatar endpoints
    @GET
    @Path("{id}/avatar")
    @Produces("image/png")
    public Response getUserAvatar(@PathParam("id") UUID id) {
        Optional<byte[]> avatarOpt = userService.getAvatar(id);
        if (avatarOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        byte[] bytes = avatarOpt.get();

        return Response.ok(bytes)
                .type("image/png")
                .header("Content-Length", bytes.length)
                .build();
    }

    @PUT
    @Path("{id}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response putUserAvatar(@PathParam("id") UUID id, @Context HttpServletRequest request) {
        try {
            Part part = request.getPart("avatar");
            if (part == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing form field 'avatar'").build();
            }
            try (InputStream is = part.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                boolean updated = userService.updateAvatar(id, bytes);
                if (!updated) return Response.status(Response.Status.NOT_FOUND).build();
                return Response.noContent().build();
            }
        } catch (IOException | ServletException e) {
            throw new IllegalStateException("Failed to read avatar multipart part", e);
        }
    }

    @DELETE
    @Path("{id}/avatar")
    public Response deleteUserAvatar(@PathParam("id") UUID id) {
        boolean deleted = userService.deleteAvatar(id);
        if (!deleted) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.noContent().build();
    }

}
