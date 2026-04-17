package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.User;
import java.util.Optional;

public interface UserDao {

    /** Inserta en {@code users} con {@code phone} y {@code role} nulos. */
    default User createUser(final String email, final String password, final String name) {
        return createUser(email, password, name, null, null);
    }

    /** Inserta con {@code phone} nulo. */
    default User createUser(final String email, final String password, final String name, final User.Role role) {
        return createUser(email, password, name, null, role);
    }

    /**
     * Persiste un usuario. Columnas {@code phone} y {@code role} pueden quedar NULL en BD si se pasan nulos.
     */
    User createUser(String email, String password, String name, String phone, User.Role role);

    User updateUser(Long id, String password, String name, String phone, User.Role role);

    void updatePassword(final Long id, final String password);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    void markVerified(Long userId);
}
