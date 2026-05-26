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
    public User updateUser(final Long id, final String password, final String name, final String phone, final User.Role role) {
        em.createQuery("UPDATE User u SET u.password = :pwd, u.name = :n, u.phone = :p, u.role = :r WHERE u.id = :id")
                .setParameter("pwd", password)
                .setParameter("n", name)
                .setParameter("p", phone)
                .setParameter("r", role)
                .setParameter("id", id)
                .executeUpdate();
        final User user = em.find(User.class, id);
        if (user != null) {
            em.flush();
            em.refresh(user);
        }
        return user;
    }

    @Override
    public void updatePassword(final Long id, final String password) {
        em.createQuery("UPDATE User u SET u.password = :pwd WHERE u.id = :id")
                .setParameter("pwd", password)
                .setParameter("id", id)
                .executeUpdate();
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
        em.createQuery("UPDATE User u SET u.verified = true WHERE u.id = :id")
                .setParameter("id", userId)
                .executeUpdate();
    }

    @Override
    public void updateProfileImage(final long userId, final Long imageId) {
        em.flush(); // Ensure pending Image inserts are flushed to the DB to avoid FK violations
        em.createQuery("UPDATE User u SET u.profileImage = :img WHERE u.id = :id")
                .setParameter("img", imageId != null ? em.getReference(Image.class, imageId) : null)
                .setParameter("id", userId)
                .executeUpdate();
    }

    @Override
    public void updateLocale(final long userId, final String languageTag) {
        em.createQuery("UPDATE User u SET u.locale = :loc WHERE u.id = :id")
                .setParameter("loc", Locale.forLanguageTag(languageTag))
                .setParameter("id", userId)
                .executeUpdate();
    }
}
