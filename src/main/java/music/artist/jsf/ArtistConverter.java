package music.artist.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import music.artist.entity.Artist;
import music.artist.service.ArtistService;

import java.util.UUID;

@FacesConverter(value = "artistConverter", managed = true)
public class ArtistConverter implements Converter<Artist> {

    @Inject
    ArtistService artistService;

    @Override
    public Artist getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (s == null || s.isBlank()) return null;
        try {
            UUID id = UUID.fromString(s);
            return artistService.find(id).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Artist artist) {
        return artist == null ? "" : (artist.getId() == null ? "" : artist.getId().toString());
    }
}
