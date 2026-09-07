package ar.edu.itba.paw.services.user;

/**
 * Error al aplicar una actualización de cuenta de perfil; el {@link #getKind()} indica
 * si falló la actualización de datos de comercio o la subida de foto.
 */
public final class ProfileAccountUpdateException extends RuntimeException {

    public enum Kind {
        COMMERCE,
        PHOTO
    }

    private final Kind kind;

    public ProfileAccountUpdateException(final Kind kind, final Throwable cause) {
        super(cause);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }
}
