package ar.edu.itba.paw.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;

@Repository
public class UserJdbcDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final static RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(rs.getLong("id") ,rs.getString("email"), rs.getString("password"), rs.getString("name"));

    @Autowired
    public UserJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("users")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public User createUser(String email, String password, String name) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("name", name);
        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new User(id.longValue(), email, password, name);
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