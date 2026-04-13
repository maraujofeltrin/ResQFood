package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserJdbcDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("role") == null ? null : User.Role.valueOf(rs.getString("role")));

    @Autowired
    public UserJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public User createUser(final String email, final String password, final String name, final String phone,
            final User.Role role) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("name", name);
        parameters.put("phone", phone);
        parameters.put("role", role == null ? null : role.name());
        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new User(id.longValue(), email, password, name, phone, role);
    }

    @Override
    public User updateUser(final Long id, final String password, final String name, final String phone,
            final User.Role role) {
        jdbcTemplate.update("UPDATE users SET password = ?, name = ?, phone = ?, role = ? WHERE id = ?",
                password, name, phone, role == null ? null : role.name(), id);
        return findById(id).orElseThrow(() -> new IllegalStateException("User not found after update: " + id));
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return jdbcTemplate.query("SELECT * FROM users WHERE email = ?", USER_ROW_MAPPER, email).stream().findAny();
    }

    @Override
    public Optional<User> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", USER_ROW_MAPPER, id).stream().findAny();
    }
}
