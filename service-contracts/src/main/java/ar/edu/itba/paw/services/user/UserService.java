package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import java.util.Optional;

public interface UserService {
    User createUser(final User user, final Client clientProfile, final Commerce commerceProfile);
    Optional<User> upgradeProvisionalUser(final User user, final Client clientProfile, final Commerce commerceProfile);
    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);
    void updatePassword(final Long userId, final String encodedPassword);
    void markVerified(final Long userId);

    /**
     * Registers a new user or upgrades a provisional account. Never throws for duplicate email;
     * use {@link RegisterResult#getOutcome()}.
     */
    RegisterResult tryRegister(final User user, final Client clientProfile, final Commerce commerceProfile);
}
