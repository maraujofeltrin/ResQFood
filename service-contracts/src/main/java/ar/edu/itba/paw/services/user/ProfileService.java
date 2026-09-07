package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.pack.Municipality;

public interface ProfileService {

    /**
     * Resumen de datos para la vista de perfil a partir del usuario persistido.
     * Con foto persistida, {@code profileImageId} apunta a {@code /images/{id}}; sin foto,
     * se usa un nombre de archivo de placeholder estático.
     */
    ProfileSettingsOverview getSettingsOverview(long userId);

    /**
     * Actualiza datos editables de la cuenta: campos de comercio (solo rol {@link User.Role#COMMERCE})
     * y, opcionalmente, la foto de perfil.
     * <p>
     * Si {@code profilePhoto} tiene contenido, reemplaza la foto existente.
     * Si {@code removePhoto} es {@code true} y no hay foto nueva, elimina la referencia
     * a la imagen de perfil (el usuario queda con el placeholder).
     * Si ambos vienen informados, prevalece la foto nueva.
     */
    void updateProfileAccount(
            long userId,
            User.Role role,
            String category,
            String street,
            String streetNumber,
            Municipality city,
            String province,
            String postalCode,
            String openingTime,
            String closingTime,
            byte[] profilePhoto,
            String profilePhotoContentType,
            boolean removePhoto);

}
