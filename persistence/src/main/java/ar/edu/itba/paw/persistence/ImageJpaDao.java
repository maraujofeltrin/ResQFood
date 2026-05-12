package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.image.Image;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Primary
@Repository
public class ImageJpaDao implements ImageDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Image saveImage(byte[] data, String contentType) {
        final Image image = new Image(null, data, contentType);
        em.persist(image);
        return image;
    }

    @Override
    public Optional<Image> getImage(long id) {
        return Optional.ofNullable(em.find(Image.class, id));
    }
}
