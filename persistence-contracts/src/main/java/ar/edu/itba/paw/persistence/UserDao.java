package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.User;
import java.util.Optional;

public interface UserDao {
    public User createUser(String email, String password, String name);
    public Optional<User> findByEmail(final String email);
    public Optional<User> findById(final Long id);
}