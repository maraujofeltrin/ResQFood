package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Client;
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
public class ClientJdbcDao implements ClientDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<Client> CLIENT_ROW_MAPPER = (rs, rowNum) -> new Client(
            rs.getLong("user_id"),
            rs.getString("name"),
            rs.getString("last_name"),
            rs.getObject("notifications_visibility_preferences", Boolean.class)
    );

    @Autowired
    public ClientJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("clients");
    }

    @Override
    public Client createClient(final Long userId, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", userId);
        parameters.put("name", name);
        parameters.put("last_name", lastName);
        parameters.put("notifications_visibility_preferences", notificationsVisibilityPreferences);
        simpleJdbcInsert.execute(parameters);
        return new Client(userId, name, lastName, notificationsVisibilityPreferences);
    }

    @Override
    public Optional<Client> findByUserId(final Long userId) {
        return jdbcTemplate.query("SELECT * FROM clients WHERE user_id = ?", CLIENT_ROW_MAPPER, userId)
                .stream()
                .findAny();
    }

    @Override
    public Client update(final Client client) {
        jdbcTemplate.update(
                "UPDATE clients SET name = ?, last_name = ?, notifications_visibility_preferences = ? WHERE user_id = ?",
                client.getName(),
                client.getLastName(),
                client.getNotificationsVisibilityPreferences(),
                client.getUserId()
        );
        return client;
    }
}

