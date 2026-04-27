package ar.edu.itba.paw.webapp.controller.image;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.services.image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Controller
public class ImageController {

    private final ImageService imageService;
    private final ServletContext servletContext;
    private byte[] placeholderBytes;
    private String placeholderContentType;

    @Autowired
    public ImageController(final ImageService imageService, final ServletContext servletContext) {
        this.imageService = imageService;
        this.servletContext = servletContext;
    }

    private synchronized byte[] getPlaceholderBytes() {
        if (placeholderBytes == null) {
            try (InputStream is = servletContext.getResourceAsStream("/images/pack-placeholder.svg")) {
                if (is != null) {
                    placeholderBytes = is.readAllBytes();
                    placeholderContentType = "image/svg+xml";
                }
            } catch (IOException ignored) {}
            if (placeholderBytes == null) {
                placeholderBytes = new byte[0];
                placeholderContentType = "application/octet-stream";
            }
        }
        return placeholderBytes;
    }

    /** Only numeric path segments match so {@code /images/pack-placeholder.svg} is served as a static file. */
    @GetMapping("/images/{id:\\d+}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("id") final long id) {
        final Optional<Image> imageOpt = imageService.getImage(id);
        if (imageOpt.isPresent()) {
            final Image image = imageOpt.get();
            if (image.getData() != null && image.getData().length > 0) {
                final String contentType = image.getContentType() != null ? image.getContentType() : "application/octet-stream";
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(image.getData());
            }
        }
        final byte[] placeholder = getPlaceholderBytes();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(placeholderContentType)).body(placeholder);
    }
}
