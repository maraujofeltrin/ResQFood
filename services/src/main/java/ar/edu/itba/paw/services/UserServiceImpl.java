package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final String RESERVATION_USER_PLACEHOLDER_PASSWORD = "__RESERVATION_PENDING_PASSWORD__";

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(final String email, final String password, final String name) {
        return userDao.createUser(email, passwordEncoder.encode(password), name, null, null);
    }

    @Override
    public User createUser(final String email, final String password, final String name, final String phone, final User.Role role) {
        return userDao.createUser(email, passwordEncoder.encode(password), name, phone, role);
    }

    @Override
    public Optional<User> upgradeProvisionalUser(final String email, final String password, final String name,
            final User.Role role) {
        final Optional<User> maybeUser = userDao.findByEmail(email);
        if (!maybeUser.isPresent()) {
            return Optional.empty();
        }

        final User existing = maybeUser.get();
        final String storedPassword = existing.getPassword();
        final boolean isProvisional = RESERVATION_USER_PLACEHOLDER_PASSWORD.equals(storedPassword)
                || passwordEncoder.matches(RESERVATION_USER_PLACEHOLDER_PASSWORD, storedPassword);
        if (!isProvisional) {
            return Optional.empty();
        }

        final User upgraded = userDao.updateUser(existing.getId(), passwordEncoder.encode(password), name,
                existing.getPhone(), role);
        return Optional.of(upgraded);
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }
}
