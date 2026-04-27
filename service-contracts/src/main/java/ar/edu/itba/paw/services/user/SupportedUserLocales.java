package ar.edu.itba.paw.services.user;

import java.util.List;
import java.util.Locale;

/**
 * Idiomas soportados para preferencia de usuario. Fuente única de verdad
 * para códigos de lenguaje validados y guardados.
 */
public final class SupportedUserLocales {

    public static final String CODE_ES = "es";
    public static final String CODE_EN = "en";

    private static final List<String> CODES = List.of(CODE_ES, CODE_EN);

    private SupportedUserLocales() {
    }

    public static List<String> languageCodes() {
        return CODES;
    }

    /**
     * @throws IllegalArgumentException si el idioma no es uno de {@link #languageCodes()}
     */
    public static void assertSupported(final Locale locale) {
        if (locale == null || locale.getLanguage() == null || locale.getLanguage().isEmpty()) {
            throw new IllegalArgumentException("Locale is required");
        }
        final String lang = locale.getLanguage().toLowerCase(Locale.ROOT);
        if (!CODES.contains(lang)) {
            throw new IllegalArgumentException("Unsupported locale");
        }
    }

    /**
     * @param languageCode p. ej. "es" o "en" (desde formulario, sin exigir mayúsculas)
     * @return {@link Locale} asociado
     * @throws IllegalArgumentException si nulo, vacío o no soportado
     */
    public static Locale toLocaleOrThrow(final String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            throw new IllegalArgumentException("Language code is required");
        }
        final String norm = languageCode.trim().toLowerCase(Locale.ROOT);
        if (!CODES.contains(norm)) {
            throw new IllegalArgumentException("Unsupported language code: " + languageCode);
        }
        if (CODE_EN.equals(norm)) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag(CODE_ES);
    }
}
