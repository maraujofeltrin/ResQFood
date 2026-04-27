package ar.edu.itba.paw.services.user;

import java.util.List;
import java.util.Objects;

/**
 * Datos mostrados en la pantalla de configuración de perfil. En esta etapa el
 * contenido es placeholder; etapas posteriores lo alimentarán desde persistencia.
 */
public final class ProfileSettingsOverview {

    private final String fullName;
    private final String phone;
    private final String email;
    /** Archivo estático bajo {@code /images/} para mostrar la foto (dummy hasta avatars persistidos). */
    private final String profileImageFileName;
    private final String selectedLanguageCode;
    private final List<String> languageCodes;

    public ProfileSettingsOverview(
            final String fullName,
            final String phone,
            final String email,
            final String profileImageFileName,
            final String selectedLanguageCode,
            final List<String> languageCodes) {
        this.fullName = Objects.requireNonNull(fullName);
        this.phone = Objects.requireNonNull(phone);
        this.email = Objects.requireNonNull(email);
        this.profileImageFileName = Objects.requireNonNull(profileImageFileName);
        this.selectedLanguageCode = Objects.requireNonNull(selectedLanguageCode);
        this.languageCodes = List.copyOf(languageCodes);
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

    public String getProfileImageFileName() {
        return profileImageFileName;
    }

    public String getSelectedLanguageCode() {
        return selectedLanguageCode;
    }

    public List<String> getLanguageCodes() {
        return languageCodes;
    }
}
