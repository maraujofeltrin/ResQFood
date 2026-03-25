package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

public class Reservation {
    public enum Status {
        RESERVED,
        PAID,
        CANCELED,
        EXPIRED
    }

    private final Long id;
    private final Long customerId;
    private final Long packId;
    private final LocalDateTime reservationDate;
    private final Double finalPrice;
    private Status status;
    private final String pickupCode;
    private final LocalDateTime pickupConfirmationDate;

    public Reservation(Long id, Long customerId, Long packId, LocalDateTime reservationDate, Double finalPrice,
            Status status, String pickupCode, LocalDateTime pickupConfirmationDate) {
        this.id = id;
        this.customerId = customerId;
        this.packId = packId;
        this.reservationDate = reservationDate;
        this.finalPrice = finalPrice;
        this.status = status;
        this.pickupCode = pickupCode;
        this.pickupConfirmationDate = pickupConfirmationDate;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getPackId() {
        return packId;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public Status getStatus() {
        return status;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public LocalDateTime getPickupConfirmationDate() {
        return pickupConfirmationDate;
    }

    @Override
    public String toString() {
        return "Reservation [id=" + id + ", customerId=" + customerId + ", packId=" + packId
                + ", reservationDate=" + reservationDate + ", finalPrice=" + finalPrice + ", status=" + status
                + ", pickupCode=" + pickupCode + ", pickupConfirmationDate=" + pickupConfirmationDate + "]";
    }
}
