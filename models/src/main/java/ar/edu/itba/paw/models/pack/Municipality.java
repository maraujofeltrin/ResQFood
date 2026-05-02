package ar.edu.itba.paw.models.pack;

/**
 * Whitelist of municipalities (partidos) from Buenos Aires, Argentina.
 * <p>
 * Each constant maps to the exact {@code city} value stored in the
 * {@code commerces} table.  Using an enum as a strict whitelist
 * prevents user-supplied values from reaching SQL queries directly,
 * mitigating injection risks.
 * <p>
 * {@link #fromString(String)} acts as a safe converter: any value
 * not matching an enum name returns {@code null}, which the
 * controller interprets as "no location filter".
 */
public enum Municipality {

    CABA("Buenos Aires"),
    AVELLANEDA("Avellaneda"),
    LANUS("Lanús"),
    LOMAS_DE_ZAMORA("Lomas de Zamora"),
    QUILMES("Quilmes"),
    MORON("Morón"),
    LA_MATANZA("La Matanza"),
    VICENTE_LOPEZ("Vicente López"),
    SAN_ISIDRO("San Isidro"),
    SAN_MARTIN("San Martín"),
    TRES_DE_FEBRERO("Tres de Febrero"),
    TIGRE("Tigre"),
    PILAR("Pilar"),
    EZEIZA("Ezeiza"),
    FLORENCIO_VARELA("Florencio Varela"),
    BERAZATEGUI("Berazategui"),
    SAN_FERNANDO("San Fernando"),
    LA_PLATA("La Plata");

    private final String cityName;

    Municipality(String cityName) {
        this.cityName = cityName;
    }

    /** Returns the display / DB city name (e.g. "Vicente López"). */
    public String getCityName() {
        return cityName;
    }

    /**
     * Safely converts a user-supplied string to a {@code Municipality}.
     * Returns {@code null} when the value does not match any constant,
     * so the caller can treat it as "no filter applied".
     */
    public static Municipality fromString(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Municipality.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Maps a database city name string back to the corresponding Municipality enum.
     * Returns {@code null} if no match is found.
     */
    public static Municipality fromCityName(final String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }
        final String search = cityName.trim();
        for (Municipality m : Municipality.values()) {
            if (m.getCityName().equalsIgnoreCase(search)) {
                return m;
            }
        }
        return null;
    }
}
