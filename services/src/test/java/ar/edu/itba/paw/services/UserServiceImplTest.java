package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceImplTest {

    static class InMemoryUserDao implements UserDao {
        private final Map<Long, User> byId = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();
        private long nextId = 1L;

        @Override
        public User createUser(String email, String password, String name, String phone, User.Role role) {
            final User u = new User(nextId++, email, password, name, phone, role);
            byId.put(u.getId(), u);
            byEmail.put(email, u);
            return u;
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }
    }

    @Test
    public void createUser_and_findByEmailAndId() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao);

        final User u = svc.createUser("a@b.com", "pw", "Name");
        assertNotNull(u.getId());
        final var byEmail = svc.findByEmail("a@b.com");
        assertTrue(byEmail.isPresent());
        assertEquals(u.getId(), byEmail.get().getId());

        final var byId = svc.findById(u.getId());
        assertTrue(byId.isPresent());
        assertEquals("Name", byId.get().getName());
    }

    @Test
    public void createUser_withRole_storesRole() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao);

        final User u = svc.createUser("c@d.com", "pw", "N", "123", User.Role.COMMERCE);
        final var maybe = svc.findByEmail("c@d.com");
        assertTrue(maybe.isPresent());
        assertEquals(User.Role.COMMERCE, maybe.get().getRole());
        assertEquals("123", maybe.get().getPhone());
    }
}
