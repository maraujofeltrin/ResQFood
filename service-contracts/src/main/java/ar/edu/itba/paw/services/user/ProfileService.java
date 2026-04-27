package ar.edu.itba.paw.services.user;

public interface ProfileService {

    /**
     * Resumen de datos para la vista de perfil a partir del usuario persistido.
     * La imagen mostrada es un placeholder hasta existir avatars en BD.
     */
    ProfileSettingsOverview getSettingsOverview(long userId);
}
