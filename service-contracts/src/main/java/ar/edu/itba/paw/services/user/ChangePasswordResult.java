package ar.edu.itba.paw.services.user;

/**
 * Resultado del intento de cambio de contraseña para un usuario autenticado.
 */
public final class ChangePasswordResult {

    public enum Status {
        SUCCESS,
        CURRENT_PASSWORD_INCORRECT
    }

    private final Status status;

    private ChangePasswordResult(final Status status) {
        this.status = status;
    }

    public static ChangePasswordResult success() {
        return new ChangePasswordResult(Status.SUCCESS);
    }

    public static ChangePasswordResult currentPasswordIncorrect() {
        return new ChangePasswordResult(Status.CURRENT_PASSWORD_INCORRECT);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
