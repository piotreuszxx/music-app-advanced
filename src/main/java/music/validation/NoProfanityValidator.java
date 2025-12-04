package music.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoProfanityValidator implements ConstraintValidator<NoProfanity, String> {

    private Set<String> forbidden;

    @Override
    public void initialize(NoProfanity constraintAnnotation) {
        forbidden = Stream.of("badword", "curse", "swear").map(String::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        String lower = value.toLowerCase();
        for (String b : forbidden) {
            if (lower.contains(b)) return false;
        }
        return true;
    }
}
