package ar.edu.itba.paw.services.image;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.image.ProfileImageException;
import ar.edu.itba.paw.persistence.ImageDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageDao imageDao;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    void testSaveImageWhenValidReturnsPersistedImage() {
        // 1. Setup
        final byte[] data = new byte[] { 1, 2, 3 };
        final String contentType = "image/png";
        final Image saved = new Image(12L, data, contentType);
        when(imageDao.saveImage(data, contentType)).thenReturn(saved);

        // 2. Ejercicio
        final Image result = imageService.saveImage(data, contentType);

        // 3. Asserts
        assertEquals(12L, result.getId());
        assertArrayEquals(data, result.getData());
        assertEquals(contentType, result.getContentType());
    }

    @Test
    void testSaveImageWhenDataEmptyThrowsProfileImageException() {
        // 1. Setup
        final byte[] data = new byte[0];

        // 2. Ejercicio
        final ProfileImageException ex = assertThrows(ProfileImageException.class,
                () -> imageService.saveImage(data, "image/png"));

        // 3. Asserts
        assertEquals(ProfileImageException.Reason.DATA_EMPTY, ex.getReason());
    }

    @Test
    void testGetImageWhenExistsReturnsOptionalWithImage() {
        // 1. Setup
        final Image image = new Image(7L, new byte[] { 9 }, "image/jpeg");
        when(imageDao.getImage(7L)).thenReturn(Optional.of(image));

        // 2. Ejercicio
        final Optional<Image> result = imageService.getImage(7L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(7L, result.get().getId());
    }
}
