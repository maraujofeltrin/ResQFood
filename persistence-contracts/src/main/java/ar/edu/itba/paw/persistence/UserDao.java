package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.User;
import java.util.Optional;

public interface UserDao {
    public default User createUser(String email, String password, String name) {
        return createUser(email, password, name, null, null);
    }

    public User createUser(String email, String password, String name, String phone, User.Role role);
    public Optional<User> findByEmail(final String email);
    public Optional<User> findById(final Long id);
}