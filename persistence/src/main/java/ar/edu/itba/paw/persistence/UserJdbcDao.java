package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

@Repository
public class UserJdbcDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        final Long profileImageId = rs.getObject("profile_image_id") == null ? null : rs.getLong("profile_image_id");
        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("role") == null ? null : User.Role.valueOf(rs.getString("role")),
                rs.getBoolean("verified"),
                Locale.forLanguageTag(rs.getString("locale")),
                profileImageId);
    };

    @Autowired
    public UserJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public User createUser(final String email, final String password, final String name, final String phone,
            final User.Role role, final Locale locale) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("name", name);
        parameters.put("phone", phone);
        parameters.put("role", role == null ? null : role.name());
        parameters.put("locale", locale.toLanguageTag());
        parameters.put("verified", false);
        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new User(id.longValue(), email, password, name, phone, role, false, locale, null);
    }

    @Override
    public User updateUser(final Long id, final String password, final String name, final String phone,
            final User.Role role) {
        jdbcTemplate.update("UPDATE users SET password = ?, name = ?, phone = ?, role = ? WHERE id = ?",
                password, name, phone, role == null ? null : role.name(), id);
        return findById(id).orElseThrow(() -> new IllegalStateException("User not found after update: " + id));
    }

    @Override
    public void updatePassword(final Long id, final String password) {
        jdbcTemplate.update("UPDATE users SET password = ? WHERE id = ?", password, id);
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return jdbcTemplate.query("SELECT * FROM users WHERE email = ?", USER_ROW_MAPPER, email).stream().findAny();
    }

    @Override
    public Optional<User> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", USER_ROW_MAPPER, id).stream().findAny();
    }

    @Override
    public void markVerified(final Long userId) {
        jdbcTemplate.update("UPDATE users SET verified = true WHERE id = ?", userId);
    }

    @Override
    public void updateProfileImage(final long userId, final Long imageId) {
        jdbcTemplate.update("UPDATE users SET profile_image_id = ? WHERE id = ?", imageId, userId);
    }

    @Override
    public void updateLocale(final long userId, final String languageTag) {
        jdbcTemplate.update("UPDATE users SET locale = ? WHERE id = ?", languageTag, userId);
    }
}
