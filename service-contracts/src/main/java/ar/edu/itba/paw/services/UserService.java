package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;
import java.util.Optional;

public interface UserService {

    /** Crea usuario sin teléfono ni rol en BD. */
    default User createUser(final String email, final String password, final String name) {
        return createUser(email, password, name, null, null);
    }

    /** Crea usuario con rol; teléfono opcional (null en BD). */
    default User createUser(final String email, final String password, final String name, final User.Role role) {
        return createUser(email, password, name, null, role);
    }

    /** Crea usuario con todos los campos persistibles de {@code users}. */
    User createUser(String email, String password, String name, String phone, User.Role role);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
