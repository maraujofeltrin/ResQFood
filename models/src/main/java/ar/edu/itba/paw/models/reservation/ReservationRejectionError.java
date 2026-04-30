package ar.edu.itba.paw.models.reservation;

/**
 * Expected rejection failure reasons;
 */
public enum ReservationRejectionError {
    INVALID_PARAMS,
    RESERVATION_NOT_FOUND,
    PACK_NOT_FOUND,
    WRONG_COMMERCE,
    ALREADY_CANCELED,
    ALREADY_COMPLETED,
    INVALID_STATUS,
    STOCK_RESTORE_FAILED
}
