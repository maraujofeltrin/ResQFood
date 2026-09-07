package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.image.Image;
import java.util.Optional;

public interface ImageDao {
    Image saveImage(byte[] data, String contentType);
    Optional<Image> getImage(long id);
}
