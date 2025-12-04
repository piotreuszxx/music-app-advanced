package music.song.jsf.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("durationConverter")
public class DurationConverter implements Converter<Double> {

    private static final String PATTERN = "[0-9]+(\\.[0-6])?";

    @Override
    public Double getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        if (!v.matches(PATTERN)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duration must be an integer or have one fractional digit 0-6 (e.g. 3 or 3.5)", null);
            throw new ConverterException(msg);
        }
        try {
            return Double.valueOf(v);
        } catch (NumberFormatException ex) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duration has invalid numeric format", null);
            throw new ConverterException(msg);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Double value) {
        if (value == null) return "";
        return value.toString();
    }
}
