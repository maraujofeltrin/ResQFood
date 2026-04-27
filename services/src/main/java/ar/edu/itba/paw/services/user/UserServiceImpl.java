package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.security.EmailVerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final ClientDao clientDao;
    private final CommerceDao commerceDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final ClientDao clientDao, final CommerceDao commerceDao,
            final PasswordEncoder passwordEncoder,
            final EmailVerificationTokenService emailVerificationTokenService) {
        this.userDao = userDao;
        this.clientDao = clientDao;
        this.commerceDao = commerceDao;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @Transactional
    @Override
    public User createUser(final User user, final Client clientProfile, final Commerce commerceProfile) {
        final User createdUser = userDao.createUser(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getLocale());

        persistProfileByRole(createdUser, clientProfile, commerceProfile);
        return createdUser;
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

    @Transactional
    @Override
    public ChangePasswordResult changePassword(final long userId, final String currentPassword, final String newPassword) {
        final User user = userDao.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ChangePasswordResult.currentPasswordIncorrect();
        }
        userDao.updatePassword(userId, passwordEncoder.encode(newPassword));
        return ChangePasswordResult.success();
    }

    @Override
    public void markVerified(final Long userId) {
        userDao.markVerified(userId);
    }

    @Transactional
    @Override
    public RegisterResult tryRegister(final User user, final Client clientProfile, final Commerce commerceProfile,
            final String appBaseUrl) {
        final Optional<User> existingUser = findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return RegisterResult.duplicateEmail();
        }
        final User created = createUser(user, clientProfile, commerceProfile);
        emailVerificationTokenService.sendVerificationMail(created.getId(), created.getEmail(), appBaseUrl,
                created.getLocale());
        return RegisterResult.createdPendingVerification(created);
    }
}
