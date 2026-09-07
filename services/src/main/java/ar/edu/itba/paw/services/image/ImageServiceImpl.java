package ar.edu.itba.paw.services.image;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.image.ProfileImageException;
import ar.edu.itba.paw.persistence.ImageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageDao imageDao;

    @Autowired
    public ImageServiceImpl(ImageDao imageDao) {
        this.imageDao = imageDao;
    }

    @Transactional
    @Override
    public Image saveImage(byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            LOGGER.debug("saveImage rejected: empty data");
            throw new ProfileImageException(ProfileImageException.Reason.DATA_EMPTY);
        }
        if (contentType == null || contentType.isEmpty()) {
            LOGGER.debug("saveImage rejected: missing content type");
            throw new ProfileImageException(ProfileImageException.Reason.INVALID_TYPE);
        }
        return imageDao.saveImage(data, contentType);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Image> getImage(long id) {
        return imageDao.getImage(id);
    }
}
