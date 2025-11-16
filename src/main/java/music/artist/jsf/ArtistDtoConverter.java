package music.artist.jsf;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import music.artist.dto.GetArtistsResponse;
import music.artist.service.ArtistService;

import java.util.UUID;

@FacesConverter(value = "artistDtoConverter", managed = true)
public class ArtistDtoConverter implements Converter<GetArtistsResponse.Artist> {

    @EJB
    ArtistService artistService;

    @Override
    public GetArtistsResponse.Artist getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (s == null || s.isBlank()) return null;
        try {
            UUID id = UUID.fromString(s);
            return artistService.findDto(id).map(a -> new GetArtistsResponse.Artist(a.getId(), a.getName())).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, GetArtistsResponse.Artist artist) {
        if (artist == null) return "";
        return artist.getId() == null ? "" : artist.getId().toString();
    }
}
