package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceImplTest {

    /** Stores whatever password string it receives — no hashing in unit tests. */
    static class NoOpPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(final CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }

    static class InMemoryUserDao implements UserDao {
        private final Map<Long, User> byId = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();
        private long nextId = 1L;

        @Override
        public User createUser(final String email, final String password, final String name, final String phone, final User.Role role) {
            final User u = new User(nextId++, email, password, name, phone, role);
            byId.put(u.getId(), u);
            byEmail.put(email, u);
            return u;
        }

        @Override
        public User updateUser(final Long id, final String password, final String name, final String phone, final User.Role role) {
            final User current = byId.get(id);
            if (current == null) {
                throw new IllegalStateException("User not found: " + id);
            }
            final User updated = new User(id, current.getEmail(), password, name, phone, role);
            byId.put(id, updated);
            byEmail.put(updated.getEmail(), updated);
            return updated;
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        @Override
        public Optional<User> findById(final Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public void updatePassword(final Long id, final String password) {
            final User current = byId.get(id);
            if (current == null) {
                throw new IllegalStateException("User not found: " + id);
            }
            final User updated = new User(id, current.getEmail(), password, current.getName(), current.getPhone(), current.getRole());
            byId.put(id, updated);
            byEmail.put(updated.getEmail(), updated);
        }
    }

    @Test
    public void createUser_and_findByEmailAndId() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new NoOpPasswordEncoder());

        final User u = svc.createUser("a@b.com", "pw", "Name");
        assertNotNull(u.getId());
        final Optional<User> byEmail = svc.findByEmail("a@b.com");
        assertTrue(byEmail.isPresent());
        assertEquals(u.getId(), byEmail.get().getId());

        final Optional<User> byId = svc.findById(u.getId());
        assertTrue(byId.isPresent());
        assertEquals("Name", byId.get().getName());
    }

    @Test
    public void createUser_withRole_storesRole() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new NoOpPasswordEncoder());

        svc.createUser("c@d.com", "pw", "N", "123", User.Role.COMMERCE);
        final Optional<User> maybe = svc.findByEmail("c@d.com");
        assertTrue(maybe.isPresent());
        assertEquals(User.Role.COMMERCE, maybe.get().getRole());
        assertEquals("123", maybe.get().getPhone());
    }

    @Test
    public void upgradeProvisionalUser_updatesPasswordAndProfile() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new NoOpPasswordEncoder());

        svc.createUser("pending@ex.com", "__RESERVATION_PENDING_PASSWORD__", "Pending", "123", User.Role.CLIENT);

        final Optional<User> upgraded = svc.upgradeProvisionalUser("pending@ex.com", "real-pass", "Real Name", User.Role.COMMERCE);
        assertTrue(upgraded.isPresent());
        assertEquals("real-pass", upgraded.get().getPassword());
        assertEquals("Real Name", upgraded.get().getName());
        assertEquals(User.Role.COMMERCE, upgraded.get().getRole());
        assertEquals("123", upgraded.get().getPhone());
    }
}
