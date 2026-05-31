package ar.edu.itba.paw.services.security;

/**
 * Thrown when an ownership check targets a resource that does not exist.
 * Mapped to HTTP 404 by the web layer.
 */
public final class OwnershipResourceNotFoundException extends RuntimeException {

    public OwnershipResourceNotFoundException(final String message) {
        super(message);
    }
}
