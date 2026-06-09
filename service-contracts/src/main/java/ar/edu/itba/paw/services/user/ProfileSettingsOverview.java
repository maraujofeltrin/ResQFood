package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.notification.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Datos mostrados en la pantalla de perfil, construidos desde el {@link ar.edu.itba.paw.models.user.User}
 * persistido. Si hay {@link #profileImageId}, la URL de la foto es {@code /images/{id}}; si no, {@link #profileImageFileName}.
 * Para rol COMMERCE, {@link #commerce} contiene datos del comercio; para CLIENT es {@code null}.
 */
public final class ProfileSettingsOverview {

    private final String fullName;
    private final String phone;
    private final String email;
    /** Id en tabla {@code images} para {@code /images/{id}}; null si solo placeholder estático. */
    private final Long profileImageId;
    /** Archivo estático bajo {@code /images/} cuando no hay foto persistida (p. ej. placeholder SVG). */
    private final String profileImageFileName;
    private final String selectedLanguageCode;
    private final List<String> languageCodes;
    private final ProfileCommerceSection commerce;
    private final Map<NotificationType, Boolean> mailPreferences;

    public ProfileSettingsOverview(
            final String fullName,
            final String phone,
            final String email,
            final Long profileImageId,
            final String profileImageFileName,
            final String selectedLanguageCode,
            final List<String> languageCodes,
            final ProfileCommerceSection commerce,
            final Map<NotificationType, Boolean> mailPreferences) {
        this.fullName = Objects.requireNonNull(fullName);
        this.phone = Objects.requireNonNull(phone);
        this.email = Objects.requireNonNull(email);
        this.profileImageId = profileImageId;
        this.profileImageFileName = Objects.requireNonNull(profileImageFileName);
        this.selectedLanguageCode = Objects.requireNonNull(selectedLanguageCode);
        this.languageCodes = List.copyOf(languageCodes);
        this.commerce = commerce;
        this.mailPreferences = mailPreferences == null ? Map.of() : Map.copyOf(mailPreferences);
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Long getProfileImageId() {
        return profileImageId;
    }

    public String getProfileImageFileName() {
        return profileImageFileName;
    }

    public String getSelectedLanguageCode() {
        return selectedLanguageCode;
    }

    public List<String> getLanguageCodes() {
        return languageCodes;
    }

    public ProfileCommerceSection getCommerce() {
        return commerce;
    }

    public Map<NotificationType, Boolean> getMailPreferences() {
        return mailPreferences;
    }
}
