package ar.edu.itba.paw.webapp.controller.pack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.services.pack.PackService;

@Controller
public class PackImageController {
    private final PackService packService;
    private final ServletContext servletContext;
    private byte[] placeholderBytes;
    private String placeholderContentType;

    @Autowired
    public PackImageController(final PackService packService, final ServletContext servletContext) {
        this.packService = packService;
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

    @GetMapping("/packs/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> packImage(@PathVariable("id") final long id) {
        final Optional<Pack> packOpt = packService.findImageByPackId(id);
        if (packOpt.isPresent()) {
            final Pack pack = packOpt.get();
            if (pack.getImageData() != null && pack.getImageData().length > 0) {
                String contentType = pack.getImageContentType() != null ? pack.getImageContentType() : "application/octet-stream";
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(pack.getImageData());
            }
        }
        final byte[] placeholder = getPlaceholderBytes();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(placeholderContentType)).body(placeholder);
    }
}
