package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.user.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Locale;
import java.util.Optional;

@Primary
@Repository
public class UserJpaDao implements UserDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public User createUser(final String email, final String password, final String name, final String phone, final User.Role role, final Locale locale) {
        final User user = new User(null, email, password, name, phone, role, false, locale, (Image) null);
        em.persist(user);
        return user;
    }

    @Override
    public void updatePassword(final Long id, final String password) {
        final User user = em.find(User.class, id);
        if (user != null) {
            user.setPassword(password);
        }
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return em.createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<User> findById(final Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public void markVerified(final Long userId) {
        final User user = em.find(User.class, userId);
        if (user != null) {
            user.setVerified(true);
        }
    }

    @Override
    public void updateProfileImage(final long userId, final Long imageId) {
        em.flush(); // Ensures the Image is persisted before being referenced by the User
        final User user = em.find(User.class, userId);
        if (user != null) {
            user.setProfileImage(imageId != null ? em.getReference(Image.class, imageId) : null);
        }
    }

    @Override
    public void updateLocale(final long userId, final String languageTag) {
        final User user = em.find(User.class, userId);
        if (user != null) {
            user.setLocale(Locale.forLanguageTag(languageTag));
        }
    }
}
