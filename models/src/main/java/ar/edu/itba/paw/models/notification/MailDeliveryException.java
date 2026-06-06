package ar.edu.itba.paw.models.notification;

/**
 * Thrown when an email delivery operation fails due to infrastructure issues.
 */
public class MailDeliveryException extends RuntimeException {

    public MailDeliveryException(final String message) {
        super(message);
    }

    public MailDeliveryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
