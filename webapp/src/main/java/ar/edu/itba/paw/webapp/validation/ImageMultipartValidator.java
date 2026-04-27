package ar.edu.itba.paw.webapp.validation;

import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class ImageMultipartValidator {

    public static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final MessageSource messageSource;

    public ImageMultipartValidator(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Valida archivo de imagen. Si {@code required} es false, no genera error cuando no hay archivo (flujo oferta).
     */
    public void validate(
            final MultipartFile image,
            @NonNull final Errors errors,
            @NonNull final String field,
            @NonNull final Locale locale,
            final boolean required) {
        if (image == null || image.isEmpty()) {
            if (required) {
                errors.rejectValue(field, "error.image.required",
                        messageSource.getMessage("profile.validation.photo.required", null, locale));
            }
            return;
        }
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            errors.rejectValue(field, "error.image.maxSize",
                    messageSource.getMessage("commerce.createPack.validation.image.maxSize", null, locale));
            return;
        }
        final String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            errors.rejectValue(field, "error.image.invalidType",
                    messageSource.getMessage("commerce.createPack.validation.image.invalidType", null, locale));
        }
    }
}
