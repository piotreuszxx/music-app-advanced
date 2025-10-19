package music.configuration.observer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;

import java.nio.file.Path;

@ApplicationScoped
public class AvatarDirProducer {

    @Inject
    private ServletContext servletContext;

    @Produces
    @ApplicationScoped
    public Path produceAvatarDir() {
        String avatarParam = servletContext.getInitParameter("avatarDir");
        return Path.of(servletContext.getRealPath("/"), avatarParam);
    }
}
