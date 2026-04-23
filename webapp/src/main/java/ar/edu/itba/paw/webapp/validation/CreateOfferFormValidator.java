package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.webapp.form.CreateOfferForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class CreateOfferFormValidator implements Validator {

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"));

    private final MessageSource messageSource;
    private final ZoneId businessZone;

    @Autowired
    public CreateOfferFormValidator(final MessageSource messageSource, final ZoneId businessZone) {
        this.messageSource = messageSource;
        this.businessZone = businessZone;
    }

    @Override
    public boolean supports(@NonNull final Class<?> clazz) {
        return CreateOfferForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull final Object target, @NonNull final Errors errors) {
        final CreateOfferForm form = (CreateOfferForm) target;
        final Locale locale = LocaleContextHolder.getLocale();
        if (form.getIsAuction()) {
            validateAuctionFields(form, errors, locale);
        } else {
            validatePackFields(form, errors, locale);
        }
        validateImage(form.getImage(), errors, locale);
    }

    public void validatePackModeOnly(@NonNull final Object target, @NonNull final Errors errors) {
        final CreateOfferForm form = (CreateOfferForm) target;
        final Locale locale = LocaleContextHolder.getLocale();
        validatePackFields(form, errors, locale);
        validateImage(form.getImage(), errors, locale);
    }

    private void validatePackFields(final CreateOfferForm form, final Errors errors, final Locale locale) {
        if (form.getFinalPrice() == null) {
            errors.rejectValue("finalPrice", "error.finalPrice",
                    messageSource.getMessage("commerce.createPack.validation.finalPrice.notNull", null, locale));
        } else if (form.getFinalPrice() <= 0) {
            errors.rejectValue("finalPrice", "error.finalPrice",
                    messageSource.getMessage("commerce.createPack.validation.finalPrice.positive", null, locale));
        } else if (form.getOriginalPrice() != null && form.getFinalPrice() > form.getOriginalPrice()) {
            errors.rejectValue("finalPrice", "error.finalPrice",
                    messageSource.getMessage("commerce.createPack.validation.finalPrice.exceedsOriginal", null, locale));
        }

        if (form.getStock() == null) {
            errors.rejectValue("stock", "error.stock",
                    messageSource.getMessage("commerce.createPack.validation.stock.notNull", null, locale));
        } else if (form.getStock() <= 0) {
            errors.rejectValue("stock", "error.stock",
                    messageSource.getMessage("commerce.createPack.validation.stock.positive", null, locale));
        } else if (form.getStock() > 999) {
            errors.rejectValue("stock", "error.stock",
                    messageSource.getMessage("commerce.createPack.validation.stock.max", null, locale));
        }
    }

    private void validateAuctionFields(final CreateOfferForm form, final Errors errors, final Locale locale) {
        if (form.getInitialPrice() == null) {
            errors.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.notNull", null, locale));
        } else if (form.getInitialPrice() <= 0) {
            errors.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.positive", null, locale));
        } else if (form.getOriginalPrice() != null && form.getInitialPrice() > form.getOriginalPrice()) {
            errors.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.exceedsOriginal", null, locale));
        }

        if (form.getEndDate() == null || form.getEndDate().isBlank()) {
            errors.rejectValue("endDate", "error.endDate",
                    messageSource.getMessage("commerce.createAuction.validation.endDate.notEmpty", null, locale));
        }
        if (form.getEndTime() == null || form.getEndTime().isBlank()) {
            errors.rejectValue("endTime", "error.endTime",
                    messageSource.getMessage("commerce.createAuction.validation.endTime.notEmpty", null, locale));
        }

        if (form.getEndDate() != null && !form.getEndDate().isBlank()
                && form.getEndTime() != null && !form.getEndTime().isBlank()) {
            try {
                final LocalDate date = LocalDate.parse(form.getEndDate().trim());
                final LocalTime time = LocalTime.parse(form.getEndTime().trim());
                final ZonedDateTime endZoned = ZonedDateTime.of(date, time, businessZone);
                if (!endZoned.toInstant().isAfter(Instant.now())) {
                    errors.rejectValue("endDate", "error.endDate",
                            messageSource.getMessage("commerce.createAuction.validation.endDateTime.future", null, locale));
                }
            } catch (DateTimeParseException e) {
                errors.rejectValue("endDate", "error.endDate",
                        messageSource.getMessage("commerce.createAuction.validation.endDateTime.invalid", null, locale));
            }
        }
    }

    private void validateImage(final MultipartFile image, final Errors errors, final Locale locale) {
        if (image != null && !image.isEmpty()) {
            final String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                errors.rejectValue("image", "error.image.invalidType",
                        messageSource.getMessage("commerce.createPack.validation.image.invalidType", null, locale));
            }
        }
    }
}
