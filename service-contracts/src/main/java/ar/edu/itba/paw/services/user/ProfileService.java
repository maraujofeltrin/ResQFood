package ar.edu.itba.paw.services.user;

public interface ProfileService {

    /**
     * Resumen de datos para la vista de perfil. El {@code userId} se acepta para
     * futuras consultas a persistencia; la implementación actual ignora el valor.
     */
    ProfileSettingsOverview getSettingsOverview(long userId);
}
