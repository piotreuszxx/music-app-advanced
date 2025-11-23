package music.artist.jsf;

import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import music.artist.dto.PatchArtistRequest;
import music.artist.dto.PutArtistRequest;
import music.artist.dto.GetArtistResponse;
import music.artist.service.ArtistService;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Named("artistForm")
@ViewScoped
@Getter
@Setter
public class ArtistFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ArtistService artistService;

    private String id;
    private String name;
    private String country;
    private LocalDate debutYear;
    private Double height;

    public void init() {
        if (id == null) return;
        try {
            UUID uuid = UUID.fromString(id);
            Optional<GetArtistResponse> dto = artistService.findDto(uuid);
            if (dto.isPresent()) {
                GetArtistResponse a = dto.get();
                this.name = a.getName();
                this.country = a.getCountry();
                this.debutYear = a.getDebutYear();
                this.height = a.getHeight();
            } else {
                FacesContext fc = FacesContext.getCurrentInstance();
                if (fc != null) {
                    try {
                        fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/404.xhtml");
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                try {
                    fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/404.xhtml");
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
    }

    public String save() {
        try {
            if (id != null && !id.isBlank()) {
                UUID uuid = UUID.fromString(id);
                PatchArtistRequest req = PatchArtistRequest.builder()
                        .name(name)
                        .country(country)
                        .debutYear(debutYear)
                        .height(height)
                        .build();
                artistService.patchArtist(uuid, req);
                return "/artists/view.xhtml?id=" + id + "&faces-redirect=true";
            } else {
                UUID uuid = UUID.randomUUID();
                PutArtistRequest req = PutArtistRequest.builder()
                        .name(name)
                        .country(country)
                        .debutYear(debutYear)
                        .height(height)
                        .build();
                artistService.createIfNotExists(uuid, req);
                return "/artists/list.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            return null;
        }
    }
}
