package ar.edu.itba.paw.webapp.controller.image;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.services.image.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class ImageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    @Autowired
    public ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    /** Only numeric path segments match so {@code /images/pack-placeholder.svg} is served as a static file. */
    @GetMapping("/images/{id:\\d+}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("id") final long id) {
        final Optional<Image> imageOpt = imageService.getImage(id);
        if (imageOpt.isPresent()) {
            final Image image = imageOpt.get();
            if (image.getData() != null && image.getData().length > 0) {
                final String contentType =
                        image.getContentType() != null ? image.getContentType() : "application/octet-stream";
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(image.getData());
            }
        }
        LOGGER.debug("Image not found for id {}", id);
        return ResponseEntity.notFound().build();
    }
}
