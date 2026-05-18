package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Commerce;
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
public class CommerceJdbcDao implements CommerceDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<Commerce> COMMERCE_ROW_MAPPER = (rs, rowNum) -> new Commerce(
            rs.getLong("user_id"),
            rs.getString("commercial_name"),
            rs.getString("category") == null ? null : Commerce.Category.valueOf(rs.getString("category")),
            rs.getString("street"),
            rs.getObject("street_number", Integer.class),
            Municipality.fromCityName(rs.getString("city")),
            rs.getString("province"),
            rs.getString("postal_code"),
            rs.getString("opening_time"),
            rs.getString("closing_time")
    );

    @Autowired
    public CommerceJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("commerces");
    }

    @Override
    public Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final Municipality city, final String province,
            final String postalCode, final String openingTime, final String closingTime) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", userId);
        parameters.put("commercial_name", commercialName);
        parameters.put("category", category == null ? null : category.name());
        parameters.put("street", street);
        parameters.put("street_number", streetNumber);
        parameters.put("city", city == null ? null : city.getCityName());
        parameters.put("province", province);
        parameters.put("postal_code", postalCode);
        parameters.put("opening_time", openingTime);
        parameters.put("closing_time", closingTime);
        simpleJdbcInsert.execute(parameters);
        return new Commerce(userId, commercialName, category, street, streetNumber, city, province, postalCode,
                openingTime, closingTime);
    }

    @Override
    public Optional<Commerce> findByUserId(final Long userId) {
        return jdbcTemplate.query("SELECT * FROM commerces WHERE user_id = ?", COMMERCE_ROW_MAPPER, userId)
                .stream()
                .findAny();
    }

    @Override
    public Commerce update(final Commerce commerce) {
        jdbcTemplate.update(
                "UPDATE commerces SET commercial_name = ?, category = ?, street = ?, street_number = ?, city = ?, province = ?, postal_code = ?, opening_time = ?, closing_time = ? WHERE user_id = ?",
                commerce.getCommercialName(),
                commerce.getCategory() == null ? null : commerce.getCategory().name(),
                commerce.getStreet(),
                commerce.getStreetNumber(),
                commerce.getCity() == null ? null : commerce.getCity().getCityName(),
                commerce.getProvince(),
                commerce.getPostalCode(),
                commerce.getOpeningTime(),
                commerce.getClosingTime(),
                commerce.getUserId()
        );
        return commerce;
    }

    @Override
    public java.util.List<Commerce> filterCommerces(String query, String cityFilter, int page, int pageSize) {
        throw new UnsupportedOperationException("JDBC is deprecated");
    }

    @Override
    public int countFilteredCommerces(String query, String cityFilter) {
        throw new UnsupportedOperationException("JDBC is deprecated");
    }
}

