package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import java.util.Locale;
import java.util.Optional;

public interface UserService {
    User createUser(final User user, final Client clientProfile, final Commerce commerceProfile);
    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);
    void updatePassword(final Long userId, final String encodedPassword);

    /**
     * Cambia la contraseña tras validar la actual. La nueva contraseña se persiste ya codificada.
     */
    ChangePasswordResult changePassword(long userId, String currentPassword, String newPassword);
    void markVerified(final Long userId);
    RegisterResult tryRegister(final User user, final Client clientProfile, final Commerce commerceProfile);

    /**
     * Persiste una nueva imagen y asocia su id al usuario. Valida tamaño y tipo MIME como en el flujo de packs.
     */
    void updateProfilePhoto(long userId, byte[] data, String contentType);

    /**
     * Elimina la foto de perfil del usuario.
     */
    void removeProfilePhoto(long userId);

    /** Persiste {@code es} o {@code en} como preferencia de idioma (UI y correos). */
    void updatePreferredLocale(long userId, Locale locale);
}
