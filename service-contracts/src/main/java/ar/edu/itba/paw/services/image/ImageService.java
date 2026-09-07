package ar.edu.itba.paw.services.image;

import ar.edu.itba.paw.models.image.Image;
import java.util.Optional;

public interface ImageService {
    Image saveImage(byte[] data, String contentType);
    Optional<Image> getImage(long id);
}
