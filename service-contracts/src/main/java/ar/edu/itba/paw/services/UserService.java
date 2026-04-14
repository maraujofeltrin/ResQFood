package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;
import java.util.Optional;

public interface UserService {
    User createUser(final String email, final String password, final String name);
    User createUser(final String email, final String password, final String name, final String phone, final User.Role role);
    Optional<User> upgradeProvisionalUser(final String email, final String password, final String name, final User.Role role);
    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);
    void updatePassword(final Long userId, final String encodedPassword);
}
