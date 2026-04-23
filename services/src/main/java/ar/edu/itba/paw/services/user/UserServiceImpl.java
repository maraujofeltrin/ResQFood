package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final ClientDao clientDao;
    private final CommerceDao commerceDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final ClientDao clientDao, final CommerceDao commerceDao,
            final PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.clientDao = clientDao;
        this.commerceDao = commerceDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public User createUser(final User user, final Client clientProfile, final Commerce commerceProfile) {
        final User createdUser = userDao.createUser(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getName(),
                user.getPhone(),
                user.getRole());

        persistProfileByRole(createdUser, clientProfile, commerceProfile);
        return createdUser;
    }

    @Transactional
    @Override
    public Optional<User> upgradeProvisionalUser(final User user, final Client clientProfile,
            final Commerce commerceProfile) {
        final Optional<User> maybeUser = userDao.findByEmail(user.getEmail());
        if (!maybeUser.isPresent()) {
            return Optional.empty();
        }

        final User existing = maybeUser.get();
        final String storedPassword = existing.getPassword();
        final boolean isProvisional = UserPasswordConstants.RESERVATION_PENDING_PASSWORD.equals(storedPassword)
            || passwordEncoder.matches(UserPasswordConstants.RESERVATION_PENDING_PASSWORD, storedPassword);
        if (!isProvisional) {
            return Optional.empty();
        }

        final User upgraded = userDao.updateUser(
                existing.getId(),
                passwordEncoder.encode(user.getPassword()),
                user.getName(),
                hasText(user.getPhone()) ? user.getPhone() : existing.getPhone(),
                user.getRole());

        persistProfileByRole(upgraded, clientProfile, commerceProfile);
        return Optional.of(upgraded);
    }

    private void persistProfileByRole(final User user, final Client clientProfile, final Commerce commerceProfile) {
        if (user.getRole() == User.Role.CLIENT) {
            if (clientProfile == null) {
                throw new IllegalArgumentException("Client profile data is required for CLIENT users");
            }
            final Client clientToPersist = new Client(
                    user.getId(),
                    clientProfile.getName(),
                    clientProfile.getLastName(),
                    clientProfile.getNotificationsVisibilityPreferences());

            clientDao.findByUserId(user.getId())
                    .map(existingClient -> clientDao.update(clientToPersist))
                    .orElseGet(() -> clientDao.createClient(
                            user.getId(),
                            clientProfile.getName(),
                        clientProfile.getLastName(),
                        clientProfile.getNotificationsVisibilityPreferences()));
            return;
        }

        if (user.getRole() == User.Role.COMMERCE) {
            if (commerceProfile == null) {
                throw new IllegalArgumentException("Commerce profile data is required for COMMERCE users");
            }
            final Commerce commerceToPersist = new Commerce(
                    user.getId(),
                    commerceProfile.getCommercialName(),
                    commerceProfile.getCategory(),
                    commerceProfile.getStreet(),
                    commerceProfile.getStreetNumber(),
                    commerceProfile.getCity(),
                    commerceProfile.getProvince(),
                    commerceProfile.getPostalCode(),
                    commerceProfile.getOpeningTime(),
                    commerceProfile.getClosingTime());

            commerceDao.findByUserId(user.getId())
                    .map(existingCommerce -> commerceDao.update(commerceToPersist))
                    .orElseGet(() -> commerceDao.createCommerce(
                            user.getId(),
                            commerceProfile.getCommercialName(),
                            commerceProfile.getCategory(),
                            commerceProfile.getStreet(),
                            commerceProfile.getStreetNumber(),
                            commerceProfile.getCity(),
                            commerceProfile.getProvince(),
                            commerceProfile.getPostalCode(),
                            commerceProfile.getOpeningTime(),
                            commerceProfile.getClosingTime()));
        }
    }

    private static boolean hasText(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }

    @Override
    public void updatePassword(final Long userId, final String encodedPassword) {
        userDao.updatePassword(userId, encodedPassword);
    }

    @Override
    public void markVerified(final Long userId) {
        userDao.markVerified(userId);
    }

    @Transactional
    @Override
    public RegisterResult tryRegister(final User user, final Client clientProfile, final Commerce commerceProfile) {
        final Optional<User> existingUser = findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            final Optional<User> upgraded = upgradeProvisionalUser(user, clientProfile, commerceProfile);
            if (upgraded.isPresent()) {
                return RegisterResult.upgraded(upgraded.get());
            }
            return RegisterResult.duplicateEmail();
        }
        final User created = createUser(user, clientProfile, commerceProfile);
        return RegisterResult.createdPendingVerification(created);
    }
}
