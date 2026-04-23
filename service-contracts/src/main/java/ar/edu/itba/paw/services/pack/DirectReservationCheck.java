package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of validating whether a direct (non-auction) reservation may proceed.
 */
public final class DirectReservationCheck {

    public enum Outcome {
        /** Pack missing or inactive, or not suitable for direct sale. */
        PACK_UNAVAILABLE,
        /** An active auction blocks direct reservation. */
        AUCTION_ACTIVE,
        /** Auction finished and pack not available for direct purchase as in the controller rules. */
        AUCTION_ENDED_NO_DIRECT,
        /** Quantity exceeds available stock. */
        QUANTITY_EXCEEDS_STOCK,
        /** Direct-sale price is not set on the pack. */
        MISSING_FINAL_PRICE,
        OK
    }

    private final Outcome outcome;
    private final Pack pack;
    private final Integer availableStock;
    private final double unitPrice;

    private DirectReservationCheck(final Outcome outcome, final Pack pack, final Integer availableStock,
            final double unitPrice) {
        this.outcome = outcome;
        this.pack = pack;
        this.availableStock = availableStock;
        this.unitPrice = unitPrice;
    }

    public static DirectReservationCheck ok(final Pack pack, final double finalPrice) {
        return new DirectReservationCheck(Outcome.OK, Objects.requireNonNull(pack), pack.getStock(), finalPrice);
    }

    public static DirectReservationCheck blocked(final Outcome reason, final Pack pack) {
        return new DirectReservationCheck(reason, pack, pack != null ? pack.getStock() : null, 0d);
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Optional<Pack> getPack() {
        return Optional.ofNullable(pack);
    }

    public Optional<Integer> getAvailableStock() {
        return Optional.ofNullable(availableStock);
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
