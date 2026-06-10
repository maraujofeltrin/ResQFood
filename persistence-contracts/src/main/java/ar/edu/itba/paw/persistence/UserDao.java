package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.User;
import java.util.Optional;
import java.util.Locale;

public interface UserDao {

    /** Inserta con locale en español por compatibilidad con el contrato anterior. */
    default User createUser(final String email, final String password, final String name, final String phone,
            final User.Role role) {
        return createUser(email, password, name, phone, role, Locale.forLanguageTag("es"));
    }

    /**
     * Persiste un usuario. Columnas {@code phone} y {@code role} pueden quedar NULL en BD si se pasan nulos.
     */
    User createUser(String email, String password, String name, String phone, User.Role role, Locale locale);

    void updatePassword(final Long id, final String password);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    void markVerified(Long userId);

    void updateProfileImage(long userId, Long imageId);

    void updateLocale(long userId, String languageTag);
}
