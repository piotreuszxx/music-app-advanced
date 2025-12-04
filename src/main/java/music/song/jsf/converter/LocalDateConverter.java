package music.song.jsf.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@FacesConverter("localDateConverter")
public class LocalDateConverter implements Converter<LocalDate> {

    @Override
    public LocalDate getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException ex) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid date format (expected YYYY-MM-DD)", null);
            throw new ConverterException(msg);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalDate value) {
        if (value == null) return "";
        return value.toString();
    }
}
