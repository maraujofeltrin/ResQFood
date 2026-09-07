package ar.edu.itba.paw.models.reservation;

/**
 * Error codes for pickup-by-code (commerce) flows; i18n mapping stays in the web layer.
 */
public enum PickupByCodeError {
    EMPTY,
    NOT_FOUND,
    WRONG_COMMERCE,
    ALREADY_COMPLETED,
    ALREADY_CANCELED
}
