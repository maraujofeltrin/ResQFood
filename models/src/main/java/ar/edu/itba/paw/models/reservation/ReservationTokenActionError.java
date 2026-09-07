package ar.edu.itba.paw.models.reservation;

/**
 * Expected token action failures;
 */
public enum ReservationTokenActionError {
    INVALID_TOKEN,
    NOT_FOUND,
    ALREADY_USED,
    EXPIRED,
    MISSING_PICKUP_CODE,
    INVALID_PICKUP_CODE
}
