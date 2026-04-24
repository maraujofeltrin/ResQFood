package ar.edu.itba.paw.models.user;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Commerce {
    public enum Category {
        BAKERY,
        RESTAURANT,
        GREENGROCER,
        OTHER
    }

    private final Long userId;
    private String commercialName;
    private Category category;
    private String street;
    private Integer streetNumber;
    private String city;
    private String province;
    private String postalCode;
    private String openingTime;
    private String closingTime;

    public Commerce(Long userId, String commercialName, Category category, String street, Integer streetNumber,
            String city, String province, String postalCode, String openingTime, String closingTime) {
        this.userId = userId;
        this.commercialName = commercialName;
        this.category = category;
        this.street = street;
        this.streetNumber = streetNumber;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public Category getCategory() {
        return category;
    }

    public String getStreet() {
        return street;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    // ── Domain helpers ──────────────────────────────────────────────────

    /**
     * Returns a display-friendly street line, e.g. "Av. Corrientes 1234".
     * Falls back to "—" when both street and number are absent.
     */
    public String getFullStreetLine() {
        final boolean hasStreet = street != null && !street.isBlank();
        final boolean hasNumber = streetNumber != null;
        if (!hasStreet && !hasNumber) {
            return "—";
        }
        if (hasStreet && hasNumber) {
            return street.trim() + " " + streetNumber;
        }
        return hasStreet ? street.trim() : String.valueOf(streetNumber);
    }

    /**
     * Returns "City, Province, PostalCode" omitting blank parts.
     * Falls back to "—" when all parts are absent.
     */
    public String getCityProvincePostal() {
        final List<String> parts = new ArrayList<>(3);
        if (city != null && !city.isBlank()) {
            parts.add(city.trim());
        }
        if (province != null && !province.isBlank()) {
            parts.add(province.trim());
        }
        if (postalCode != null && !postalCode.isBlank()) {
            parts.add(postalCode.trim());
        }
        return parts.isEmpty() ? "—" : String.join(", ", parts);
    }

    /**
     * Returns {@code true} if the commerce is currently open based on its
     * opening/closing hours evaluated against the given time zone.
     */
    public boolean isOpenNow(final ZoneId zone) {
        final Optional<LocalTime> open = parseFlexibleTime(openingTime);
        final Optional<LocalTime> close = parseFlexibleTime(closingTime);
        if (open.isEmpty() || close.isEmpty()) {
            return false;
        }
        final LocalTime o = open.get();
        final LocalTime c = close.get();
        if (o.equals(c)) {
            return false;
        }
        final LocalTime now = LocalTime.now(zone);
        if (!c.isBefore(o)) {
            return !now.isBefore(o) && !now.isAfter(c);
        }
        return !now.isBefore(o) || !now.isAfter(c);
    }

    private static Optional<LocalTime> parseFlexibleTime(final String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        final String v = value.trim();
        final DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                DateTimeFormatter.ofPattern("H:mm", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm", Locale.US),
                DateTimeFormatter.ofPattern("H:mm:ss", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US),
        };
        for (final DateTimeFormatter formatter : formatters) {
            try {
                return Optional.of(LocalTime.parse(v, formatter));
            } catch (final DateTimeParseException ignored) {
                // try next pattern
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Commerce [userId=" + userId + ", commercialName=" + commercialName + ", category=" + category
                + ", street=" + street + ", streetNumber=" + streetNumber + ", city=" + city + ", province="
                + province + ", postalCode=" + postalCode + ", openingTime=" + openingTime + ", closingTime="
                + closingTime + "]";
    }
}
