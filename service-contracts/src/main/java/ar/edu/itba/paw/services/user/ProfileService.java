package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.User;

public interface ProfileService {

    /**
     * Resumen de datos para la vista de perfil a partir del usuario persistido.
     * Con foto persistida, {@code profileImageId} apunta a {@code /images/{id}}; sin foto,
     * se usa un nombre de archivo de placeholder estático.
     */
    ProfileSettingsOverview getSettingsOverview(long userId);

    /**
     * Actualiza datos editables de la cuenta: campos de comercio (solo rol {@link User.Role#COMMERCE})
     * y/o foto de perfil si {@code profilePhoto} es no nulo y no vacío.
     */
    void updateProfileAccount(
            long userId,
            User.Role role,
            String category,
            String street,
            String streetNumber,
            String city,
            String province,
            String postalCode,
            String openingTime,
            String closingTime,
            byte[] profilePhoto,
            String profilePhotoContentType);
}
