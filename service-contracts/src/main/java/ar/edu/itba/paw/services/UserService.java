package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Client;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.User;
import java.util.Optional;

public interface UserService {
    User createUser(final User user, final Client clientProfile, final Commerce commerceProfile);
    Optional<User> upgradeProvisionalUser(final User user, final Client clientProfile, final Commerce commerceProfile);
    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);
    void updatePassword(final Long userId, final String encodedPassword);
}
